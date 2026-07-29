package com.hermes.agent.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = HermesPrimary,
    onPrimary = HermesOnPrimary,
    primaryContainer = HermesPrimaryContainer,
    onPrimaryContainer = HermesOnPrimaryContainer,
    secondary = HermesSecondary,
    onSecondary = HermesOnSecondary,
    secondaryContainer = HermesSecondaryContainer,
    onSecondaryContainer = HermesOnSecondaryContainer,
    tertiary = HermesTertiary,
    onTertiary = HermesOnTertiary,
    tertiaryContainer = HermesTertiaryContainer,
    onTertiaryContainer = HermesOnTertiaryContainer,
    background = HermesBackground,
    onBackground = HermesOnBackground,
    surface = HermesSurface,
    onSurface = HermesOnSurface,
    surfaceVariant = HermesSurfaceVariant,
    onSurfaceVariant = HermesOnSurfaceVariant,
    outline = HermesOutline,
    outlineVariant = HermesOutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = HermesPrimaryDark,
    onPrimary = HermesOnPrimaryDark,
    primaryContainer = HermesPrimaryContainerDark,
    onPrimaryContainer = HermesOnPrimaryContainerDark,
    secondary = HermesSecondaryDark,
    onSecondary = HermesOnSecondaryDark,
    secondaryContainer = HermesSecondaryContainerDark,
    onSecondaryContainer = HermesOnSecondaryContainerDark,
    tertiary = HermesTertiaryDark,
    onTertiary = HermesOnTertiaryDark,
    tertiaryContainer = HermesTertiaryContainerDark,
    onTertiaryContainer = HermesOnTertiaryContainerDark,
    background = HermesBackgroundDark,
    onBackground = HermesOnBackgroundDark,
    surface = HermesSurfaceDark,
    onSurface = HermesOnSurfaceDark,
    surfaceVariant = HermesSurfaceVariantDark,
    onSurfaceVariant = HermesOnSurfaceVariantDark,
    outline = HermesOutlineDark,
    outlineVariant = HermesOutlineVariantDark
)

/**
 * Hermes Agent Material 3 theme.
 *
 * @param darkMode "system", "dark", or "light"
 */
@Composable
fun HermesAgentTheme(
    darkMode: String = "system",
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (darkMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
