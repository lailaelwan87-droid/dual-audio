package com.dualaudio.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates a gentle stereo acoustic chime on USAGE_MEDIA so the user can immediately
 * verify that audio is playing through both the 3.5mm wired headphone and Bluetooth headset.
 */
class AudioTestTonePlayer {

    suspend fun playStereoChime() = withContext(Dispatchers.IO) {
        val sampleRate = 48_000
        val durationSec = 2.4
        val totalSamples = (sampleRate * durationSec).toInt()
        val pcm = ShortArray(totalSamples * 2) // Stereo L/R interleaved

        // Warm harmonic notes: C5 -> E5 -> G5 (left, right, both)
        val segLen = totalSamples / 3
        val freqs = doubleArrayOf(523.25, 659.25, 783.99)

        for (i in 0 until totalSamples) {
            val seg = (i / segLen).coerceIn(0, 2)
            val localPos = (i - seg * segLen).toDouble() / segLen
            val env = sin(PI * localPos).coerceAtLeast(0.0)
            val t = i.toDouble() / sampleRate
            val tone = (0.75 * sin(2.0 * PI * freqs[seg] * t) +
                0.25 * sin(2.0 * PI * freqs[seg] * 2.0 * t)) * env * 0.45

            val sample = (tone * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            // Segment 0: Left emphasis, Segment 1: Right emphasis, Segment 2: Full stereo
            pcm[i * 2] = if (seg == 1) (sample / 4).toShort() else sample
            pcm[i * 2 + 1] = if (seg == 0) (sample / 4).toShort() else sample
        }

        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBuf, pcm.size * 2))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(pcm, 0, pcm.size)
            track.play()
            val start = System.currentTimeMillis()
            while (isActive && System.currentTimeMillis() - start < 2450L) {
                Thread.sleep(60L)
            }
        } finally {
            runCatching { track.stop() }
            runCatching { track.release() }
        }
    }
}
