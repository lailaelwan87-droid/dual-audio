package com.dualaudio.app.routing

import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * Kotlin Shizuku UserService hosting DualOutputEngine with a scheduled
 * background monitor for automatic reconnection recovery.
 *
 * Derived from and extending the Shizuku UserService daemon architecture of xTudoS/dual-audio
 * (https://github.com/xTudoS/dual-audio, Copyright (C) 2026 xTudoS) under GPL-3.0-or-later.
 */
class DualOutputShizukuService : IAudioRoutingService.Stub() {

    private val engine: DualOutputEngine = DualOutputEngine()
    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "dual-output-monitor").apply { isDaemon = true }
    }

    @Volatile private var active = false
    @Volatile private var autoReapply = true
    @Volatile private var mode = DualOutputEngine.MODE_WIRED_BLUETOOTH
    @Volatile private var bluetoothFirst = false
    @Volatile private var customType1 = -1
    @Volatile private var customAddr1 = ""
    @Volatile private var customType2 = -1
    @Volatile private var customAddr2 = ""

    private var monitorTask: ScheduledFuture<*>? = null

    override fun destroy() {
        stopMonitor()
        scheduler.shutdownNow()
        exitProcess(0)
    }

    @Synchronized
    override fun enableWithMode(mode: Int, bluetoothFirst: Boolean, autoReapply: Boolean): String {
        this.mode = mode
        this.bluetoothFirst = bluetoothFirst
        this.autoReapply = autoReapply
        this.active = true

        val response = try {
            val code = engine.applyDualMode(mode, bluetoothFirst)
            if (code == 0) "OK" else "CODE_$code"
        } catch (e: Exception) {
            "WAITING:${extractRootReason(e)}"
        }

        if (autoReapply) startMonitor() else stopMonitor()
        return response
    }

    @Synchronized
    override fun enableCustom(
        type1: Int,
        addr1: String?,
        type2: Int,
        addr2: String?,
        autoReapply: Boolean
    ): String {
        this.mode = DualOutputEngine.MODE_CUSTOM
        this.customType1 = type1
        this.customAddr1 = addr1.orEmpty()
        this.customType2 = type2
        this.customAddr2 = addr2.orEmpty()
        this.autoReapply = autoReapply
        this.active = true

        val response = try {
            val code = engine.applyCustomPair(type1, customAddr1, type2, customAddr2)
            if (code == 0) "OK" else "CODE_$code"
        } catch (e: Exception) {
            "WAITING:${extractRootReason(e)}"
        }

        if (autoReapply) startMonitor() else stopMonitor()
        return response
    }

    @Synchronized
    override fun disable(): String {
        active = false
        stopMonitor()
        val code = engine.restoreDefaultRouting()
        return if (code == 0) "DISABLED" else "CODE_$code"
    }

    override fun status(): String {
        return buildString {
            appendLine(engine.buildDiagnosticSummary())
            appendLine("Service Active: ${if (active) "ON" else "OFF"}")
            append("Auto-Reapply: ${if (active && autoReapply) "ON" else "OFF"}")
        }
    }

    override fun isEnabled(): Boolean = active

    override fun getActiveMode(): Int = mode

    override fun isRoutingApplied(): Boolean {
        return runCatching {
            engine.isRouteActive(mode, customType1, customAddr1, customType2, customAddr2)
        }.getOrDefault(false)
    }

    override fun measureLatencyReport(): String = engine.inspectLatencyReport()

    override fun setDeviceCategoryVolume(category: Int, index: Int, maxIndex: Int): Int =
        engine.applyIndependentCategoryVolume(category, index, maxIndex)

    override fun getDeviceCategoryVolume(category: Int): Int =
        engine.readIndependentCategoryVolume(category)

    override fun getLastVolumeStatus(): String = engine.lastVolumeDiagnostic

    @Synchronized
    private fun startMonitor() {
        if (monitorTask != null && monitorTask?.isCancelled == false) return
        monitorTask = scheduler.scheduleWithFixedDelay(
            {
                if (!active || !autoReapply) return@scheduleWithFixedDelay
                runCatching {
                    val ready = engine.areTargetsConnected(
                        mode, customType1, customAddr1, customType2, customAddr2
                    )
                    if (ready && !engine.isRouteActive(
                            mode, customType1, customAddr1, customType2, customAddr2
                        )
                    ) {
                        if (mode == DualOutputEngine.MODE_CUSTOM) {
                            engine.applyCustomPair(customType1, customAddr1, customType2, customAddr2)
                        } else {
                            engine.applyDualMode(mode, bluetoothFirst)
                        }
                    }
                }.onFailure { err ->
                    Log.w(TAG, "Monitor tick skipped", err)
                }
            },
            MONITOR_PERIOD_SEC,
            MONITOR_PERIOD_SEC,
            TimeUnit.SECONDS
        )
    }

    @Synchronized
    private fun stopMonitor() {
        monitorTask?.cancel(false)
        monitorTask = null
    }

    private fun extractRootReason(t: Throwable): String {
        var cur: Throwable = t
        while (cur.cause != null) cur = cur.cause!!
        return cur.message ?: cur.javaClass.simpleName
    }

    companion object {
        private const val TAG = "DualOutputShizuku"
        private const val MONITOR_PERIOD_SEC = 3L
    }
}
