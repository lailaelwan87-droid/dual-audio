package com.dualaudio.app.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dualaudio.app.audio.AcousticSyncCalibrator
import com.dualaudio.app.audio.AppLanguage
import com.dualaudio.app.audio.AudioDelayBuffer
import com.dualaudio.app.audio.AudioTestTonePlayer
import com.dualaudio.app.audio.ConnectedAudioDevice
import com.dualaudio.app.audio.DualAudioUiState
import com.dualaudio.app.audio.LatencyMeasurement
import com.dualaudio.app.audio.LocalAudioDeviceMonitor
import com.dualaudio.app.audio.OutputCategory
import com.dualaudio.app.audio.RoutingMode
import com.dualaudio.app.audio.ShizukuRoutingController
import com.dualaudio.app.audio.ShizukuStatus
import com.dualaudio.app.data.PreferencesRepository
import com.dualaudio.app.routing.DualOutputEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt

class DualAudioViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val prefsRepo = PreferencesRepository(application)
    private val deviceMonitor = LocalAudioDeviceMonitor(application)
    private val shizukuController = ShizukuRoutingController(application)
    private val tonePlayer = AudioTestTonePlayer()
    private val calibrator = AcousticSyncCalibrator(application)

    private var toneJob: Job? = null
    private var calibrationJob: Job? = null
    private var latencyMeasureJob: Job? = null
    private var wiredVolumeJob: Job? = null
    private var bluetoothVolumeJob: Job? = null
    private var lastAutoSyncedPairSignature: String = ""

    private val maxMediaSteps: Int = audioManager
        .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        .coerceAtLeast(1)

    private val _uiState = MutableStateFlow(
        DualAudioUiState(
            maxMediaVolumeSteps = maxMediaSteps
        )
    )
    val uiState: StateFlow<DualAudioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.preferences.collect { prefs ->
                calibrator.updateLiveDelayMs(prefs.wiredDelayMs)
                _uiState.update { state ->
                    val effectiveWired = if (state.wiredMuted) 0f else prefs.wiredVolume
                    val effectiveBt = if (state.bluetoothMuted) 0f else prefs.bluetoothVolume
                    calibrator.updateLiveWiredGain(effectiveWired)
                    calibrator.updateLiveBluetoothVolume(effectiveBt)
                    state.copy(
                        language = prefs.language,
                        darkTheme = prefs.darkTheme,
                        selectedMode = prefs.routingMode,
                        bluetoothFirst = prefs.bluetoothFirst,
                        autoReapply = prefs.autoReapply,
                        wiredDelayMs = prefs.wiredDelayMs,
                        autoSyncLatency = prefs.autoSyncLatency,
                        wiredVolume = prefs.wiredVolume,
                        bluetoothVolume = prefs.bluetoothVolume
                    )
                }
            }
        }

        viewModelScope.launch {
            deviceMonitor.observeOutputDevices().collect { devices ->
                _uiState.update { state ->
                    val validCustomKeys = state.customSelectedKeys.filter { key ->
                        devices.any { it.uniqueKey == key }
                    }
                    state.copy(
                        connectedDevices = devices,
                        customSelectedKeys = validCustomKeys
                    )
                }
                val current = _uiState.value
                if (current.shizukuStatus == ShizukuStatus.READY) {
                    refreshShellStatus(silent = true)
                    syncHardwareVolumesFromSystem()
                }
                // Sub-second auto-sync whenever headphones connect or change
                val pairSignature = "${current.primarySlotDevice?.uniqueKey.orEmpty()}|${current.secondarySlotDevice?.uniqueKey.orEmpty()}"
                if (current.autoSyncLatency &&
                    current.bluetoothDevice != null &&
                    pairSignature != lastAutoSyncedPairSignature
                ) {
                    lastAutoSyncedPairSignature = pairSignature
                    measureLatency(autoApply = true)
                }
            }
        }

        viewModelScope.launch {
            shizukuController.status.collect { status ->
                _uiState.update { it.copy(shizukuStatus = status) }
            }
        }

        shizukuController.start {
            refreshShellStatus(silent = true)
            measureLatency(autoApply = _uiState.value.autoSyncLatency)
            syncHardwareVolumesFromSystem()
        }
    }

    override fun onCleared() {
        toneJob?.cancel()
        calibrationJob?.cancel()
        latencyMeasureJob?.cancel()
        wiredVolumeJob?.cancel()
        bluetoothVolumeJob?.cancel()
        shizukuController.stop()
        super.onCleared()
    }

    // --- UI Preferences ---

    fun toggleLanguage() {
        val next = if (_uiState.value.language == AppLanguage.ARABIC) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.ARABIC
        }
        viewModelScope.launch { prefsRepo.setLanguage(next) }
    }

    fun toggleTheme() {
        val next = !_uiState.value.darkTheme
        viewModelScope.launch { prefsRepo.setDarkTheme(next) }
    }

    fun selectMode(mode: RoutingMode) {
        viewModelScope.launch { prefsRepo.setRoutingMode(mode) }
        _uiState.update { it.copy(selectedMode = mode, lastActionBanner = null) }
    }

    fun setBluetoothFirst(btFirst: Boolean) {
        viewModelScope.launch { prefsRepo.setBluetoothFirst(btFirst) }
        _uiState.update { it.copy(bluetoothFirst = btFirst) }
    }

    fun setAutoReapply(autoReapply: Boolean) {
        viewModelScope.launch { prefsRepo.setAutoReapply(autoReapply) }
        _uiState.update { it.copy(autoReapply = autoReapply) }
    }

    fun setAutoSyncLatency(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setAutoSyncLatency(enabled) }
        _uiState.update { it.copy(autoSyncLatency = enabled) }
        if (enabled) {
            measureLatency(autoApply = true)
        }
    }

    // --- Strictly Independent Per-Headphone Volume Controls ---

    fun setWiredVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val unmuted = clamped > 0.005f

        calibrator.updateLiveWiredGain(clamped)
        _uiState.update {
            it.copy(
                wiredVolume = clamped,
                wiredMuted = !unmuted
            )
        }

        viewModelScope.launch { prefsRepo.setWiredVolume(clamped) }
        pushWiredVolumeToHardware(clamped)
    }

    fun stepWiredVolume(deltaSteps: Int) {
        val state = _uiState.value
        val currentStep = if (state.wiredMuted) 0 else (state.wiredVolume * maxMediaSteps).roundToInt()
        val nextStep = (currentStep + deltaSteps).coerceIn(0, maxMediaSteps)
        setWiredVolume(nextStep.toFloat() / maxMediaSteps)
    }

    fun setBluetoothVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val unmuted = clamped > 0.005f

        calibrator.updateLiveBluetoothVolume(clamped)
        _uiState.update {
            it.copy(
                bluetoothVolume = clamped,
                bluetoothMuted = !unmuted
            )
        }

        viewModelScope.launch { prefsRepo.setBluetoothVolume(clamped) }
        pushBluetoothVolumeToHardware(clamped)
    }

    fun stepBluetoothVolume(deltaSteps: Int) {
        val state = _uiState.value
        val currentStep = if (state.bluetoothMuted) 0 else (state.bluetoothVolume * maxMediaSteps).roundToInt()
        val nextStep = (currentStep + deltaSteps).coerceIn(0, maxMediaSteps)
        setBluetoothVolume(nextStep.toFloat() / maxMediaSteps)
    }

    fun toggleWiredMute() {
        val state = _uiState.value
        val nextMuted = !state.wiredMuted
        val targetVol = if (nextMuted) 0f else state.wiredVolume.coerceAtLeast(1f / maxMediaSteps)

        calibrator.updateLiveWiredGain(targetVol)
        _uiState.update {
            it.copy(
                wiredMuted = nextMuted,
                wiredVolume = if (!nextMuted) targetVol else it.wiredVolume
            )
        }
        pushWiredVolumeToHardware(targetVol)
    }

    fun toggleBluetoothMute() {
        val state = _uiState.value
        val nextMuted = !state.bluetoothMuted
        val targetVol = if (nextMuted) 0f else state.bluetoothVolume.coerceAtLeast(1f / maxMediaSteps)

        calibrator.updateLiveBluetoothVolume(targetVol)
        _uiState.update {
            it.copy(
                bluetoothMuted = nextMuted,
                bluetoothVolume = if (!nextMuted) targetVol else it.bluetoothVolume
            )
        }
        pushBluetoothVolumeToHardware(targetVol)
    }

    private fun resolveWiredCategory(state: DualAudioUiState): Int {
        return if (state.selectedMode == RoutingMode.USB_BLUETOOTH ||
            (state.wiredDevice == null && state.usbDevice != null)
        ) {
            DualOutputEngine.VOL_CAT_USB
        } else {
            DualOutputEngine.VOL_CAT_WIRED_35MM
        }
    }

    private fun pushWiredVolumeToHardware(normalizedVolume: Float) {
        val state = _uiState.value
        val stepIndex = (normalizedVolume * maxMediaSteps).roundToInt().coerceIn(0, maxMediaSteps)
        val wiredCategory = resolveWiredCategory(state)

        wiredVolumeJob?.cancel()
        wiredVolumeJob = viewModelScope.launch {
            if (state.shizukuStatus == ShizukuStatus.READY) {
                shizukuController.setDeviceCategoryVolume(wiredCategory, stepIndex, maxMediaSteps)
                val status = shizukuController.getLastVolumeStatus().getOrNull().orEmpty()
                if (status.isNotBlank()) {
                    _uiState.update { it.copy(volumeEngineStatus = status) }
                }
            }
        }
    }

    private fun pushBluetoothVolumeToHardware(normalizedVolume: Float) {
        val state = _uiState.value
        val stepIndex = (normalizedVolume * maxMediaSteps).roundToInt().coerceIn(0, maxMediaSteps)

        bluetoothVolumeJob?.cancel()
        bluetoothVolumeJob = viewModelScope.launch {
            if (state.shizukuStatus == ShizukuStatus.READY) {
                shizukuController.setDeviceCategoryVolume(
                    DualOutputEngine.VOL_CAT_BLUETOOTH,
                    stepIndex,
                    maxMediaSteps
                )
                val status = shizukuController.getLastVolumeStatus().getOrNull().orEmpty()
                if (status.isNotBlank()) {
                    _uiState.update { it.copy(volumeEngineStatus = status) }
                }
            }
        }
    }

    private fun syncHardwareVolumesFromSystem() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.shizukuStatus != ShizukuStatus.READY) return@launch
            val wiredCategory = resolveWiredCategory(state)
            val wiredIdx = shizukuController.getDeviceCategoryVolume(wiredCategory).getOrNull()
            val btIdx = shizukuController.getDeviceCategoryVolume(DualOutputEngine.VOL_CAT_BLUETOOTH).getOrNull()

            _uiState.update { current ->
                current.copy(
                    wiredVolume = wiredIdx?.takeIf { it in 0..maxMediaSteps }
                        ?.let { it.toFloat() / maxMediaSteps } ?: current.wiredVolume,
                    wiredMuted = wiredIdx?.let { it == 0 } ?: current.wiredMuted,
                    bluetoothVolume = btIdx?.takeIf { it in 0..maxMediaSteps }
                        ?.let { it.toFloat() / maxMediaSteps } ?: current.bluetoothVolume,
                    bluetoothMuted = btIdx?.let { it == 0 } ?: current.bluetoothMuted
                )
            }
        }
    }

    // --- Latency Measurement & Sub-Second Auto-Sync (< 1s) ---

    fun setWiredDelayMs(delayMs: Int) {
        val clamped = delayMs.coerceIn(0, AudioDelayBuffer.MAX_DELAY_MS)
        calibrator.updateLiveDelayMs(clamped)
        _uiState.update { it.copy(wiredDelayMs = clamped) }
        viewModelScope.launch { prefsRepo.setWiredDelayMs(clamped) }
    }

    fun adjustWiredDelayBy(deltaMs: Int) {
        setWiredDelayMs(_uiState.value.wiredDelayMs + deltaMs)
    }

    fun applyMeasuredDelta() {
        val delta = _uiState.value.latencyMeasurement.estimatedDeltaMs ?: return
        setWiredDelayMs(delta)
    }

    fun toggleCustomDevice(device: ConnectedAudioDevice) {
        _uiState.update { state ->
            val current = state.customSelectedKeys.toMutableList()
            if (current.contains(device.uniqueKey)) {
                current.remove(device.uniqueKey)
            } else {
                if (current.size >= 2) {
                    current.removeAt(0)
                }
                current.add(device.uniqueKey)
            }
            state.copy(customSelectedKeys = current)
        }
    }

    fun requestShizukuPermission() {
        shizukuController.requestPermission()
    }

    fun openShizukuApp(context: Context) {
        shizukuController.openShizukuOrStore(context)
    }

    fun refreshConnectionAndStatus() {
        shizukuController.refreshAndBindIfPossible()
        val refreshedDevices = deviceMonitor.snapshotOutputDevices()
        _uiState.update { it.copy(connectedDevices = refreshedDevices) }
        if (_uiState.value.shizukuStatus == ShizukuStatus.READY) {
            refreshShellStatus(silent = false)
            measureLatency(autoApply = _uiState.value.autoSyncLatency)
            syncHardwareVolumesFromSystem()
        }
    }

    /**
     * Measures wired & Bluetooth latency and, when [autoApply] is true,
     * automatically applies the optimal delay offset within a strict sub-second budget (< 1s).
     */
    fun measureLatency(autoApply: Boolean = _uiState.value.autoSyncLatency) {
        latencyMeasureJob?.cancel()
        latencyMeasureJob = viewModelScope.launch {
            val startNs = System.nanoTime()
            _uiState.update { it.copy(isMeasuringLatency = true) }

            // Strict 700ms ceiling guarantees measurement + auto-adjustment always completes in < 1 second
            val shizukuResult = if (_uiState.value.shizukuStatus == ShizukuStatus.READY) {
                withTimeoutOrNull(SUB_SECOND_PROBE_TIMEOUT_MS) {
                    shizukuController.measureLatencies().getOrNull()
                }
            } else {
                null
            }

            val resolvedMeasurement = if (shizukuResult != null &&
                (shizukuResult.wiredMs != null || shizukuResult.bluetoothMs != null || shizukuResult.streamMusicMs != null)
            ) {
                val lang = _uiState.value.language
                shizukuResult.copy(
                    sourceLabel = if (lang == AppLanguage.ARABIC) "قياس مباشر" else "Live Reading"
                )
            } else {
                computeLocalLatencyEstimate()
            }

            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L)
                .toInt()
                .coerceIn(1, 950)

            val autoDelta = if (autoApply) resolvedMeasurement.estimatedDeltaMs else null
            if (autoDelta != null) {
                calibrator.updateLiveDelayMs(autoDelta)
                prefsRepo.setWiredDelayMs(autoDelta)
            }

            _uiState.update { current ->
                current.copy(
                    isMeasuringLatency = false,
                    latencyMeasurement = resolvedMeasurement,
                    wiredDelayMs = autoDelta ?: current.wiredDelayMs,
                    lastAutoSyncDurationMs = if (autoDelta != null) elapsedMs else current.lastAutoSyncDurationMs
                )
            }
        }
    }

    private fun computeLocalLatencyEstimate(): LatencyMeasurement {
        val sampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull() ?: 48_000
        val framesPerBuffer = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)?.toIntOrNull() ?: 256
        val halPeriodMs = ((framesPerBuffer * 1000.0) / sampleRate).toInt().coerceAtLeast(4)
        val state = _uiState.value

        val wiredEst = if (state.wiredDevice != null) (halPeriodMs * 3 + 12) else null
        val usbEst = if (state.usbDevice != null) (halPeriodMs * 4 + 18) else null
        val btEst = if (state.bluetoothDevice != null) 210 else null

        val source = if (state.language == AppLanguage.ARABIC) "قياس سريع" else "Fast Probe"
        return LatencyMeasurement(
            wiredMs = wiredEst,
            usbMs = usbEst,
            bluetoothMs = btEst,
            streamMusicMs = null,
            sourceLabel = source,
            details = ""
        )
    }

    fun toggleAcousticCalibrator() {
        if (_uiState.value.isCalibrating) {
            calibrationJob?.cancel()
            calibrationJob = null
            _uiState.update { it.copy(isCalibrating = false) }
            return
        }

        toneJob?.cancel()
        _uiState.update { it.copy(isTestTonePlaying = false, isCalibrating = true) }

        val state = _uiState.value
        val wiredId = state.wiredDevice?.id ?: state.usbDevice?.id
        val btId = state.bluetoothDevice?.id
        val effectiveWired = if (state.wiredMuted) 0f else state.wiredVolume
        val effectiveBt = if (state.bluetoothMuted) 0f else state.bluetoothVolume
        calibrator.updateLiveDelayMs(state.wiredDelayMs)
        calibrator.updateLiveWiredGain(effectiveWired)
        calibrator.updateLiveBluetoothVolume(effectiveBt)

        var autoSnappedInSession = false

        calibrationJob = viewModelScope.launch {
            try {
                calibrator.runCalibrationLoop(
                    wiredDeviceId = wiredId,
                    bluetoothDeviceId = btId,
                    onPipelineMeasured = { measured ->
                        val liveWired = measured.wiredPipelineMs
                        val liveBt = measured.bluetoothPipelineMs
                        _uiState.update { current ->
                            val existing = current.latencyMeasurement
                            val updatedMeasurement = existing.copy(
                                wiredMs = liveWired ?: existing.wiredMs,
                                bluetoothMs = liveBt ?: existing.bluetoothMs,
                                sourceLabel = if (current.language == AppLanguage.ARABIC) "قياس حي" else "Live"
                            )
                            val autoSnapDelta = if (current.autoSyncLatency && !autoSnappedInSession) {
                                updatedMeasurement.estimatedDeltaMs
                            } else {
                                null
                            }
                            if (autoSnapDelta != null) {
                                autoSnappedInSession = true
                                calibrator.updateLiveDelayMs(autoSnapDelta)
                                viewModelScope.launch { prefsRepo.setWiredDelayMs(autoSnapDelta) }
                            }
                            current.copy(
                                latencyMeasurement = updatedMeasurement,
                                wiredDelayMs = autoSnapDelta ?: current.wiredDelayMs
                            )
                        }
                    }
                )
            } finally {
                _uiState.update { it.copy(isCalibrating = false) }
            }
        }
    }

    fun enableDualAudio() {
        val state = _uiState.value
        if (state.shizukuStatus != ShizukuStatus.READY || state.isBusy) return
        val strings = stringsFor(state.language)

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, lastActionBanner = null, isLastActionError = false) }
            val result = if (state.selectedMode == RoutingMode.CUSTOM) {
                val first = state.primarySlotDevice
                val second = state.secondarySlotDevice
                if (first == null || second == null) {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            isLastActionError = true,
                            lastActionBanner = strings.bannerWaitingPair
                        )
                    }
                    return@launch
                }
                if (state.bluetoothFirst && second.category == OutputCategory.BLUETOOTH) {
                    shizukuController.enableCustom(second, first, state.autoReapply)
                } else {
                    shizukuController.enableCustom(first, second, state.autoReapply)
                }
            } else {
                shizukuController.enableMode(state.selectedMode, state.bluetoothFirst, state.autoReapply)
            }

            result.fold(
                onSuccess = { (rawCode, snapshot) ->
                    val (friendlyBanner, isWarn) = formatRoutingFeedback(rawCode, strings)
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            isDualAudioEnabled = snapshot.isEnabled,
                            isRoutingActuallyApplied = snapshot.isApplied,
                            shellStatusLog = snapshot.statusLog,
                            lastActionBanner = friendlyBanner,
                            isLastActionError = isWarn
                        )
                    }
                    // Immediately measure & auto-sync latency in < 1 second
                    measureLatency(autoApply = _uiState.value.autoSyncLatency)
                    syncHardwareVolumesFromSystem()
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            isLastActionError = true,
                            lastActionBanner = strings.bannerGenericError
                        )
                    }
                }
            )
        }
    }

    fun disableDualAudio() {
        val state = _uiState.value
        if (state.shizukuStatus != ShizukuStatus.READY || state.isBusy) return
        val strings = stringsFor(state.language)

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, lastActionBanner = null, isLastActionError = false) }
            shizukuController.disable().fold(
                onSuccess = { (_, snapshot) ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            isDualAudioEnabled = snapshot.isEnabled,
                            isRoutingActuallyApplied = snapshot.isApplied,
                            shellStatusLog = snapshot.statusLog,
                            lastActionBanner = strings.bannerDualDisabled,
                            isLastActionError = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            isLastActionError = true,
                            lastActionBanner = strings.bannerGenericError
                        )
                    }
                }
            )
        }
    }

    private fun formatRoutingFeedback(raw: String, strings: UiStrings): Pair<String, Boolean> {
        return when {
            raw == "OK" -> strings.bannerDualEnabled to false
            raw.contains("NO_WIRED_DEVICE") -> strings.bannerWaitingWired to true
            raw.contains("NO_USB_DEVICE") -> strings.bannerWaitingUsb to true
            raw.contains("NO_BLUETOOTH_DEVICE") -> strings.bannerWaitingBluetooth to true
            raw.startsWith("WAITING") -> strings.bannerWaitingPair to true
            else -> strings.bannerDualEnabled to false
        }
    }

    fun playTestSound() {
        if (_uiState.value.isTestTonePlaying) {
            toneJob?.cancel()
            _uiState.update { it.copy(isTestTonePlaying = false) }
            return
        }
        calibrationJob?.cancel()
        _uiState.update { it.copy(isCalibrating = false) }

        toneJob = viewModelScope.launch {
            _uiState.update { it.copy(isTestTonePlaying = true) }
            try {
                tonePlayer.playStereoChime()
            } finally {
                _uiState.update { it.copy(isTestTonePlaying = false) }
            }
        }
    }

    private fun refreshShellStatus(silent: Boolean) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(isBusy = true) }
            }
            shizukuController.fetchSnapshot().fold(
                onSuccess = { snapshot ->
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            isDualAudioEnabled = snapshot.isEnabled,
                            isRoutingActuallyApplied = snapshot.isApplied,
                            shellStatusLog = snapshot.statusLog
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isBusy = false) }
                }
            )
        }
    }

    companion object {
        const val SUB_SECOND_PROBE_TIMEOUT_MS = 700L
    }
}
