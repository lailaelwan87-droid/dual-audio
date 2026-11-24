package com.dualaudio.app.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.dualaudio.app.routing.DualOutputEngine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Observes real-time connected audio output devices using public Android AudioManager APIs.
 * Works immediately without root or Shizuku so the user can see their 3.5mm wired headphones,
 * USB audio, and Bluetooth headphones live as they plug/unplug or pair them.
 */
class LocalAudioDeviceMonitor(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun observeOutputDevices(): Flow<List<ConnectedAudioDevice>> = callbackFlow {
        val emitCurrent = {
            trySend(snapshotOutputDevices())
        }

        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                emitCurrent()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                emitCurrent()
            }
        }

        emitCurrent()
        audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))

        awaitClose {
            audioManager.unregisterAudioDeviceCallback(callback)
        }
    }

    fun snapshotOutputDevices(): List<ConnectedAudioDevice> {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        return devices
            .filter { info ->
                // Filter out internal telephony/sink-only entries that cannot play media
                info.type != AudioDeviceInfo.TYPE_TELEPHONY &&
                    info.type != AudioDeviceInfo.TYPE_BUILTIN_EARPIECE &&
                    info.type != AudioDeviceInfo.TYPE_UNKNOWN
            }
            .map { info ->
                val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    runCatching { info.address }.getOrNull().orEmpty()
                } else {
                    ""
                }
                val rawName = info.productName?.toString()?.trim().orEmpty()
                val category = classifyType(info.type)
                ConnectedAudioDevice(
                    id = info.id,
                    type = info.type,
                    name = rawName.ifEmpty { fallbackName(info.type) },
                    address = address,
                    category = category,
                    typeLabel = DualOutputEngine.typeLabel(info.type)
                )
            }
            .sortedBy { categorySortRank(it.category) }
    }

    private fun classifyType(type: Int): OutputCategory = when (type) {
        DualOutputEngine.TYPE_WIRED_HEADSET,
        DualOutputEngine.TYPE_WIRED_HEADPHONES,
        DualOutputEngine.TYPE_LINE_ANALOG,
        DualOutputEngine.TYPE_AUX_LINE -> OutputCategory.WIRED_35MM

        DualOutputEngine.TYPE_USB_HEADSET,
        DualOutputEngine.TYPE_USB_DEVICE -> OutputCategory.USB_AUDIO

        DualOutputEngine.TYPE_BLUETOOTH_A2DP,
        DualOutputEngine.TYPE_BLE_HEADSET,
        DualOutputEngine.TYPE_BLE_SPEAKER -> OutputCategory.BLUETOOTH

        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> OutputCategory.BUILTIN_SPEAKER
        else -> OutputCategory.OTHER
    }

    private fun categorySortRank(category: OutputCategory): Int = when (category) {
        OutputCategory.WIRED_35MM -> 0
        OutputCategory.BLUETOOTH -> 1
        OutputCategory.USB_AUDIO -> 2
        OutputCategory.BUILTIN_SPEAKER -> 3
        OutputCategory.OTHER -> 4
    }

    private fun fallbackName(type: Int): String = when (type) {
        DualOutputEngine.TYPE_WIRED_HEADSET -> "3.5mm Wired Headset"
        DualOutputEngine.TYPE_WIRED_HEADPHONES -> "3.5mm Wired Headphones"
        DualOutputEngine.TYPE_LINE_ANALOG -> "3.5mm Analog Line Out"
        DualOutputEngine.TYPE_AUX_LINE -> "AUX Line Out"
        DualOutputEngine.TYPE_USB_HEADSET -> "USB Headset"
        DualOutputEngine.TYPE_USB_DEVICE -> "USB Audio DAC"
        DualOutputEngine.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
        DualOutputEngine.TYPE_BLE_HEADSET -> "Bluetooth LE Audio"
        DualOutputEngine.TYPE_BLE_SPEAKER -> "Bluetooth LE Speaker"
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Phone Speaker"
        else -> "Audio Output"
    }
}
