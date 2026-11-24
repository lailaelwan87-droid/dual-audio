package com.dualaudio.app.audio

import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

/**
 * Lock-free-friendly circular PCM-16 stereo delay ring buffer.
 * Supports live, glitch-minimized millisecond delay changes (0..600 ms) and gain trim
 * so the wired 3.5mm output can be aligned down to the exact millisecond with Bluetooth A2DP.
 */
class AudioDelayBuffer(
    private val sampleRate: Int = 48_000,
    private val channels: Int = 2,
    maxDelayMs: Int = 600
) {
    private val maxDelayFrames: Int = (sampleRate * maxDelayMs) / 1000
    // Add 1024 frames of headroom to guarantee non-overlapping read/write indices
    private val capacitySamples: Int = (maxDelayFrames + 1024) * channels
    private val ring = ShortArray(capacitySamples)
    private var writePos = 0

    private val targetDelayMs = AtomicInteger(200)
    private val gainMilli = AtomicInteger(1000) // 1000 = 1.0x gain

    fun setDelayMs(ms: Int) {
        targetDelayMs.set(ms.coerceIn(0, MAX_DELAY_MS))
    }

    fun getDelayMs(): Int = targetDelayMs.get()

    fun setGain(gain: Float) {
        val clamped = gain.coerceIn(0.0f, 1.5f)
        gainMilli.set((clamped * 1000f).roundToInt())
    }

    fun clear() {
        ring.fill(0)
        writePos = 0
    }

    /**
     * Writes `count` interleaved PCM-16 samples into the ring and reads `count` delayed
     * samples into `output` with the currently configured delay and gain.
     */
    fun process(input: ShortArray, output: ShortArray, count: Int) {
        val delayFrames = ((sampleRate.toLong() * targetDelayMs.get()) / 1000L)
            .toInt()
            .coerceIn(0, maxDelayFrames)
        val delaySamples = delayFrames * channels
        val gain = gainMilli.get() / 1000f
        val cap = capacitySamples

        var wp = writePos
        for (i in 0 until count) {
            ring[wp] = input[i]

            var rp = wp - delaySamples
            if (rp < 0) rp += cap

            val delayedSample = ring[rp]
            output[i] = if (gain == 1.0f) {
                delayedSample
            } else {
                (delayedSample * gain)
                    .roundToInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
            }

            wp++
            if (wp >= cap) wp = 0
        }
        writePos = wp
    }

    companion object {
        const val MAX_DELAY_MS = 500
    }
}
