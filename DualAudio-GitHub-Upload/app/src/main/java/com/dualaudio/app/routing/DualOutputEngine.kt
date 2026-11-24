package com.dualaudio.app.routing

import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method
import java.util.regex.Pattern
import kotlin.math.roundToInt

/**
 * Kotlin dual-output audio routing, per-device volume control, and AudioFlinger latency
 * inspection engine for privileged Shizuku execution.
 *
 * Derived from and extending the combined audio routing approach of xTudoS/dual-audio
 * (https://github.com/xTudoS/dual-audio, Copyright (C) 2026 xTudoS) under GPL-3.0-or-later.
 */
class DualOutputEngine {

    private val audioServiceBinder: Any
    val mediaStrategyId: Int

    @Volatile
    var lastVolumeDiagnostic: String = "idle"
        private set

    init {
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        val rawBinder = serviceManagerClass
            .getMethod("getService", String::class.java)
            .invoke(null, "audio") as? IBinder
            ?: throw IllegalStateException("System audio service binder unavailable")

        val stubClass = Class.forName("android.media.IAudioService\$Stub")
        audioServiceBinder = stubClass
            .getMethod("asInterface", IBinder::class.java)
            .invoke(null, rawBinder)
            ?: throw IllegalStateException("Unable to attach IAudioService proxy")

        mediaStrategyId = resolveMediaProductStrategyId()
    }

    fun areTargetsConnected(
        mode: Int,
        customType1: Int,
        customAddr1: String,
        customType2: Int,
        customAddr2: String
    ): Boolean {
        if (mode == MODE_CUSTOM) {
            return locateDeviceByTypeAndAddress(customType1, customAddr1) != null &&
                locateDeviceByTypeAndAddress(customType2, customAddr2) != null
        }
        return locatePrimaryEndpoint(mode) != null && locateFirstEndpoint(BLUETOOTH_TYPES) != null
    }

    fun isRouteActive(
        mode: Int,
        customType1: Int,
        customAddr1: String,
        customType2: Int,
        customAddr2: String
    ): Boolean {
        val current = queryPreferredEndpoints()
        if (current.size != 2) return false
        val type0 = readAttributeDeviceType(current[0])
        val type1 = readAttributeDeviceType(current[1])

        if (mode == MODE_CUSTOM) {
            return (type0 == customType1 && type1 == customType2) ||
                (type0 == customType2 && type1 == customType1)
        }

        val pair = intArrayOf(type0, type1)
        val hasPrimary = pair.any { t ->
            when (mode) {
                MODE_WIRED_BLUETOOTH -> t in WIRED_35MM_TYPES
                MODE_USB_BLUETOOTH -> t in USB_TYPES
                else -> t in WIRED_35MM_TYPES || t in USB_TYPES
            }
        }
        val hasBluetooth = pair.any { it in BLUETOOTH_TYPES }
        return hasPrimary && hasBluetooth
    }

    fun applyDualMode(mode: Int, bluetoothFirst: Boolean): Int {
        val primary = locatePrimaryEndpoint(mode)
            ?: throw IllegalStateException(missingPrimaryError(mode))
        val bt = locateFirstEndpoint(BLUETOOTH_TYPES)
            ?: throw IllegalStateException("NO_BLUETOOTH_DEVICE")

        val ordered = if (bluetoothFirst) {
            listOf(createDeviceAttributes(bt), createDeviceAttributes(primary))
        } else {
            listOf(createDeviceAttributes(primary), createDeviceAttributes(bt))
        }
        return pushPreferredEndpoints(ordered)
    }

    fun applyCustomPair(type1: Int, addr1: String, type2: Int, addr2: String): Int {
        val first = locateDeviceByTypeAndAddress(type1, addr1)
            ?: throw IllegalStateException("MISSING_FIRST_DEVICE")
        val second = locateDeviceByTypeAndAddress(type2, addr2)
            ?: throw IllegalStateException("MISSING_SECOND_DEVICE")

        return pushPreferredEndpoints(
            listOf(createDeviceAttributes(first), createDeviceAttributes(second))
        )
    }

