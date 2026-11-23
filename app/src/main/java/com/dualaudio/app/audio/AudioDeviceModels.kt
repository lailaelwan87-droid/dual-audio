package com.dualaudio.app.audio

import com.dualaudio.app.routing.DualOutputEngine

enum class RoutingMode(val code: Int) {
    WIRED_BLUETOOTH(DualOutputEngine.MODE_WIRED_BLUETOOTH),
    USB_BLUETOOTH(DualOutputEngine.MODE_USB_BLUETOOTH),
    AUTO_BLUETOOTH(DualOutputEngine.MODE_AUTO_BLUETOOTH),
    CUSTOM(DualOutputEngine.MODE_CUSTOM);

    companion object {
        fun fromCode(code: Int): RoutingMode =
            entries.firstOrNull { it.code == code } ?: WIRED_BLUETOOTH
    }
}

enum class OutputCategory {
    WIRED_35MM,
    USB_AUDIO,
    BLUETOOTH,
    BUILTIN_SPEAKER,
    OTHER
}

data class ConnectedAudioDevice(
    val id: Int,
    val type: Int,
    val name: String,
    val address: String,
    val category: OutputCategory,
    val typeLabel: String
) {
    val uniqueKey: String get() = "${type}_${address}_${id}"
}

data class LatencyMeasurement(
    val wiredMs: Int? = null,
    val usbMs: Int? = null,
    val bluetoothMs: Int? = null,
    val streamMusicMs: Int? = null,
    val sourceLabel: String = "",
    val details: String = ""
) {
    val activeWiredOrUsbMs: Int? get() = wiredMs ?: usbMs

    val estimatedDeltaMs: Int?
        get() {
            val bt = bluetoothMs ?: return null
            val wired = activeWiredOrUsbMs ?: 25
            return (bt - wired).coerceIn(0, AudioDelayBuffer.MAX_DELAY_MS)
        }
}

enum class ShizukuStatus {
    ANDROID_TOO_OLD,
    SHIZUKU_NOT_INSTALLED,
    SHIZUKU_NOT_RUNNING,
    SHIZUKU_TOO_OLD,
    PERMISSION_REQUIRED,
    CONNECTING_SERVICE,
    READY
}

enum class AppLanguage {
    ARABIC,
    ENGLISH
}

data class DualAudioUiState(
    val language: AppLanguage = AppLanguage.ARABIC,
    val darkTheme: Boolean = true,
    val shizukuStatus: ShizukuStatus = ShizukuStatus.SHIZUKU_NOT_RUNNING,
    val selectedMode: RoutingMode = RoutingMode.WIRED_BLUETOOTH,
    val bluetoothFirst: Boolean = false,
    val autoReapply: Boolean = true,
    val isDualAudioEnabled: Boolean = false,
    val isRoutingActuallyApplied: Boolean = false,
    val isBusy: Boolean = false,
    val isTestTonePlaying: Boolean = false,
    val connectedDevices: List<ConnectedAudioDevice> = emptyList(),
    val customSelectedKeys: List<String> = emptyList(),
    val lastActionBanner: String? = null,
    val isLastActionError: Boolean = false,
    val shellStatusLog: String = "",

    // Latency Measurement & Sub-Second Auto-Sync Controls
    val wiredDelayMs: Int = 200,
    val autoSyncLatency: Boolean = true,
    val lastAutoSyncDurationMs: Int? = null,
    val isMeasuringLatency: Boolean = false,
    val latencyMeasurement: LatencyMeasurement = LatencyMeasurement(),
    val isCalibrating: Boolean = false,

    // Independent Per-Headphone Volume Controls
    val wiredVolume: Float = 0.75f,
    val bluetoothVolume: Float = 0.75f,
    val wiredMuted: Boolean = false,
    val bluetoothMuted: Boolean = false,
    val maxMediaVolumeSteps: Int = 15,
    val volumeEngineStatus: String = ""
) {
    val wiredDevice: ConnectedAudioDevice?
        get() = connectedDevices.firstOrNull { it.category == OutputCategory.WIRED_35MM }

    val usbDevice: ConnectedAudioDevice?
        get() = connectedDevices.firstOrNull { it.category == OutputCategory.USB_AUDIO }

    val bluetoothDevice: ConnectedAudioDevice?
        get() = connectedDevices.firstOrNull { it.category == OutputCategory.BLUETOOTH }

    val primarySlotDevice: ConnectedAudioDevice?
        get() = when (selectedMode) {
            RoutingMode.WIRED_BLUETOOTH -> wiredDevice
            RoutingMode.USB_BLUETOOTH -> usbDevice
            RoutingMode.AUTO_BLUETOOTH -> wiredDevice ?: usbDevice
            RoutingMode.CUSTOM -> customSelectedKeys.getOrNull(0)?.let { key ->
                connectedDevices.firstOrNull { it.uniqueKey == key }
            }
        }

    val secondarySlotDevice: ConnectedAudioDevice?
        get() = when (selectedMode) {
            RoutingMode.CUSTOM -> customSelectedKeys.getOrNull(1)?.let { key ->
                connectedDevices.firstOrNull { it.uniqueKey == key }
            }
            else -> bluetoothDevice
        }

    val bothTargetDevicesConnected: Boolean
        get() = primarySlotDevice != null && secondarySlotDevice != null
}
