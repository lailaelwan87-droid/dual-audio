package com.dualaudio.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dualaudio.app.audio.ConnectedAudioDevice
import com.dualaudio.app.audio.DualAudioUiState
import com.dualaudio.app.audio.RoutingMode
import com.dualaudio.app.audio.ShizukuStatus
import com.dualaudio.app.ui.UiStrings
import com.dualaudio.app.ui.categoryLabel
import com.dualaudio.app.ui.components.DualSignalHeroCard
import com.dualaudio.app.ui.components.DualVolumeMixerCard
import com.dualaudio.app.ui.modeDescription
import com.dualaudio.app.ui.modeTitle
import com.dualaudio.app.ui.shizukuDesc
import com.dualaudio.app.ui.shizukuTitle
import com.dualaudio.app.ui.theme.JetBrainsMonoFamily

@Composable
fun RouterConsoleScreen(
    state: DualAudioUiState,
    strings: UiStrings,
    onSelectMode: (RoutingMode) -> Unit,
    onToggleCustomDevice: (ConnectedAudioDevice) -> Unit,
    onEnableDualAudio: () -> Unit,
    onDisableDualAudio: () -> Unit,
    onPlayTestTone: () -> Unit,
    onSetAutoReapply: (Boolean) -> Unit,
    onSetBluetoothFirst: (Boolean) -> Unit,
    onRequestShizukuPermission: () -> Unit,
    onOpenShizuku: () -> Unit,
    onOpenSyncTab: () -> Unit,
    onWiredVolumeChange: (Float) -> Unit,
    onStepWiredVolume: (Int) -> Unit,
    onBluetoothVolumeChange: (Float) -> Unit,
    onStepBluetoothVolume: (Int) -> Unit,
    onToggleWiredMute: () -> Unit,
    onToggleBluetoothMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shizukuReady = state.shizukuStatus == ShizukuStatus.READY

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
            // 1. Shizuku Privilege Status Strip
            item(key = "shizuku_card") {
                ShizukuStatusCard(
                    status = state.shizukuStatus,
                    strings = strings,
                    onGrantPermission = onRequestShizukuPermission,
                    onOpenShizuku = onOpenShizuku
                )
            }

            // 2. Animated Dual-Channel Signal Visualizer Hero
            item(key = "signal_hero") {
                DualSignalHeroCard(
                    state = state,
                    strings = strings
                )
            }

            // 3. Feedback Banner from last routing command
            item(key = "action_banner") {
                AnimatedVisibility(
                    visible = !state.lastActionBanner.isNullOrBlank(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val isError = state.isLastActionError
                    val containerColor = if (isError) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }
                    val contentColor = if (isError) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = containerColor,
                        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("routing_feedback_banner")
                    ) {
                        Text(
                            text = state.lastActionBanner.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // 4. Master Dual Audio Controls + Test Chime
            item(key = "master_controls") {
                MasterControlDeck(
                    shizukuReady = shizukuReady,
                    isBusy = state.isBusy,
                    isEnabled = state.isDualAudioEnabled,
                    isTestTonePlaying = state.isTestTonePlaying,
                    strings = strings,
                    onEnable = onEnableDualAudio,
                    onDisable = onDisableDualAudio,
                    onPlayTest = onPlayTestTone
                )
            }

            // 4a. Independent Per-Headphone Volume Mixer
            item(key = "dual_volume_mixer") {
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

            // 4b. Quick Bluetooth Delay & Sync Control Shortcut
            item(key = "quick_sync_shortcut") {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .clickable { onOpenSyncTab() }
                        .testTag("quick_sync_shortcut_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.quickSyncBannerTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val autoSpeedText = state.lastAutoSyncDurationMs?.let { " • ⚡${it}ms" } ?: ""
                            Text(
                                text = "+${state.wiredDelayMs} ms" +
                                    (state.latencyMeasurement.estimatedDeltaMs?.let { " • Δ≈$it ms" } ?: "") +
                                    (if (state.autoSyncLatency) " • ${strings.autoSyncActiveChip}$autoSpeedText" else ""),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMonoFamily,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = strings.quickSyncBannerCta,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 5. Routing Mode Selector (3.5mm Wired + BT default!)
            item(key = "routing_modes") {
                RoutingModeSelectorCard(
                    selectedMode = state.selectedMode,
                    strings = strings,
                    onSelectMode = onSelectMode
                )
            }

            // 6. Custom Pair Device Picker (shown when CUSTOM mode is selected)
            if (state.selectedMode == RoutingMode.CUSTOM) {
                item(key = "custom_picker") {
                    CustomDevicePickerCard(
                        devices = state.connectedDevices,
                        selectedKeys = state.customSelectedKeys,
                        strings = strings,
                        onToggleDevice = onToggleCustomDevice
                    )
                }
            }

            // 7. Advanced Routing Options
            item(key = "advanced_options") {
                AdvancedRoutingOptionsCard(
                    autoReapply = state.autoReapply,
                    bluetoothFirst = state.bluetoothFirst,
                    strings = strings,
                    onSetAutoReapply = onSetAutoReapply,
                    onSetBluetoothFirst = onSetBluetoothFirst
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ShizukuStatusCard(
    status: ShizukuStatus,
    strings: UiStrings,
    onGrantPermission: () -> Unit,
    onOpenShizuku: () -> Unit
) {
    val accent = when (status) {
        ShizukuStatus.READY -> MaterialTheme.colorScheme.primary
        ShizukuStatus.PERMISSION_REQUIRED,
        ShizukuStatus.CONNECTING_SERVICE -> MaterialTheme.colorScheme.secondary
        ShizukuStatus.SHIZUKU_NOT_RUNNING,
        ShizukuStatus.SHIZUKU_NOT_INSTALLED,
        ShizukuStatus.SHIZUKU_TOO_OLD -> MaterialTheme.colorScheme.tertiary
        ShizukuStatus.ANDROID_TOO_OLD -> MaterialTheme.colorScheme.error
    }

    val icon = when (status) {
        ShizukuStatus.READY -> Icons.Rounded.CheckCircle
        ShizukuStatus.PERMISSION_REQUIRED -> Icons.Rounded.Security
        ShizukuStatus.CONNECTING_SERVICE -> Icons.Rounded.GraphicEq
        else -> Icons.Rounded.WarningAmber
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shizuku_status_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = strings.shizukuTitle(status),
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.shizukuTitle(status),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.shizukuDesc(status),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when (status) {
                ShizukuStatus.PERMISSION_REQUIRED -> {
                    Button(
                        onClick = onGrantPermission,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("grant_shizuku_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.shizukuGrantBtn)
                    }
                }

                ShizukuStatus.SHIZUKU_NOT_RUNNING,
                ShizukuStatus.SHIZUKU_NOT_INSTALLED -> {
                    FilledTonalButton(
                        onClick = onOpenShizuku,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("open_shizuku_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (status == ShizukuStatus.SHIZUKU_NOT_INSTALLED) {
                                strings.shizukuInstallBtn
                            } else {
                                strings.shizukuOpenBtn
                            }
                        )
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun MasterControlDeck(
    shizukuReady: Boolean,
    isBusy: Boolean,
    isEnabled: Boolean,
    isTestTonePlaying: Boolean,
    strings: UiStrings,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onPlayTest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onEnable,
                enabled = shizukuReady && !isBusy,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .weight(1.35f)
                    .heightIn(min = 54.dp)
                    .testTag("enable_dual_audio_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.enableDualAudioBtn,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = onDisable,
                enabled = shizukuReady && !isBusy,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isEnabled) {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 54.dp)
                    .testTag("disable_dual_audio_button")
            ) {
                Text(
                    text = strings.disableDualAudioBtn,
                    color = if (isEnabled) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Built-in Stereo Media Test Tone Button (works anytime to verify dual playback!)
        FilledTonalButton(
            onClick = onPlayTest,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("play_test_sound_button"),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isTestTonePlaying) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isTestTonePlaying) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        ) {
            Icon(
                imageVector = if (isTestTonePlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isTestTonePlaying) strings.stopTestSoundBtn else strings.testSoundBtn
            )
        }
    }
}

@Composable
private fun RoutingModeSelectorCard(
    selectedMode: RoutingMode,
    strings: UiStrings,
    onSelectMode: (RoutingMode) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = strings.routingModeSectionTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RoutingMode.entries.forEach { mode ->
                    val selected = mode == selectedMode
                    val accent = when (mode) {
                        RoutingMode.WIRED_BLUETOOTH -> MaterialTheme.colorScheme.primary
                        RoutingMode.USB_BLUETOOTH -> MaterialTheme.colorScheme.tertiary
                        RoutingMode.AUTO_BLUETOOTH -> MaterialTheme.colorScheme.secondary
                        RoutingMode.CUSTOM -> MaterialTheme.colorScheme.onSurface
                    }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (selected) accent.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 58.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onSelectMode(mode) }
                            .testTag("routing_mode_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = modeIcon(mode),
                                    contentDescription = strings.modeTitle(mode),
                                    tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.modeTitle(mode),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) accent else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.modeDescription(mode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomDevicePickerCard(
    devices: List<ConnectedAudioDevice>,
    selectedKeys: List<String>,
    strings: UiStrings,
    onToggleDevice: (ConnectedAudioDevice) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_device_picker_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${strings.customPickerTitle} (${selectedKeys.size}/2)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            devices.forEach { device ->
                val checked = selectedKeys.contains(device.uniqueKey)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleDevice(device) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("custom_device_${device.type}_${device.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { onToggleDevice(device) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${strings.categoryLabel(device.category)} • ${device.typeLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedRoutingOptionsCard(
    autoReapply: Boolean,
    bluetoothFirst: Boolean,
    strings: UiStrings,
    onSetAutoReapply: (Boolean) -> Unit,
    onSetBluetoothFirst: (Boolean) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.advancedOptionsTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Auto-reapply toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.autoReapplyTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.autoReapplyDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoReapply,
                    onCheckedChange = onSetAutoReapply,
                    modifier = Modifier.testTag("auto_reapply_switch")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Primary device order selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.primaryOrderTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = strings.primaryOrderDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !bluetoothFirst,
                        onClick = { onSetBluetoothFirst(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("order_wired_first_button")
                    ) {
                        Text(
                            text = strings.primaryOrderWiredFirst,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    SegmentedButton(
                        selected = bluetoothFirst,
                        onClick = { onSetBluetoothFirst(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("order_bt_first_button")
                    ) {
                        Text(
                            text = strings.primaryOrderBtFirst,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun modeIcon(mode: RoutingMode): ImageVector = when (mode) {
    RoutingMode.WIRED_BLUETOOTH -> Icons.Rounded.Headphones
    RoutingMode.USB_BLUETOOTH -> Icons.Rounded.Usb
    RoutingMode.AUTO_BLUETOOTH -> Icons.Rounded.AutoAwesome
    RoutingMode.CUSTOM -> Icons.Rounded.Tune
}