    fun restoreDefaultRouting(): Int {
        return try {
            invokeAudioService(
                listOf("removePreferredDevicesForStrategy", "clearPreferredDevicesForStrategy"),
                mediaStrategyId
            ) as Int
        } catch (_: NoSuchMethodException) {
            invokeAudioService(
                listOf("removeDevicesRoleForStrategy"),
                mediaStrategyId,
                ROLE_PREFERRED
            ) as Int
        }
    }

    fun queryPreferredEndpoints(): List<Any> {
        val raw = try {
            invokeAudioService(listOf("getPreferredDevicesForStrategy"), mediaStrategyId)
        } catch (_: NoSuchMethodException) {
            invokeAudioService(
                listOf("getDevicesForRoleAndStrategy"),
                mediaStrategyId,
                ROLE_PREFERRED
            )
        }
        @Suppress("UNCHECKED_CAST")
        return (raw as? List<Any>) ?: emptyList()
    }

    /**
     * Adjusts the independent media volume index for a single headphone category
     * without disturbing the other connected headphone.
     */
    fun applyIndependentCategoryVolume(category: Int, index: Int, maxIndex: Int): Int {
        val safeMax = maxIndex.coerceAtLeast(1)
        val clamped = index.coerceIn(0, safeMax)
        val tag = categoryLabel(category)

        // Primary path (API 33+): IAudioService.setDeviceVolume(VolumeInfo, AudioDeviceAttributes, pkg)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val apiErr = trySetVolumeViaAudioService(category, clamped, safeMax)
            if (apiErr == null) {
                lastVolumeDiagnostic = "OK[$tag=$clamped/$safeMax]:setDeviceVolume"
                return 0
            }
            lastVolumeDiagnostic = "L1[$tag]:$apiErr"
        }

        // Fallback path: Direct AudioSystem per-device bitmask
        val masks = internalDeviceMasksFor(category)
        var outcome = -1
        var usedMethod = "none"

        runCatching {
            val audioSystemCls = Class.forName("android.media.AudioSystem")
            val setAS = lookupStaticMethod(audioSystemCls, "setStreamVolumeIndexAS", Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!)
            val setLegacy = lookupStaticMethod(audioSystemCls, "setStreamVolumeIndex", Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!)
            val setForAttrs = lookupStaticMethod(audioSystemCls, "setVolumeIndexForAttributes", AudioAttributes::class.java, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!)
            val mediaAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()

            for (mask in masks) {
                var res = -1
                if (setAS != null) {
                    res = (setAS.invoke(null, AudioManager.STREAM_MUSIC, clamped, mask) as? Int) ?: -1
                    if (res == 0) usedMethod = "setStreamVolumeIndexAS"
                }
                if (res != 0 && setLegacy != null) {
                    res = (setLegacy.invoke(null, AudioManager.STREAM_MUSIC, clamped, mask) as? Int) ?: -1
                    if (res == 0) usedMethod = "setStreamVolumeIndex"
                }
                if (res != 0 && setForAttrs != null) {
                    res = (setForAttrs.invoke(null, mediaAttrs, clamped, mask) as? Int) ?: -1
                    if (res == 0) usedMethod = "setVolumeIndexForAttributes"
                }
                if (res == 0) {
                    outcome = 0
                } else if (outcome != 0) {
                    outcome = res
                }
            }
        }

        // Extra AVRCP fallback for Bluetooth if previous layers failed
        if (outcome != 0 && category == VOL_CAT_BLUETOOTH) {
            if (trySetBluetoothAvrcpVolume(clamped)) {
                lastVolumeDiagnostic = "OK[$tag=$clamped/$safeMax]:avrcp"
                return 0
            }
        }

