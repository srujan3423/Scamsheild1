package com.scamshield.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ScamShieldColors =
    lightColorScheme(
        primary = ElectricBlue,
        onPrimary = LightSurface,
        primaryContainer = PrimaryTint,
        onPrimaryContainer = DarkText,

        secondary = DarkText,
        onSecondary = LightSurface,

        background = LightBackground,
        onBackground = DarkText,

        surface = LightSurface,
        onSurface = DarkText,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = SecondaryText,

        outline = LightBorder,

        error = DangerRed,
        onError = LightSurface,
        errorContainer = DangerTint,
        onErrorContainer = DangerRed
    )

@Composable
fun ScamshieldTheme(
    content: @Composable () -> Unit
) {

    val view = LocalView.current

    if (!view.isInEditMode) {

        SideEffect {

            val window = (view.context as Activity).window

            window.statusBarColor = LightBackground.toArgb()
            window.navigationBarColor = LightSurface.toArgb()

            val controller = WindowCompat.getInsetsController(window, view)

            // Dark icons on the light status and navigation bars
            controller.isAppearanceLightStatusBars = true
            controller.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = ScamShieldColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}