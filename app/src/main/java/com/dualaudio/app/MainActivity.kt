package com.dualaudio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dualaudio.app.audio.AppLanguage
import com.dualaudio.app.ui.DualAudioViewModel
import com.dualaudio.app.ui.screens.DevicesDiagnosticsScreen
import com.dualaudio.app.ui.screens.RouterConsoleScreen
import com.dualaudio.app.ui.screens.SetupGuideScreen
import com.dualaudio.app.ui.screens.SyncDelayScreen
import com.dualaudio.app.ui.stringsFor
import com.dualaudio.app.ui.theme.DualAudioTheme

private enum class DualAudioDestination(
    val routeId: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CONSOLE("route_router_console", Icons.Rounded.GraphicEq, Icons.Outlined.GraphicEq),
    SYNC("route_sync_delay", Icons.Rounded.Tune, Icons.Outlined.Tune),
    DEVICES("route_devices_diagnostics", Icons.Rounded.Devices, Icons.Outlined.Devices),
    GUIDE("route_setup_guide", Icons.Rounded.MenuBook, Icons.Outlined.MenuBook)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val viewModel: DualAudioViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            SideEffect {
                val style = if (state.darkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            DualAudioTheme(darkTheme = state.darkTheme) {
                val layoutDir = if (state.language == AppLanguage.ARABIC) {
                    LayoutDirection.Rtl
                } else {
                    LayoutDirection.Ltr
                }
                CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
                    DualAudioAppShell(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DualAudioAppShell(viewModel: DualAudioViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = stringsFor(state.language)
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableStateOf(DualAudioDestination.CONSOLE) }

    BackHandler(enabled = currentTab != DualAudioDestination.CONSOLE) {
        currentTab = DualAudioDestination.CONSOLE
    }

    val tabLabel: (DualAudioDestination) -> String = { dest ->
        when (dest) {
            DualAudioDestination.CONSOLE -> strings.navConsole
            DualAudioDestination.SYNC -> strings.navSync
            DualAudioDestination.DEVICES -> strings.navDevices
            DualAudioDestination.GUIDE -> strings.navGuide
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = viewModel::refreshConnectionAndStatus,
                            modifier = Modifier.testTag("top_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = strings.refreshCd
                            )
                        }
                        IconButton(
                            onClick = viewModel::toggleTheme,
                            modifier = Modifier.testTag("top_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (state.darkTheme) {
                                    Icons.Rounded.LightMode
                                } else {
                                    Icons.Rounded.DarkMode
                                },
                                contentDescription = strings.toggleThemeCd
                            )
                        }
                        TextButton(
                            onClick = viewModel::toggleLanguage,
                            modifier = Modifier.testTag("top_language_toggle_button")
                        ) {
                            Text(
                                text = strings.languageButtonLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        DualAudioDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = tabLabel(dest),
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tabLabel(dest),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_${dest.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        DualAudioDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = tabLabel(dest)
                                    )
                                },
                                label = { Text(tabLabel(dest)) },
                                modifier = Modifier.testTag("rail_tab_${dest.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        DualAudioDestination.CONSOLE -> RouterConsoleScreen(
                            state = state,
                            strings = strings,
                            onSelectMode = viewModel::selectMode,
                            onToggleCustomDevice = viewModel::toggleCustomDevice,
                            onEnableDualAudio = viewModel::enableDualAudio,
                            onDisableDualAudio = viewModel::disableDualAudio,
                            onPlayTestTone = viewModel::playTestSound,
                            onSetAutoReapply = viewModel::setAutoReapply,
                            onSetBluetoothFirst = viewModel::setBluetoothFirst,
                            onRequestShizukuPermission = viewModel::requestShizukuPermission,
                            onOpenShizuku = { viewModel.openShizukuApp(context) },
                            onOpenSyncTab = { currentTab = DualAudioDestination.SYNC },
                            onWiredVolumeChange = viewModel::setWiredVolume,
                            onStepWiredVolume = viewModel::stepWiredVolume,
                            onBluetoothVolumeChange = viewModel::setBluetoothVolume,
                            onStepBluetoothVolume = viewModel::stepBluetoothVolume,
                            onToggleWiredMute = viewModel::toggleWiredMute,
                            onToggleBluetoothMute = viewModel::toggleBluetoothMute
                        )

                        DualAudioDestination.SYNC -> SyncDelayScreen(
                            state = state,
                            strings = strings,
                            onMeasureLatency = { viewModel.measureLatency(autoApply = state.autoSyncLatency) },
                            onInstantAutoSync = { viewModel.measureLatency(autoApply = true) },
                            onSetAutoSyncLatency = viewModel::setAutoSyncLatency,
                            onApplyMeasuredDelta = viewModel::applyMeasuredDelta,
                            onSetDelayMs = viewModel::setWiredDelayMs,
                            onAdjustDelayBy = viewModel::adjustWiredDelayBy,
                            onToggleCalibrator = viewModel::toggleAcousticCalibrator,
                            onWiredVolumeChange = viewModel::setWiredVolume,
                            onStepWiredVolume = viewModel::stepWiredVolume,
                            onBluetoothVolumeChange = viewModel::setBluetoothVolume,
                            onStepBluetoothVolume = viewModel::stepBluetoothVolume,
                            onToggleWiredMute = viewModel::toggleWiredMute,
                            onToggleBluetoothMute = viewModel::toggleBluetoothMute
                        )

                        DualAudioDestination.DEVICES -> DevicesDiagnosticsScreen(
                            state = state,
                            strings = strings,
                            onRefresh = viewModel::refreshConnectionAndStatus
                        )

                        DualAudioDestination.GUIDE -> SetupGuideScreen(
                            state = state,
                            strings = strings,
                            onOpenShizuku = { viewModel.openShizukuApp(context) }
                        )
                    }
                }
            }
        }
    }
}
