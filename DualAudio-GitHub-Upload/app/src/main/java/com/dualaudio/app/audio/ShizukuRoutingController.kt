package com.dualaudio.app.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import com.dualaudio.app.BuildConfig
import com.dualaudio.app.routing.DualOutputShizukuService
import com.dualaudio.app.routing.IAudioRoutingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

data class ShellSnapshot(
    val statusLog: String,
    val isEnabled: Boolean,
    val isApplied: Boolean,
    val activeMode: RoutingMode
)

/**
 * Manages the Shizuku binder lifecycle and IPC connection to DualOutputShizukuService.
 * Keeps the privileged routing service alive so dual audio continues reapplying
 * automatically in the background when enabled.
 */
class ShizukuRoutingController(private val appContext: Context) {

    private val _status = MutableStateFlow(computeStatus())
    val status: StateFlow<ShizukuStatus> = _status.asStateFlow()

    @Volatile
    private var service: IAudioRoutingService? = null

    private var onServiceReadyCallback: (() -> Unit)? = null
    private var registered = false

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(BuildConfig.APPLICATION_ID, DualOutputShizukuService::class.java.name)
    )
        .daemon(true)
        .processNameSuffix("routing")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
            if (binder == null || !binder.pingBinder()) return
            service = IAudioRoutingService.Stub.asInterface(binder)
            _status.value = computeStatus()
            onServiceReadyCallback?.invoke()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            _status.value = computeStatus()
        }
    }

    private val binderReceived = Shizuku.OnBinderReceivedListener {
        refreshAndBindIfPossible()
    }

    private val binderDead = Shizuku.OnBinderDeadListener {
        service = null
        _status.value = computeStatus()
    }

    private val permissionResult = Shizuku.OnRequestPermissionResultListener { requestCode, result ->
        if (requestCode == REQUEST_CODE_SHIZUKU && result == PackageManager.PERMISSION_GRANTED) {
            refreshAndBindIfPossible()
        } else {
            _status.value = computeStatus()
        }
    }

    fun start(onServiceReady: () -> Unit) {
        onServiceReadyCallback = onServiceReady
        if (!registered) {
            registered = true
            runCatching {
                Shizuku.addBinderReceivedListenerSticky(binderReceived)
                Shizuku.addBinderDeadListener(binderDead)
                Shizuku.addRequestPermissionResultListener(permissionResult)
            }
        }
        refreshAndBindIfPossible()
    }

    fun stop() {
        if (registered) {
            registered = false
            runCatching {
                Shizuku.removeBinderReceivedListener(binderReceived)
                Shizuku.removeBinderDeadListener(binderDead)
                Shizuku.removeRequestPermissionResultListener(permissionResult)
            }
        }
        if (service != null) {
            // Keep daemon alive so auto-reapply continues after closing the app
            runCatching { Shizuku.unbindUserService(userServiceArgs, connection, false) }
            service = null
        }
    }

    fun refreshAndBindIfPossible() {
        val current = computeStatus()
        _status.value = current
        if (current == ShizukuStatus.CONNECTING_SERVICE && service == null) {
            runCatching {
                Shizuku.bindUserService(userServiceArgs, connection)
            }
        }
    }

    fun requestPermission() {
        if (isShizukuRunning() && !hasPermission()) {
            runCatching {
                Shizuku.requestPermission(REQUEST_CODE_SHIZUKU)
            }
        }
    }

    fun openShizukuOrStore(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            val marketIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$SHIZUKU_PACKAGE")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(marketIntent) }
        }
    }

    suspend fun enableMode(
        mode: RoutingMode,
        bluetoothFirst: Boolean,
        autoReapply: Boolean
    ): Result<Pair<String, ShellSnapshot>> = withContext(Dispatchers.IO) {
        val s = service ?: return@withContext Result.failure(IllegalStateException("Shizuku service is not connected"))
        runCatching {
            val actionResult = s.enableWithMode(mode.code, bluetoothFirst, autoReapply)
            actionResult to readSnapshot(s)
        }
    }

    suspend fun enableCustom(
        first: ConnectedAudioDevice,
        second: ConnectedAudioDevice,
        autoReapply: Boolean
    ): Result<Pair<String, ShellSnapshot>> = withContext(Dispatchers.IO) {
        val s = service ?: return@withContext Result.failure(IllegalStateException("Shizuku service is not connected"))
        runCatching {
            val actionResult = s.enableCustom(first.type, first.address, second.type, second.address, autoReapply)
            actionResult to readSnapshot(s)
        }
    }

    suspend fun disable(): Result<Pair<String, ShellSnapshot>> = withContext(Dispatchers.IO) {
        val s = service ?: return@withContext Result.failure(IllegalStateException("Shizuku service is not connected"))
        runCatching {
            val actionResult = s.disable()
            actionResult to readSnapshot(s)
        }
    }

    suspend fun fetchSnapshot(): Result<ShellSnapshot> = withContext(Dispatchers.IO) {
        val s = service ?: return@withContext Result.failure(IllegalStateException("Shizuku service is not connected"))
        runCatching { readSnapshot(s) }
    }

    suspend fun measureLatencies(): Result<LatencyMeasurement> = withContext(Dispatchers.IO) {
        val s = service ?: return@withContext Result.failure(IllegalStateException("Shizuku service is not connected"))
        runCatching {
            val raw = s.measureLatencyReport().orEmpty()
            parseLatencyReport(raw)
        }
    }

    suspend fun setDeviceCategoryVolume(category: Int, index: Int, maxIndex: Int): Result<Int> = withContext(Dispatchers.IO) {
        val s = service ?: return@withContext Result.failure(IllegalStateException("Shizuku service is not connected"))
        runCatching { s.setDeviceCategoryVolume(category, index, maxIndex) }
    }

    suspend fun getDeviceCategoryVolume(category: Int): Result<Int> = withContext(Dispatchers.IO) {
        val s = service ?: return@withContext Result.failure(IllegalStateException("Shizuku service is not connected"))
        runCatching { s.getDeviceCategoryVolume(category) }
    }

    suspend fun getLastVolumeStatus(): Result<String> = withContext(Dispatchers.IO) {
        val s = service ?: return@withContext Result.failure(IllegalStateException("Shizuku service is not connected"))
        runCatching { s.lastVolumeStatus.orEmpty() }
    }

    private fun parseLatencyReport(raw: String): LatencyMeasurement {
        val map = raw.lines()
            .mapNotNull { line ->
                val idx = line.indexOf('=')
                if (idx > 0) line.substring(0, idx).trim() to line.substring(idx + 1).trim() else null
            }
            .toMap()

        fun positiveInt(key: String): Int? = map[key]?.toIntOrNull()?.takeIf { it >= 0 }

        return LatencyMeasurement(
            wiredMs = positiveInt("wiredMs"),
            usbMs = positiveInt("usbMs"),
            bluetoothMs = positiveInt("bluetoothMs"),
            streamMusicMs = positiveInt("streamMusicMs"),
            sourceLabel = "AudioFlinger (Shell)",
            details = map["details"].orEmpty()
        )
    }

    private fun readSnapshot(s: IAudioRoutingService): ShellSnapshot {
        val log = s.status()
        val enabled = s.isEnabled
        val applied = s.isRoutingApplied
        val mode = RoutingMode.fromCode(s.activeMode)
        return ShellSnapshot(
            statusLog = log,
            isEnabled = enabled,
            isApplied = applied,
            activeMode = mode
        )
    }

    private fun computeStatus(): ShizukuStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return ShizukuStatus.ANDROID_TOO_OLD
        }
        if (!isShizukuRunning()) {
            return if (isShizukuInstalled()) {
                ShizukuStatus.SHIZUKU_NOT_RUNNING
            } else {
                ShizukuStatus.SHIZUKU_NOT_INSTALLED
            }
        }
        val preV11 = runCatching { Shizuku.isPreV11() }.getOrDefault(true)
        if (preV11) return ShizukuStatus.SHIZUKU_TOO_OLD
        if (!hasPermission()) return ShizukuStatus.PERMISSION_REQUIRED
        if (service == null) return ShizukuStatus.CONNECTING_SERVICE
        return ShizukuStatus.READY
    }

    private fun isShizukuRunning(): Boolean =
        runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    private fun hasPermission(): Boolean =
        isShizukuRunning() &&
            !runCatching { Shizuku.isPreV11() }.getOrDefault(true) &&
            runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }
                .getOrDefault(false)

    private fun isShizukuInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.packageManager.getPackageInfo(
                    SHIZUKU_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                appContext.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        private const val REQUEST_CODE_SHIZUKU = 1001
    }
}