        lastVolumeDiagnostic = if (outcome == 0) {
            "OK[$tag=$clamped/$safeMax]:$usedMethod"
        } else {
            "ERR[$tag=$clamped/$safeMax]:code=$outcome"
        }
        return outcome
    }

    fun readIndependentCategoryVolume(category: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val viaService = tryGetVolumeViaAudioService(category)
            if (viaService >= 0) return viaService
        }

        val masks = internalDeviceMasksFor(category)
        runCatching {
            val audioSystemCls = Class.forName("android.media.AudioSystem")
            val getIdx = lookupStaticMethod(audioSystemCls, "getStreamVolumeIndex", Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!)
            if (getIdx != null) {
                for (mask in masks) {
                    val v = (getIdx.invoke(null, AudioManager.STREAM_MUSIC, mask) as? Int) ?: -1
                    if (v >= 0) return v
                }
            }
        }
        return -1
    }

    fun inspectLatencyReport(): String {
        val musicLatency = queryGlobalStreamLatency()
        var wiredMs = -1
        var usbMs = -1
        var btMs = -1
        val summary = StringBuilder()

        var proc: Process? = null
        try {
            proc = Runtime.getRuntime().exec(arrayOf("dumpsys", "media.audio_flinger"))
            BufferedReader(InputStreamReader(proc.inputStream)).use { reader ->
                var inThread = false
                var devicesText = ""
                var threadLatency = -1

                fun commitSection() {
                    if (threadLatency < 0 || devicesText.isBlank()) return
                    val upper = devicesText.uppercase()
                    if (upper.contains("BLUETOOTH_A2DP") || upper.contains("BLE_HEADSET") || upper.contains("BLE_SPEAKER")) {
                        btMs = maxOf(btMs, threadLatency)
                        summary.append("BT: ${threadLatency}ms | ")
                    }
                    if (upper.contains("WIRED_HEADPHONE") || upper.contains("WIRED_HEADSET") || upper.contains("LINE")) {
                        wiredMs = if (wiredMs < 0) threadLatency else minOf(wiredMs, threadLatency)
                        summary.append("Wired: ${threadLatency}ms | ")
                    }
                    if (upper.contains("USB_HEADSET") || upper.contains("USB_DEVICE")) {
                        usbMs = if (usbMs < 0) threadLatency else minOf(usbMs, threadLatency)
                        summary.append("USB: ${threadLatency}ms | ")
                    }
                }

                var line = reader.readLine()
                while (line != null) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("Output thread ")) {
                        commitSection()
                        inThread = true
                        devicesText = ""
                        threadLatency = -1
                    } else if (inThread) {
                        if (trimmed.contains("Output device:") || trimmed.contains("AUDIO_DEVICE_OUT_")) {
                            devicesText += " $trimmed"
                        }
                        val parsed = parseLatencyMs(trimmed)
                        if (parsed >= 0) {
                            threadLatency = parsed
                        }
                    }
                    line = reader.readLine()
                }
                commitSection()
            }
        } catch (t: Throwable) {
            summary.append("inspection error: ${t.message}")
        } finally {
            runCatching { proc?.destroy() }
        }

        return buildString {
            appendLine("wiredMs=$wiredMs")
            appendLine("usbMs=$usbMs")
            appendLine("bluetoothMs=$btMs")
            appendLine("streamMusicMs=$musicLatency")
            append("details=${summary.toString().trim()}")
        }
    }

    fun buildDiagnosticSummary(): String {
        val sb = StringBuilder()
        sb.appendLine("Strategy ID: $mediaStrategyId")
        val latency = queryGlobalStreamLatency()
        if (latency >= 0) {
            sb.appendLine("Stream Latency: $latency ms")
        }
        sb.appendLine("Outputs:")
        val outputs = listSystemOutputDevices()
        if (outputs.isEmpty()) {
            sb.appendLine("  (none)")
        } else {
            outputs.forEach { info ->
                sb.append("  [").append(typeLabel(info.type)).append("] ")
                    .append(info.productName)
                val addr = extractAddress(info)
                if (addr.isNotEmpty()) sb.append(" (").append(addr).append(')')
                sb.appendLine()
            }
        }
        sb.appendLine("Active Preferred Count: ${queryPreferredEndpoints().size}")
        sb.append("Volume Engine: $lastVolumeDiagnostic")
        return sb.toString()
    }

    private fun pushPreferredEndpoints(attributes: List<Any>): Int {
        return try {
            invokeAudioService(
                listOf("setPreferredDevicesForStrategy"),
                mediaStrategyId,
                attributes
            ) as Int
        } catch (_: NoSuchMethodException) {
            invokeAudioService(
                listOf("setDevicesRoleForStrategy"),
                mediaStrategyId,
                ROLE_PREFERRED,
                attributes
            ) as Int
        }
    }

    private fun trySetVolumeViaAudioService(category: Int, index: Int, maxIndex: Int): String? {
        return try {
            val deviceAttr = buildCategoryAttributes(category)
            val volInfo = buildVolumeInfoObject(index, maxIndex, includeIndices = true)
            invokeAudioService(listOf("setDeviceVolume"), volInfo, deviceAttr, SHELL_PACKAGE)
            null
        } catch (t: Throwable) {
            var root: Throwable = t
            while (root.cause != null) root = root.cause!!
            "${root.javaClass.simpleName}: ${root.message}"
        }
    }

    private fun tryGetVolumeViaAudioService(category: Int): Int {
        return runCatching {
            val deviceAttr = buildCategoryAttributes(category)
            val queryVol = buildVolumeInfoObject(-1, -1, includeIndices = false)
            val res = invokeAudioService(listOf("getDeviceVolume"), queryVol, deviceAttr, SHELL_PACKAGE)
            val idx = res?.javaClass?.getMethod("getVolumeIndex")?.invoke(res) as? Int
            idx?.takeIf { it >= 0 } ?: -1
        }.getOrDefault(-1)
    }

    private fun trySetBluetoothAvrcpVolume(index: Int): Boolean {
        return runCatching {
            invokeAudioService(listOf("setAvrcpAbsoluteVolumeIndex"), index)
            true
        }.getOrDefault(false)
    }

    private fun buildCategoryAttributes(category: Int): Any {
        val connected = when (category) {
            VOL_CAT_WIRED_35MM -> locateFirstEndpoint(WIRED_35MM_TYPES)
            VOL_CAT_USB -> locateFirstEndpoint(USB_TYPES)
            else -> locateFirstEndpoint(BLUETOOTH_TYPES)
        }
        if (connected != null) return createDeviceAttributes(connected)

        val fallbackType = when (category) {
            VOL_CAT_WIRED_35MM -> TYPE_WIRED_HEADPHONES
            VOL_CAT_USB -> TYPE_USB_HEADSET
            else -> TYPE_BLUETOOTH_A2DP
        }
        val attrCls = Class.forName("android.media.AudioDeviceAttributes")
        return attrCls.getConstructor(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java)
            .newInstance(ROLE_OUTPUT, fallbackType, "")
    }

    private fun buildVolumeInfoObject(index: Int, maxIndex: Int, includeIndices: Boolean): Any {
        val builderCls = Class.forName("android.media.VolumeInfo\$Builder")
        val builder = builderCls.getConstructor(Int::class.javaPrimitiveType)
            .newInstance(AudioManager.STREAM_MUSIC)
        if (includeIndices) {
            builderCls.getMethod("setMuted", Boolean::class.javaPrimitiveType).invoke(builder, index == 0)
            builderCls.getMethod("setMinVolumeIndex", Int::class.javaPrimitiveType).invoke(builder, 0)
            builderCls.getMethod("setMaxVolumeIndex", Int::class.javaPrimitiveType).invoke(builder, maxIndex)
            builderCls.getMethod("setVolumeIndex", Int::class.javaPrimitiveType).invoke(builder, index)
        }
        return builderCls.getMethod("build").invoke(builder)!!
    }

    private fun internalDeviceMasksFor(category: Int): IntArray {
        return runCatching {
            when (category) {
                VOL_CAT_WIRED_35MM -> {
                    val found = locateFirstEndpoint(WIRED_35MM_TYPES)?.let { publicTypeToBitmask(it.type) } ?: 0
                    if (found != 0) intArrayOf(found, MASK_WIRED_HEADPHONE, MASK_WIRED_HEADSET)
                    else intArrayOf(MASK_WIRED_HEADPHONE, MASK_WIRED_HEADSET, MASK_LINE, MASK_AUX_LINE)
                }
                VOL_CAT_USB -> {
                    val found = locateFirstEndpoint(USB_TYPES)?.let { publicTypeToBitmask(it.type) } ?: 0
                    if (found != 0) intArrayOf(found, MASK_USB_HEADSET, MASK_USB_DEVICE)
                    else intArrayOf(MASK_USB_HEADSET, MASK_USB_DEVICE)
                }
                else -> {
                    val found = locateFirstEndpoint(BLUETOOTH_TYPES)?.let { publicTypeToBitmask(it.type) } ?: 0
                    if (found != 0) intArrayOf(found, MASK_BT_A2DP, MASK_BT_A2DP_HEADPHONES)
                    else intArrayOf(MASK_BT_A2DP, MASK_BT_A2DP_HEADPHONES, MASK_BT_A2DP_SPEAKER, MASK_BLE_HEADSET)
                }
            }
        }.getOrElse {
            if (category == VOL_CAT_BLUETOOTH) intArrayOf(MASK_BT_A2DP)
            else intArrayOf(MASK_WIRED_HEADPHONE, MASK_WIRED_HEADSET)
        }
    }

    private fun locatePrimaryEndpoint(mode: Int): AudioDeviceInfo? = when (mode) {
        MODE_WIRED_BLUETOOTH -> locateFirstEndpoint(WIRED_35MM_TYPES)
        MODE_USB_BLUETOOTH -> locateFirstEndpoint(USB_TYPES)
        else -> locateFirstEndpoint(WIRED_35MM_TYPES) ?: locateFirstEndpoint(USB_TYPES)
    }

    private fun locateFirstEndpoint(types: IntArray): AudioDeviceInfo? {
        val outputs = listSystemOutputDevices()
        for (wanted in types) {
            outputs.firstOrNull { it.type == wanted }?.let { return it }
        }
        return null
    }

    private fun locateDeviceByTypeAndAddress(type: Int, address: String): AudioDeviceInfo? {
        var fallback: AudioDeviceInfo? = null
        for (info in listSystemOutputDevices()) {
            if (info.type == type) {
                if (address.isEmpty() || address == extractAddress(info)) return info
                if (fallback == null) fallback = info
            }
        }
        return fallback
    }

    private fun listSystemOutputDevices(): Array<AudioDeviceInfo> {
        val method = Class.forName("android.media.AudioManager")
            .getDeclaredMethod("getDevicesStatic", Int::class.javaPrimitiveType)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return (method.invoke(null, FLAG_OUTPUT_DEVICES) as? Array<AudioDeviceInfo>) ?: emptyArray()
    }

    private fun createDeviceAttributes(info: AudioDeviceInfo): Any {
        val attrCls = Class.forName("android.media.AudioDeviceAttributes")
        return try {
            attrCls.getConstructor(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java)
                .newInstance(ROLE_OUTPUT, info.type, extractAddress(info))
        } catch (_: NoSuchMethodException) {
            attrCls.getConstructor(AudioDeviceInfo::class.java).newInstance(info)
        }
    }

    private fun readAttributeDeviceType(attr: Any): Int =
        attr.javaClass.getMethod("getType").invoke(attr) as Int

    private fun extractAddress(info: AudioDeviceInfo): String =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.address.orEmpty() else ""
        }.getOrDefault("")

    private fun invokeAudioService(methodNames: List<String>, vararg args: Any?): Any? {
        for (name in methodNames) {
            val match = audioServiceBinder.javaClass.methods.firstOrNull {
                it.name == name && it.parameterCount == args.size
            }
            if (match != null) {
                return match.invoke(audioServiceBinder, *args)
            }
        }
        throw NoSuchMethodException("No matching method in $methodNames (${args.size} params)")
    }

    private fun resolveMediaProductStrategyId(): Int {
        val mediaAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        val providers = listOf(
            "android.media.audiopolicy.AudioProductStrategy",
            "android.media.AudioManager"
        )
        for (clsName in providers) {
            runCatching {
                val list = Class.forName(clsName)
                    .getMethod("getAudioProductStrategies")
                    .invoke(null) as? List<*> ?: return@runCatching
                for (strategy in list) {
                    if (strategy == null) continue
                    val supported = strategy.javaClass
                        .getMethod("supportsAudioAttributes", AudioAttributes::class.java)
                        .invoke(strategy, mediaAttrs) as? Boolean ?: false
                    if (supported) {
                        return strategy.javaClass.getMethod("getId").invoke(strategy) as Int
                    }
                }
            }
        }
        return DEFAULT_MEDIA_STRATEGY
    }

    private fun queryGlobalStreamLatency(): Int = runCatching {
        val m = Class.forName("android.media.AudioSystem")
            .getMethod("getOutputLatency", Int::class.javaPrimitiveType)
        (m.invoke(null, AudioManager.STREAM_MUSIC) as? Int) ?: -1
    }.getOrDefault(-1)

    private fun missingPrimaryError(mode: Int): String = when (mode) {
        MODE_WIRED_BLUETOOTH -> "NO_WIRED_DEVICE"
        MODE_USB_BLUETOOTH -> "NO_USB_DEVICE"
        else -> "NO_WIRED_OR_USB_DEVICE"
    }

    companion object {
        const val MODE_WIRED_BLUETOOTH = 0
        const val MODE_USB_BLUETOOTH = 1
        const val MODE_AUTO_BLUETOOTH = 2
        const val MODE_CUSTOM = 3

        const val VOL_CAT_WIRED_35MM = 0
        const val VOL_CAT_USB = 1
        const val VOL_CAT_BLUETOOTH = 2

        const val TYPE_WIRED_HEADSET = AudioDeviceInfo.TYPE_WIRED_HEADSET
        const val TYPE_WIRED_HEADPHONES = AudioDeviceInfo.TYPE_WIRED_HEADPHONES
        const val TYPE_LINE_ANALOG = AudioDeviceInfo.TYPE_LINE_ANALOG
        const val TYPE_BLUETOOTH_A2DP = AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
        const val TYPE_USB_DEVICE = AudioDeviceInfo.TYPE_USB_DEVICE
        const val TYPE_AUX_LINE = AudioDeviceInfo.TYPE_AUX_LINE
        const val TYPE_USB_HEADSET = 22
        const val TYPE_BLE_HEADSET = 26
        const val TYPE_BLE_SPEAKER = 27

        private const val SHELL_PACKAGE = "com.android.shell"
        private const val ROLE_OUTPUT = 2
        private const val ROLE_PREFERRED = 1
        private const val FLAG_OUTPUT_DEVICES = 2
        private const val DEFAULT_MEDIA_STRATEGY = 0

        private const val MASK_WIRED_HEADSET = 0x4
        private const val MASK_WIRED_HEADPHONE = 0x8
        private const val MASK_BT_A2DP = 0x80
        private const val MASK_BT_A2DP_HEADPHONES = 0x100
        private const val MASK_BT_A2DP_SPEAKER = 0x200
        private const val MASK_USB_DEVICE = 0x4000
        private const val MASK_LINE = 0x20000
        private const val MASK_AUX_LINE = 0x200000
        private const val MASK_USB_HEADSET = 0x4000000
        private const val MASK_BLE_HEADSET = 0x20000000
        private const val MASK_BLE_SPEAKER = 0x20000001

        private val WIRED_35MM_TYPES = intArrayOf(
            TYPE_WIRED_HEADSET,
            TYPE_WIRED_HEADPHONES,
            TYPE_LINE_ANALOG,
            TYPE_AUX_LINE
        )
        private val USB_TYPES = intArrayOf(TYPE_USB_HEADSET, TYPE_USB_DEVICE)
        private val BLUETOOTH_TYPES = intArrayOf(TYPE_BLUETOOTH_A2DP, TYPE_BLE_HEADSET, TYPE_BLE_SPEAKER)

        private val PRIMARY_LATENCY_REGEX =
            Pattern.compile("(?i)(?:HAL\\s+)?latency\\s*\\(ms\\)\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)")
        private val ALT_LATENCY_REGEX =
            Pattern.compile("(?i)latency\\s*[:=]\\s*([0-9]+(?:\\.[0-9]+)?)\\s*ms")

        @JvmStatic
        fun parseLatencyMs(line: String?): Int {
            if (line.isNullOrBlank()) return -1
            val m1 = PRIMARY_LATENCY_REGEX.matcher(line)
            if (m1.find()) {
                return runCatching { m1.group(1)!!.toFloat().roundToInt() }.getOrDefault(-1)
            }
            val m2 = ALT_LATENCY_REGEX.matcher(line)
            if (m2.find()) {
                return runCatching { m2.group(1)!!.toFloat().roundToInt() }.getOrDefault(-1)
            }
            return -1
        }

        @JvmStatic
        fun typeLabel(type: Int): String = when (type) {
            TYPE_WIRED_HEADSET -> "3.5mm Headset"
            TYPE_WIRED_HEADPHONES -> "3.5mm Headphones"
            TYPE_LINE_ANALOG -> "Analog Line"
            TYPE_AUX_LINE -> "AUX Out"
            TYPE_USB_HEADSET -> "USB Headset"
            TYPE_USB_DEVICE -> "USB Audio"
            TYPE_BLUETOOTH_A2DP -> "Bluetooth Audio"
            TYPE_BLE_HEADSET -> "Bluetooth LE"
            TYPE_BLE_SPEAKER -> "Bluetooth Speaker"
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Phone Speaker"
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Earpiece"
            else -> "Audio Out"
        }

        private fun publicTypeToBitmask(type: Int): Int = when (type) {
            TYPE_WIRED_HEADSET -> MASK_WIRED_HEADSET
            TYPE_WIRED_HEADPHONES -> MASK_WIRED_HEADPHONE
            TYPE_LINE_ANALOG -> MASK_LINE
            TYPE_AUX_LINE -> MASK_AUX_LINE
            TYPE_USB_HEADSET -> MASK_USB_HEADSET
            TYPE_USB_DEVICE -> MASK_USB_DEVICE
            TYPE_BLUETOOTH_A2DP -> MASK_BT_A2DP
            TYPE_BLE_HEADSET -> MASK_BLE_HEADSET
            TYPE_BLE_SPEAKER -> MASK_BLE_SPEAKER
            else -> 0
        }

        private fun categoryLabel(category: Int): String = when (category) {
            VOL_CAT_WIRED_35MM -> "WIRED"
            VOL_CAT_USB -> "USB"
            else -> "BT"
        }

        private fun lookupStaticMethod(cls: Class<*>, name: String, vararg params: Class<*>): Method? =
            runCatching {
                cls.getDeclaredMethod(name, *params).apply { isAccessible = true }
            }.getOrNull()
    }
}
