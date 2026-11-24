package com.dualaudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dualaudio.app.audio.DualAudioUiState
import com.dualaudio.app.audio.OutputCategory
import com.dualaudio.app.audio.RoutingMode
import com.dualaudio.app.audio.ShizukuStatus
import com.dualaudio.app.ui.UiStrings
import com.dualaudio.app.ui.theme.JetBrainsMonoFamily
import com.dualaudio.app.ui.theme.SignalAmber
import com.dualaudio.app.ui.theme.SignalCyan
import com.dualaudio.app.ui.theme.SignalEmerald
import kotlin.math.roundToInt

/**
 * Strictly independent per-headphone volume mixer:
 *  - Separate framed strip for Channel A: 3.5mm Wired / USB (slider, -/+ step buttons, mute)
 *  - Separate framed strip for Channel B: Bluetooth A2DP / LE (slider, -/+ step buttons, mute)
 * Adjusting one strip never modifies the other headphone's volume.
 */
@Composable
fun DualVolumeMixerCard(
    state: DualAudioUiState,
    strings: UiStrings,
    onWiredVolumeChange: (Float) -> Unit,
    onStepWiredVolume: (Int) -> Unit,
    onBluetoothVolumeChange: (Float) -> Unit,
    onStepBluetoothVolume: (Int) -> Unit,
    onToggleWiredMute: () -> Unit,
    onToggleBluetoothMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wiredAccent = if (state.selectedMode == RoutingMode.USB_BLUETOOTH ||
        state.primarySlotDevice?.category == OutputCategory.USB_AUDIO
    ) {
        SignalAmber
    } else {
        SignalEmerald
    }
    val wiredIcon = if (wiredAccent == SignalAmber) Icons.Rounded.Usb else Icons.Rounded.Headphones
    val wiredTitle = state.primarySlotDevice?.name ?: strings.volumeWiredChannelLabel
    val btTitle = state.secondarySlotDevice?.name ?: strings.volumeBluetoothChannelLabel

    val maxSteps = state.maxMediaVolumeSteps.coerceAtLeast(1)
    val wiredStep = if (state.wiredMuted) 0 else (state.wiredVolume * maxSteps).roundToInt().coerceIn(0, maxSteps)
    val btStep = if (state.bluetoothMuted) 0 else (state.bluetoothVolume * maxSteps).roundToInt().coerceIn(0, maxSteps)
    val wiredPct = if (state.wiredMuted) 0 else (wiredStep * 100f / maxSteps).roundToInt()
    val btPct = if (state.bluetoothMuted) 0 else (btStep * 100f / maxSteps).roundToInt()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dual_volume_mixer_card"),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Equalizer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = strings.volumeMixerTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = strings.volumeIndependentBadge,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = strings.volumeMixerSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Separate Channel A Card: Wired 3.5mm / USB
            IndependentChannelVolumeCard(
                title = wiredTitle,
                subtitle = strings.volumeWiredChannelLabel,
                currentStep = wiredStep,
                maxSteps = maxSteps,
                percentage = wiredPct,
                sliderValue = if (state.wiredMuted) 0f else state.wiredVolume,
                isMuted = state.wiredMuted,
                accent = wiredAccent,
                deviceIcon = wiredIcon,
                stepDownCd = strings.volumeStepDownCd,
                stepUpCd = strings.volumeStepUpCd,
                muteCd = strings.volumeMuteCd,
                cardTag = "wired_volume_channel_card",
                sliderTag = "wired_volume_slider",
                stepDownTag = "wired_volume_minus_button",
                stepUpTag = "wired_volume_plus_button",
                muteTag = "wired_mute_button",
                onVolumeChange = onWiredVolumeChange,
                onStepDown = { onStepWiredVolume(-1) },
                onStepUp = { onStepWiredVolume(+1) },
                onToggleMute = onToggleWiredMute
            )

            // Separate Channel B Card: Bluetooth A2DP / LE
            IndependentChannelVolumeCard(
                title = btTitle,
                subtitle = strings.volumeBluetoothChannelLabel,
                currentStep = btStep,
                maxSteps = maxSteps,
                percentage = btPct,
                sliderValue = if (state.bluetoothMuted) 0f else state.bluetoothVolume,
                isMuted = state.bluetoothMuted,
                accent = SignalCyan,
                deviceIcon = Icons.Rounded.BluetoothAudio,
                stepDownCd = strings.volumeStepDownCd,
                stepUpCd = strings.volumeStepUpCd,
                muteCd = strings.volumeMuteCd,
                cardTag = "bluetooth_volume_channel_card",
                sliderTag = "bluetooth_volume_slider",
                stepDownTag = "bluetooth_volume_minus_button",
                stepUpTag = "bluetooth_volume_plus_button",
                muteTag = "bluetooth_mute_button",
                onVolumeChange = onBluetoothVolumeChange,
                onStepDown = { onStepBluetoothVolume(-1) },
                onStepUp = { onStepBluetoothVolume(+1) },
                onToggleMute = onToggleBluetoothMute
            )

            if (state.shizukuStatus != ShizukuStatus.READY) {
                Text(
                    text = strings.volumeShizukuRequiredHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun IndependentChannelVolumeCard(
    title: String,
    subtitle: String,
    currentStep: Int,
    maxSteps: Int,
    percentage: Int,
    sliderValue: Float,
    isMuted: Boolean,
    accent: Color,
    deviceIcon: ImageVector,
    stepDownCd: String,
    stepUpCd: String,
    muteCd: String,
    cardTag: String,
    sliderTag: String,
    stepDownTag: String,
    stepUpTag: String,
    muteTag: String,
    onVolumeChange: (Float) -> Unit,
    onStepDown: () -> Unit,
    onStepUp: () -> Unit,
    onToggleMute: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isMuted) MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
            else accent.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(cardTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = if (isMuted) 0.08f else 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = deviceIcon,
                        contentDescription = subtitle,
                        tint = if (isMuted) MaterialTheme.colorScheme.onSurfaceVariant else accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isMuted) MaterialTheme.colorScheme.errorContainer
                    else accent.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "$currentStep/$maxSteps • $percentage%",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isMuted) MaterialTheme.colorScheme.onErrorContainer else accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(muteTag)
                ) {
                    Icon(
                        imageVector = if (isMuted || currentStep == 0) {
                            Icons.Rounded.VolumeOff
                        } else {
                            Icons.Rounded.VolumeUp
                        },
                        contentDescription = muteCd,
                        tint = if (isMuted || currentStep == 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            accent
                        }
                    )
                }
            }

            // Dedicated - / Slider / + control strip for this headphone only
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onStepDown,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(stepDownTag)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = stepDownCd,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Slider(
                    value = sliderValue.coerceIn(0f, 1f),
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    steps = (maxSteps - 1).coerceAtLeast(0),
                    colors = SliderDefaults.colors(
                        thumbColor = if (isMuted) MaterialTheme.colorScheme.outline else accent,
                        activeTrackColor = if (isMuted) MaterialTheme.colorScheme.outline else accent,
                        inactiveTrackColor = accent.copy(alpha = 0.22f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(sliderTag)
                )

                FilledTonalIconButton(
                    onClick = onStepUp,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(stepUpTag)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stepUpCd,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
