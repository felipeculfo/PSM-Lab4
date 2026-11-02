package com.example.lab04.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AeroBluePrimary,
    onPrimary = AeroOnPrimary,
    primaryContainer = AeroPrimaryContainer,
    onPrimaryContainer = AeroOnPrimaryContainer,
    secondary = AeroSecondary,
    onSecondary = AeroOnSecondary,
    secondaryContainer = AeroSecondaryContainer,
    onSecondaryContainer = AeroOnSecondaryContainer,
    tertiary = AeroTertiary,
    onTertiary = AeroOnTertiary,
    tertiaryContainer = AeroTertiaryContainer,
    surface = AeroSurfaceLight,
    surfaceVariant = AeroSurfaceVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = AeroBluePrimaryDark,
    onPrimary = AeroOnPrimaryDark,
    primaryContainer = AeroPrimaryContainerDark,
    onPrimaryContainer = AeroOnPrimaryContainerDark,
    secondary = AeroSecondaryDark,
    onSecondary = AeroOnSecondaryDark,
    secondaryContainer = AeroSecondaryContainerDark,
    onSecondaryContainer = AeroOnSecondaryContainerDark,
    tertiary = AeroTertiaryDark,
    onTertiary = AeroOnTertiaryDark,
    tertiaryContainer = AeroTertiaryContainerDark,
    surface = AeroSurfaceDark,
    surfaceVariant = AeroSurfaceVariantDark
)

@Composable
fun Lab04Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Wyłączamy dynamicColor domyślnie, aby wymusić własną paletę Windows Aero (wymóg punktu 4.1 i 4.2)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}