package com.dualaudio.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DarkStudioColorScheme = darkColorScheme(
    primary = SignalEmerald,
    onPrimary = SignalEmeraldText,
    primaryContainer = SignalEmeraldDeep,
    onPrimaryContainer = SignalEmerald,
    secondary = SignalCyan,
    onSecondary = SignalCyanText,
    secondaryContainer = SignalCyanDeep,
    onSecondaryContainer = SignalCyan,
    tertiary = SignalAmber,
    onTertiary = ObsidianBg,
    tertiaryContainer = SignalAmberDeep,
    onTertiaryContainer = SignalAmber,
    error = SignalCoral,
    onError = TextPrimaryDark,
    errorContainer = SignalCoralDeep,
    onErrorContainer = SignalCoral,
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = StudioSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = StudioSurface,
    surfaceContainer = StudioSurfaceElevated,
    surfaceContainerHigh = StudioSurfaceHigh,
    outline = StudioOutline,
    outlineVariant = StudioOutlineSubtle
)

private val LightStudioColorScheme = lightColorScheme(
    primary = PrimaryTealLight,
    onPrimary = LightSurface,
    primaryContainer = SignalEmerald.copy(alpha = 0.18f),
    onPrimaryContainer = PrimaryTealLight,
    secondary = SecondaryBlueLight,
    onSecondary = LightSurface,
    secondaryContainer = SignalCyan.copy(alpha = 0.16f),
    onSecondaryContainer = SecondaryBlueLight,
    tertiary = TertiaryAmberLight,
    onTertiary = LightSurface,
    tertiaryContainer = SignalAmber.copy(alpha = 0.18f),
    onTertiaryContainer = TertiaryAmberLight,
    error = SignalCoral,
    onError = LightSurface,
    errorContainer = SignalCoral.copy(alpha = 0.14f),
    onErrorContainer = SignalCoral,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutline
)

val DualAudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DualAudioTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkStudioColorScheme else LightStudioColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DualAudioTypography,
        shapes = DualAudioShapes,
        content = content
    )
}
