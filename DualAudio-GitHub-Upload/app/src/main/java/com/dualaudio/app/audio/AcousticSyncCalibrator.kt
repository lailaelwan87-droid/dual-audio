package com.dualaudio.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTimestamp
import android.media.AudioTrack
import android.os.Build
import com.dualaudio.app.routing.DualOutputEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class TrackPipelineMeasurement(
    val wiredPipelineMs: Int?,
    val bluetoothPipelineMs: Int?
)

/**
 * Dual-output Acoustic Sync Calibrator:
 * Plays crisp rhythmic metronome clicks simultaneously through:
 *   - Track A -> Wired 3.5mm / USB AudioDeviceInfo (delayed live by `delayMs`)
 *   - Track B -> Bluetooth A2DP / LE AudioDeviceInfo (0 ms added delay)
 *
 * The user wears one wired earbud and one Bluetooth earbud and moves the delay slider
 * until both clicks snap into a single crisp click — revealing the exact real acoustic delay!
 */
class AcousticSyncCalibrator(private val context: Context) {

    private val delayBuffer = AudioDelayBuffer(sampleRate = SAMPLE_RATE, channels = 2, maxDelayMs = 600)
    private val liveDelayMs = AtomicInteger(200)
    private val liveWiredVolMilli = AtomicInteger(1000)
    private val liveBluetoothVolMilli = AtomicInteger(1000)

    fun updateLiveDelayMs(ms: Int) {
        val clamped = ms.coerceIn(0, AudioDelayBuffer.MAX_DELAY_MS)
        liveDelayMs.set(clamped)
        delayBuffer.setDelayMs(clamped)
    }

    fun updateLiveWiredGain(gain: Float) {
        val clamped = gain.coerceIn(0.0f, 1.5f)
        liveWiredVolMilli.set((clamped * 1000f).toInt())
        delayBuffer.setGain(clamped)
    }

