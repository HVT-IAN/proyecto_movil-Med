package com.meds.app.core.ui.theme

import android.animation.ValueAnimator
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalReduceMotion = staticCompositionLocalOf { false }
val LocalHighContrast = staticCompositionLocalOf { false }
val LocalShowGraphLabels = staticCompositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EFEA),
    onPrimaryContainer = Color(0xFF063B36),
    secondary = CompanionBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3E8F5),
    onSecondaryContainer = Color(0xFF1C2643),
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF7E7C6),
    onTertiaryContainer = Color(0xFF4A2E00),
    background = Cloud,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Mint,
    onSurfaceVariant = Slate,
    outline = Border,
    outlineVariant = Color(0xFFE8EFEC),
    error = Clay,
    onError = Color.White,
    errorContainer = Color(0xFFF5DAD4),
    onErrorContainer = Color(0xFF4A1A10),
    inverseSurface = Ink,
    inverseOnSurface = Cloud,
    inversePrimary = TealPrimaryDark,
    scrim = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF1E3A36),
    onPrimaryContainer = Color(0xFFA8F0E3),
    secondary = Color(0xFFB8C4E8),
    onSecondary = Color(0xFF1C2643),
    secondaryContainer = Color(0xFF2C3650),
    onSecondaryContainer = Color(0xFFDCE3F7),
    tertiary = Color(0xFFE3B341),
    onTertiary = Color(0xFF3D2A00),
    tertiaryContainer = Color(0xFF4A3A12),
    onTertiaryContainer = Color(0xFFF7E7C6),
    background = DeepNight,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = Color(0xFF223036),
    onSurfaceVariant = Color(0xFFA3B3B8),
    outline = Color(0xFF2A3A3F),
    outlineVariant = Color(0xFF243237),
    error = Color(0xFFE6A08E),
    onError = Color(0xFF4A1A10),
    errorContainer = Color(0xFF4A1A10),
    onErrorContainer = Color(0xFFF5DAD4),
    inverseSurface = NightInk,
    inverseOnSurface = Ink,
    inversePrimary = TealPrimary,
    scrim = Color.Black
)

private val HighContrastLightColors = LightColors.copy(
    primary = Color(0xFF0B5F58),
    outline = Ink,
    outlineVariant = Slate,
    onSurfaceVariant = Ink,
    surfaceVariant = Cloud
)

private val HighContrastDarkColors = DarkColors.copy(
    primary = Color(0xFF8FE3D2),
    outline = NightInk,
    outlineVariant = Color(0xFFA3B3B8),
    onSurfaceVariant = NightInk,
    surfaceVariant = Color(0xFF223036)
)

@Composable
fun MedsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    reduceMotion: Boolean = false,
    highContrast: Boolean = false,
    graphLabels: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme && highContrast -> HighContrastDarkColors
        darkTheme -> DarkColors
        highContrast -> HighContrastLightColors
        else -> LightColors
    }
    val semanticColors = when {
        darkTheme && highContrast -> HighContrastDarkMedsColors
        darkTheme -> DarkMedsColors
        highContrast -> HighContrastLightMedsColors
        else -> LightMedsColors
    }

    val systemReduceMotion = !ValueAnimator.areAnimatorsEnabled()
    val resolvedReduceMotion = reduceMotion || systemReduceMotion

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalMedsColors provides semanticColors,
        LocalReduceMotion provides resolvedReduceMotion,
        LocalHighContrast provides highContrast,
        LocalShowGraphLabels provides graphLabels
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MedsTypography,
            shapes = MedsShapes,
            content = content
        )
    }
}
