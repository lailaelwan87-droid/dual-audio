package com.dualaudio.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.Cable
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dualaudio.app.audio.ConnectedAudioDevice
import com.dualaudio.app.audio.DualAudioUiState
import com.dualaudio.app.audio.OutputCategory
import com.dualaudio.app.audio.RoutingMode
import com.dualaudio.app.ui.UiStrings
import com.dualaudio.app.ui.theme.SignalAmber
import com.dualaudio.app.ui.theme.SignalCyan
import com.dualaudio.app.ui.theme.SignalEmerald
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun DualSignalHeroCard(
    state: DualAudioUiState,
    strings: UiStrings,
    modifier: Modifier = Modifier
) {
    val isLive = state.isDualAudioEnabled && state.isRoutingActuallyApplied
    val isWaiting = state.isDualAudioEnabled && !state.isRoutingActuallyApplied

    val channelAColor = when (state.selectedMode) {
        RoutingMode.USB_BLUETOOTH -> SignalAmber
        else -> SignalEmerald
    }
    val channelBColor = SignalCyan

    val primaryConnected = state.primarySlotDevice != null
    val secondaryConnected = state.secondarySlotDevice != null

    val infiniteTransition = rememberInfiniteTransition(label = "signal_flow")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isLive) 1800 else 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val statusColor = when {
        isLive -> MaterialTheme.colorScheme.primary
        isWaiting -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusHeadline = when {
        isLive -> strings.visualizerActiveTitle
        isWaiting -> strings.visualizerWaitingTitle
        else -> strings.visualizerStandbyTitle
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isLive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
            }
        ),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = if (isLive) 0.14f else 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Top live status strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = statusHeadline,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isLive) statusColor else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Canvas dual-waveguide splitter
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                val w = size.width
                val h = size.height
                val centerX = w * 0.5f
                val coreY = h * 0.22f
                val leftX = w * 0.22f
                val rightX = w * 0.78f
                val bottomY = h * 0.88f

                val leftPath = Path().apply {
                    moveTo(centerX, coreY)
                    cubicTo(centerX, h * 0.56f, leftX, h * 0.44f, leftX, bottomY)
                }
                val rightPath = Path().apply {
                    moveTo(centerX, coreY)
                    cubicTo(centerX, h * 0.56f, rightX, h * 0.44f, rightX, bottomY)
                }

                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f)

                // Left signal trace
                drawPath(
                    path = leftPath,
                    color = if (primaryConnected) channelAColor.copy(alpha = if (isLive) 0.9f else 0.45f)
                    else channelAColor.copy(alpha = 0.2f),
                    style = Stroke(
                        width = if (isLive) 5.5f else 3.5f,
                        cap = StrokeCap.Round,
                        pathEffect = if (primaryConnected) null else dashEffect
                    )
                )

                // Right signal trace
                drawPath(
                    path = rightPath,
                    color = if (secondaryConnected) channelBColor.copy(alpha = if (isLive) 0.9f else 0.45f)
                    else channelBColor.copy(alpha = 0.2f),
                    style = Stroke(
                        width = if (isLive) 5.5f else 3.5f,
                        cap = StrokeCap.Round,
                        pathEffect = if (secondaryConnected) null else dashEffect
                    )
                )

                // Animated traveling audio packets along both paths when active or testing
                if (isLive || state.isTestTonePlaying) {
                    for (k in 0..2) {
                        val t = (phase + k * 0.33f) % 1f
                        val lx = cubicPoint(centerX, centerX, leftX, leftX, t)
                        val ly = cubicPoint(coreY, h * 0.56f, h * 0.44f, bottomY, t)
                        drawCircle(
                            color = channelAColor,
                            radius = 7f,
                            center = Offset(lx, ly)
                        )
                        drawCircle(
                            color = channelAColor.copy(alpha = 0.3f),
                            radius = 14f,
                            center = Offset(lx, ly)
                        )

                        val rx = cubicPoint(centerX, centerX, rightX, rightX, t)
                        val ry = cubicPoint(coreY, h * 0.56f, h * 0.44f, bottomY, t)
                        drawCircle(
                            color = channelBColor,
                            radius = 7f,
                            center = Offset(rx, ry)
                        )
                        drawCircle(
                            color = channelBColor.copy(alpha = 0.3f),
                            radius = 14f,
                            center = Offset(rx, ry)
                        )
                    }
                }

                // Central acoustic core badge & equalizer bars
                val pulseRadius = 24f + if (isLive || state.isTestTonePlaying) {
                    (sin(phase * 2f * PI).toFloat() * 4f)
                } else 0f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.32f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, coreY),
                        radius = pulseRadius * 1.7f
                    ),
                    radius = pulseRadius * 1.7f,
                    center = Offset(centerX, coreY)
                )

                drawCircle(
                    color = statusColor,
                    radius = 18f,
                    center = Offset(centerX, coreY)
                )

                // Equalizer bars inside central core
                val barOffsets = floatArrayOf(-7f, 0f, 7f)
                barOffsets.forEachIndexed { idx, dx ->
                    val wave = if (isLive || state.isTestTonePlaying) {
                        4f + 6f * ((sin((phase * 2.0 * PI) + idx * 1.3).toFloat() + 1f) * 0.5f)
                    } else {
                        if (idx == 1) 7f else 4f
                    }
                    drawLine(
                        color = Color(0xFF081018),
                        start = Offset(centerX + dx, coreY - wave),
                        end = Offset(centerX + dx, coreY + wave),
                        strokeWidth = 3.2f,
                        cap = StrokeCap.Round
                    )
                }

                // Terminal nodes
                drawCircle(
                    color = if (primaryConnected) channelAColor else channelAColor.copy(alpha = 0.25f),
                    radius = 7f,
                    center = Offset(leftX, bottomY)
                )
                drawCircle(
                    color = if (secondaryConnected) channelBColor else channelBColor.copy(alpha = 0.25f),
                    radius = 7f,
                    center = Offset(rightX, bottomY)
                )
            }

            // Dual hardware endpoint cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EndpointSlotCard(
                    slotLabel = strings.channelALabel,
                    device = state.primarySlotDevice,
                    waitingText = when (state.selectedMode) {
                        RoutingMode.WIRED_BLUETOOTH -> strings.waitingWiredHint
                        RoutingMode.USB_BLUETOOTH -> strings.waitingUsbHint
                        RoutingMode.AUTO_BLUETOOTH -> strings.waitingAutoHint
                        RoutingMode.CUSTOM -> strings.selectTwoHint
                    },
                    accent = channelAColor,
                    icon = iconForSlotA(state.selectedMode, state.primarySlotDevice),
                    connectedLabel = strings.connectedBadge,
                    disconnectedLabel = strings.disconnectedBadge,
                    modifier = Modifier.weight(1f)
                )
                EndpointSlotCard(
                    slotLabel = strings.channelBLabel,
                    device = state.secondarySlotDevice,
                    waitingText = if (state.selectedMode == RoutingMode.CUSTOM) {
                        strings.selectTwoHint
                    } else {
                        strings.waitingBluetoothHint
                    },
                    accent = channelBColor,
                    icon = iconForSlotB(state.secondarySlotDevice),
                    connectedLabel = strings.connectedBadge,
                    disconnectedLabel = strings.disconnectedBadge,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EndpointSlotCard(
    slotLabel: String,
    device: ConnectedAudioDevice?,
    waitingText: String,
    accent: Color,
    icon: ImageVector,
    connectedLabel: String,
    disconnectedLabel: String,
    modifier: Modifier = Modifier
) {
    val connected = device != null
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (connected) 0.9f else 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = if (connected) accent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = if (connected) 0.18f else 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = slotLabel,
                        tint = if (connected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = if (connected) accent.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = if (connected) connectedLabel else disconnectedLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (connected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = device?.name ?: waitingText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (connected) FontWeight.Bold else FontWeight.Medium,
                color = if (connected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = device?.typeLabel ?: slotLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun iconForSlotA(mode: RoutingMode, device: ConnectedAudioDevice?): ImageVector {
    return when (device?.category) {
        OutputCategory.WIRED_35MM -> Icons.Rounded.Headphones
        OutputCategory.USB_AUDIO -> Icons.Rounded.Usb
        OutputCategory.BLUETOOTH -> Icons.Rounded.BluetoothAudio
        OutputCategory.BUILTIN_SPEAKER -> Icons.Rounded.Speaker
        else -> when (mode) {
            RoutingMode.USB_BLUETOOTH -> Icons.Rounded.Usb
            RoutingMode.WIRED_BLUETOOTH -> Icons.Rounded.Headphones
            else -> Icons.Rounded.Cable
        }
    }
}

private fun iconForSlotB(device: ConnectedAudioDevice?): ImageVector {
    return when (device?.category) {
        OutputCategory.WIRED_35MM -> Icons.Rounded.Headphones
        OutputCategory.USB_AUDIO -> Icons.Rounded.Usb
        OutputCategory.BUILTIN_SPEAKER -> Icons.Rounded.Speaker
        else -> Icons.Rounded.BluetoothAudio
    }
}

private fun cubicPoint(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
    val u = 1f - t
    return u * u * u * p0 + 3f * u * u * t * p1 + 3f * u * t * t * p2 + t * t * t * p3
}
