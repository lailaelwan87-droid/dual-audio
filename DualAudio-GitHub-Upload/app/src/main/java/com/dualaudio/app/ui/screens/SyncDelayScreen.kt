package com.dualaudio.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dualaudio.app.audio.AudioDelayBuffer
import com.dualaudio.app.audio.DualAudioUiState
import com.dualaudio.app.ui.UiStrings
import com.dualaudio.app.ui.components.DualVolumeMixerCard
import com.dualaudio.app.ui.theme.JetBrainsMonoFamily
import com.dualaudio.app.ui.theme.SignalAmber
import com.dualaudio.app.ui.theme.SignalCyan
import com.dualaudio.app.ui.theme.SignalEmerald
import kotlin.math.roundToInt

@Composable
fun SyncDelayScreen(
    state: DualAudioUiState,
    strings: UiStrings,
    onMeasureLatency: () -> Unit,
    onInstantAutoSync: () -> Unit,
    onSetAutoSyncLatency: (Boolean) -> Unit,
    onApplyMeasuredDelta: () -> Unit,
    onSetDelayMs: (Int) -> Unit,
    onAdjustDelayBy: (Int) -> Unit,
    onToggleCalibrator: () -> Unit,
    onWiredVolumeChange: (Float) -> Unit,
    onStepWiredVolume: (Int) -> Unit,
    onBluetoothVolumeChange: (Float) -> Unit,
    onStepBluetoothVolume: (Int) -> Unit,
    onToggleWiredMute: () -> Unit,
    onToggleBluetoothMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val measurement = state.latencyMeasurement

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Sub-Second Automatic Latency Sync Card (< 1 second)
            item(key = "auto_sync_card") {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (state.autoSyncLatency) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auto_sync_latency_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = if (state.autoSyncLatency) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.autoSyncCardTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.autoSyncCardDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = state.autoSyncLatency,
                                onCheckedChange = onSetAutoSyncLatency,
                                modifier = Modifier.testTag("auto_sync_latency_switch")
                            )
                        }

                        if (state.lastAutoSyncDurationMs != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${strings.autoSyncSpeedPrefix} ${state.lastAutoSyncDurationMs} ms  •  +${state.wiredDelayMs} ms",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = JetBrainsMonoFamily,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.testTag("auto_sync_speed_badge")
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onInstantAutoSync,
                            enabled = !state.isMeasuringLatency,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("instant_auto_sync_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            if (state.isMeasuringLatency) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.measureAndAutoSyncBtn)
                        }
                    }
                }
            }

            // 2. Live Latency Telemetry Card
            item(key = "latency_telemetry") {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("latency_telemetry_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                    imageVector = Icons.Rounded.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = strings.latencyTelemetryTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (measurement.sourceLabel.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = measurement.sourceLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = strings.latencyTelemetrySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 3 Telemetry Metric Tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LatencyMetricTile(
                                label = strings.metricWiredLabel,
                                valueText = measurement.activeWiredOrUsbMs?.let { "$it ms" } ?: "--",
                                accent = SignalEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            LatencyMetricTile(
                                label = strings.metricBluetoothLabel,
                                valueText = measurement.bluetoothMs?.let { "$it ms" } ?: "--",
                                accent = SignalCyan,
                                modifier = Modifier.weight(1f)
                            )
                            LatencyMetricTile(
                                label = strings.metricDeltaLabel,
                                valueText = measurement.estimatedDeltaMs?.let { "+$it ms" } ?: "--",
                                accent = SignalAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onMeasureLatency,
                                enabled = !state.isMeasuringLatency,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("measure_latency_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Speed,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.measureLatencyBtn,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            OutlinedButton(
                                onClick = onApplyMeasuredDelta,
                                enabled = measurement.estimatedDeltaMs != null,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("apply_measured_delta_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoFixHigh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.applyMeasuredDeltaBtn,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 3. Precision Millisecond Delay Control Deck
            item(key = "delay_slider_deck") {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delay_control_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.delayControlTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.delayControlExplain,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Oversized Live Millisecond Readout
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.background,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "+${state.wiredDelayMs} ms",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("current_delay_ms_text")
                                )
                            }
                        }

                        // Live Slider 0..500 ms
                        Slider(
                            value = state.wiredDelayMs.toFloat(),
                            onValueChange = { onSetDelayMs(it.roundToInt()) },
                            valueRange = 0f..AudioDelayBuffer.MAX_DELAY_MS.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wired_delay_slider")
                        )

                        // Fine-tune +/- buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(-25, -5, 5, 25).forEach { step ->
                                val label = if (step > 0) "+$step" else "$step"
                                OutlinedButton(
                                    onClick = { onAdjustDelayBy(step) },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("delay_step_${if (step > 0) "plus_$step" else "minus_${-step}"}")
                                ) {
                                    Text(
                                        text = "${label}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = JetBrainsMonoFamily
                                    )
                                }
                            }
                        }

                        // Quick Presets Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(150, 200, 250, 300).forEach { preset ->
                                FilterChip(
                                    selected = state.wiredDelayMs == preset,
                                    onClick = { onSetDelayMs(preset) },
                                    label = {
                                        Text(
                                            text = "${preset}ms",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = JetBrainsMonoFamily
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("delay_preset_$preset")
                                )
                            }
                        }
                    }
                }
            }

            // 4. Independent Per-Headphone Volume Mixer
            item(key = "sync_dual_volume_mixer") {
                DualVolumeMixerCard(
                    state = state,
                    strings = strings,
                    onWiredVolumeChange = onWiredVolumeChange,
                    onStepWiredVolume = onStepWiredVolume,
                    onBluetoothVolumeChange = onBluetoothVolumeChange,
                    onStepBluetoothVolume = onStepBluetoothVolume,
                    onToggleWiredMute = onToggleWiredMute,
                    onToggleBluetoothMute = onToggleBluetoothMute
                )
            }

            // 5. Acoustic Metronome Sync Calibrator Card
            item(key = "acoustic_calibrator") {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (state.isCalibrating) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Hearing,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.calibratorTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = strings.calibratorDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilledTonalButton(
                            onClick = onToggleCalibrator,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("toggle_sync_calibrator_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (state.isCalibrating) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = if (state.isCalibrating) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        ) {
                            Icon(
                                imageVector = if (state.isCalibrating) Icons.Rounded.Stop else Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isCalibrating) {
                                    strings.stopCalibratorBtn
                                } else {
                                    strings.startCalibratorBtn
                                }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LatencyMetricTile(
    label: String,
    valueText: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                color = accent,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
