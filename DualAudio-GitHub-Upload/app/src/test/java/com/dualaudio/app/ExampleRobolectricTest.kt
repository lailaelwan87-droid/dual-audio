package com.dualaudio.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dualaudio.app.audio.AudioDelayBuffer
import com.dualaudio.app.audio.DualAudioUiState
import com.dualaudio.app.audio.LatencyMeasurement
import com.dualaudio.app.audio.RoutingMode
import com.dualaudio.app.routing.DualOutputEngine
import com.dualaudio.app.ui.DualAudioViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

    @Test
    fun appNameMatchesDualAudio() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Dual Audio", appName)
    }

    @Test
    fun packageNameAndLegalAssetsAreConfiguredCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.dualaudio.app", BuildConfig.APPLICATION_ID)
        assertEquals("com.dualaudio.app", context.packageName)

        val privacyPolicy = context.assets.open("legal/PRIVACY_POLICY.md").bufferedReader().use { it.readText() }
        val gplLicense = context.assets.open("legal/LICENSE").bufferedReader().use { it.readText() }
        val notices = context.assets.open("legal/THIRD_PARTY_NOTICES.md").bufferedReader().use { it.readText() }

        assertTrue(privacyPolicy.contains("com.dualaudio.app"))
        assertTrue(gplLicense.contains("GNU GENERAL PUBLIC LICENSE"))
        assertTrue(notices.contains("Copyright (C) 2026 xTudoS"))
    }

    @Test
    fun dualOutputEngineLabelsCoverWired35mmUsbAndBluetooth() {
        assertEquals("3.5mm Headset", DualOutputEngine.typeLabel(DualOutputEngine.TYPE_WIRED_HEADSET))
        assertEquals("3.5mm Headphones", DualOutputEngine.typeLabel(DualOutputEngine.TYPE_WIRED_HEADPHONES))
        assertEquals("Analog Line", DualOutputEngine.typeLabel(DualOutputEngine.TYPE_LINE_ANALOG))
        assertEquals("Bluetooth Audio", DualOutputEngine.typeLabel(DualOutputEngine.TYPE_BLUETOOTH_A2DP))
        assertEquals("Bluetooth LE", DualOutputEngine.typeLabel(DualOutputEngine.TYPE_BLE_HEADSET))
        assertEquals("USB Headset", DualOutputEngine.typeLabel(DualOutputEngine.TYPE_USB_HEADSET))
    }

    @Test
    fun routingModeCodesRoundTripCleanly() {
        assertEquals(RoutingMode.WIRED_BLUETOOTH, RoutingMode.fromCode(DualOutputEngine.MODE_WIRED_BLUETOOTH))
        assertEquals(RoutingMode.USB_BLUETOOTH, RoutingMode.fromCode(DualOutputEngine.MODE_USB_BLUETOOTH))
        assertEquals(RoutingMode.AUTO_BLUETOOTH, RoutingMode.fromCode(DualOutputEngine.MODE_AUTO_BLUETOOTH))
        assertEquals(RoutingMode.CUSTOM, RoutingMode.fromCode(DualOutputEngine.MODE_CUSTOM))
    }

    @Test
    fun audioDelayBufferShiftsSignalByExactMilliseconds() {
        // 1000 Hz stereo -> 1 ms = 1 frame = 2 samples
        val buffer = AudioDelayBuffer(sampleRate = 1000, channels = 2, maxDelayMs = 100)
        buffer.setDelayMs(10) // 10 frames = 20 samples delay

        val input = ShortArray(40) // 20 stereo frames
        input[0] = 1234
        input[1] = -1234

        val output = ShortArray(40)
        buffer.process(input, output, input.size)

        // First 10 frames (20 samples) are silence; impulse arrives at sample index 20 & 21
        assertEquals(0.toShort(), output[0])
        assertEquals(0.toShort(), output[19])
        assertEquals(1234.toShort(), output[20])
        assertEquals((-1234).toShort(), output[21])
    }

    @Test
    fun parseLatencyMsExtractsAudioFlingerLines() {
        assertEquals(224, DualOutputEngine.parseLatencyMs("  HAL latency (ms): 224.2"))
        assertEquals(28, DualOutputEngine.parseLatencyMs("  Latency (ms): 28"))
        assertEquals(195, DualOutputEngine.parseLatencyMs("  latency=195ms"))
        assertEquals(-1, DualOutputEngine.parseLatencyMs("  Sample rate: 48000 Hz"))
    }

    @Test
    fun independentVolumeCategoriesAreDistinct() {
        val categories = setOf(
            DualOutputEngine.VOL_CAT_WIRED_35MM,
            DualOutputEngine.VOL_CAT_USB,
            DualOutputEngine.VOL_CAT_BLUETOOTH
        )
        assertEquals(3, categories.size)
    }

    @Test
    fun autoSyncLatencyEnabledByDefaultAndBoundedUnderOneSecond() {
        val state = DualAudioUiState()
        assertTrue(state.autoSyncLatency)
        assertTrue(DualAudioViewModel.SUB_SECOND_PROBE_TIMEOUT_MS < 1000L)

        // Verify delta calculation clamps cleanly so delay never exceeds 500ms or drops below 0ms
        val typical = LatencyMeasurement(wiredMs = 24, bluetoothMs = 214)
        assertEquals(190, typical.estimatedDeltaMs)

        val extreme = LatencyMeasurement(wiredMs = 20, bluetoothMs = 950)
        assertEquals(AudioDelayBuffer.MAX_DELAY_MS, extreme.estimatedDeltaMs)
    }
}
