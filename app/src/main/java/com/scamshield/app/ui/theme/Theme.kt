package com.scamshield.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ScamShieldColors = darkColorScheme(
    primary = ElectricBlue,
    secondary = ElectricBlue,
    tertiary = SafeGreen,

    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,

    onPrimary = LightText,
    onSecondary = LightText,
    onTertiary = LightText,

    onBackground = LightText,
    onSurface = LightText,
    onSurfaceVariant = MutedText
)

@Composable
fun ScamshieldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ScamShieldColors,
        typography = Typography,
        content = content
    )
}