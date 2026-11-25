package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AutoIconDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = GraphiteHighest,
    onPrimaryContainer = PureWhite,
    secondary = SoftWhite,
    onSecondary = PureBlack,
    secondaryContainer = GraphiteElevated,
    onSecondaryContainer = SoftWhite,
    tertiary = SyncEmerald,
    onTertiary = PureBlack,
    background = ObsidianBackground,
    onBackground = SoftWhite,
    surface = GraphiteSurface,
    onSurface = SoftWhite,
    surfaceVariant = GraphiteElevated,
    onSurfaceVariant = SilverSecondary,
    surfaceContainerLowest = PureBlack,
    surfaceContainerLow = GraphiteSurface,
    surfaceContainer = GraphiteElevated,
    surfaceContainerHigh = GraphiteHighest,
    outline = GraphiteBorder,
    outlineVariant = GraphiteBorder,
    error = ErrorCrimson,
    onError = PureWhite
)

private val AutoIconShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AutoIconStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AutoIconDarkColorScheme,
        typography = AutoIconTypography,
        shapes = AutoIconShapes,
        content = content
    )
}