    fun updateLiveBluetoothVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        liveBluetoothVolMilli.set((clamped * 1000f).toInt())
    }

    /**
     * Runs the rhythmic click loop until cancelled.
     * Invokes `onPipelineMeasured` when AudioTrack timestamps yield live pipeline latencies.
     */
    suspend fun runCalibrationLoop(
        wiredDeviceId: Int?,
        bluetoothDeviceId: Int?,
        onPipelineMeasured: (TrackPipelineMeasurement) -> Unit
    ) = withContext(Dispatchers.IO) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val allOutputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        val wiredInfo = allOutputs.firstOrNull { it.id == wiredDeviceId }
            ?: allOutputs.firstOrNull { isWiredOrUsb(it.type) }
        val btInfo = allOutputs.firstOrNull { it.id == bluetoothDeviceId }
            ?: allOutputs.firstOrNull { isBluetooth(it.type) }

        val minBufBytes = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(CHUNK_FRAMES * 4)

        val trackWired = createTrack(minBufBytes * 2, wiredInfo)
        val trackBt = if (btInfo != null && btInfo.id != wiredInfo?.id) {
            createTrack(minBufBytes * 2, btInfo)
        } else {
            null
        }

        delayBuffer.clear()
        delayBuffer.setDelayMs(liveDelayMs.get())
        delayBuffer.setGain(liveWiredVolMilli.get() / 1000f)
        trackBt?.setVolume((liveBluetoothVolMilli.get() / 1000f).coerceIn(0f, 1f))

        val dryChunk = ShortArray(CHUNK_FRAMES * 2)
        val delayedChunk = ShortArray(CHUNK_FRAMES * 2)

        var frameCursor = 0L
        var writtenFrames = 0L
        var lastMeasureFrame = 0L
        val periodFrames = (SAMPLE_RATE * CLICK_INTERVAL_MS) / 1000
        val clickFrames = (SAMPLE_RATE * CLICK_DURATION_MS) / 1000

        try {
            trackWired.play()
            trackBt?.play()

            while (isActive) {
                trackBt?.setVolume((liveBluetoothVolMilli.get() / 1000f).coerceIn(0f, 1f))
                // Synthesize crisp 18ms woodblock-style sync click every 650ms
                for (f in 0 until CHUNK_FRAMES) {
                    val posInPeriod = ((frameCursor + f) % periodFrames).toInt()
                    val sample: Short = if (posInPeriod < clickFrames) {
                        val t = posInPeriod.toDouble() / SAMPLE_RATE
                        val decay = exp(-t * 140.0)
                        val wave = (0.72 * sin(2.0 * PI * 1450.0 * t) +
                            0.28 * sin(2.0 * PI * 2900.0 * t)) * decay * 0.75
                        (wave * Short.MAX_VALUE).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                    } else {
                        0
                    }
                    dryChunk[f * 2] = sample
                    dryChunk[f * 2 + 1] = sample
                }
                frameCursor += CHUNK_FRAMES

                // Apply real-time adjustable delay and wired volume gain to the wired channel
                delayBuffer.process(dryChunk, delayedChunk, dryChunk.size)

                // Write Bluetooth immediately (0 ms added) and Wired with `delayMs`
                trackBt?.write(dryChunk, 0, dryChunk.size, AudioTrack.WRITE_NON_BLOCKING)
                trackWired.write(delayedChunk, 0, delayedChunk.size, AudioTrack.WRITE_BLOCKING)
                writtenFrames += CHUNK_FRAMES

                // Periodically probe AudioTrack hardware timestamps for live latency telemetry
                if (writtenFrames - lastMeasureFrame >= SAMPLE_RATE / 2) {
                    lastMeasureFrame = writtenFrames
                    val wiredMs = estimateTrackLatencyMs(trackWired, writtenFrames)
                    val btMs = trackBt?.let { estimateTrackLatencyMs(it, writtenFrames) }
                    if (wiredMs != null || btMs != null) {
                        onPipelineMeasured(
                            TrackPipelineMeasurement(
                                wiredPipelineMs = wiredMs,
                                bluetoothPipelineMs = btMs
                            )
                        )
                    }
                }
            }
        } finally {
            runCatching { trackWired.stop() }
            runCatching { trackWired.release() }
            runCatching { trackBt?.stop() }
            runCatching { trackBt?.release() }
        }
    }

    private fun createTrack(bufferBytes: Int, preferredDevice: AudioDeviceInfo?): AudioTrack {
        val builder = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
        }

        val track = builder.build()
        if (preferredDevice != null) {
            track.setPreferredDevice(preferredDevice)
        }
        return track
    }

    private fun estimateTrackLatencyMs(track: AudioTrack, writtenFrames: Long): Int? {
        val ts = AudioTimestamp()
        if (!track.getTimestamp(ts)) return null
        val pendingFrames = (writtenFrames - ts.framePosition).coerceAtLeast(0L)
        val latencyMs = ((pendingFrames * 1000L) / SAMPLE_RATE).toInt()
        return latencyMs.takeIf { it in 5..800 }
    }

    private fun isWiredOrUsb(type: Int): Boolean = when (type) {
        DualOutputEngine.TYPE_WIRED_HEADSET,
        DualOutputEngine.TYPE_WIRED_HEADPHONES,
        DualOutputEngine.TYPE_LINE_ANALOG,
        DualOutputEngine.TYPE_AUX_LINE,
        DualOutputEngine.TYPE_USB_HEADSET,
        DualOutputEngine.TYPE_USB_DEVICE -> true
        else -> false
    }

    private fun isBluetooth(type: Int): Boolean = when (type) {
        DualOutputEngine.TYPE_BLUETOOTH_A2DP,
        DualOutputEngine.TYPE_BLE_HEADSET,
        DualOutputEngine.TYPE_BLE_SPEAKER -> true
        else -> false
    }

    companion object {
        private const val SAMPLE_RATE = 48_000
        private const val CHUNK_FRAMES = 480 // 10 ms chunks for responsive live delay updates
        private const val CLICK_INTERVAL_MS = 650
        private const val CLICK_DURATION_MS = 18
    }
}
