package com.meds.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

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

@Composable
fun MedsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val semanticColors = if (darkTheme) DarkMedsColors else LightMedsColors

    CompositionLocalProvider(LocalMedsColors provides semanticColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MedsTypography,
            shapes = MedsShapes,
            content = content
        )
    }
}
