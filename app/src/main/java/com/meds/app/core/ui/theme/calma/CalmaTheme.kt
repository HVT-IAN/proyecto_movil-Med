package com.meds.app.core.ui.theme.calma

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meds.app.core.ui.theme.Manrope
import com.meds.app.core.ui.theme.MedsTheme

/**
 * Tema del módulo paciente: "Calma operativa".
 *
 * Hereda la paleta y los colores semánticos globales, pero sustituye la
 * tipografía por Manrope en todos los niveles y reduce la escala de los
 * titulares. La intención es una jerarquía clara, sin decoración ambiental
 * ni cambios de familia tipográfica dentro de la misma pantalla.
 */
@Composable
fun CalmaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MedsTheme(darkTheme = darkTheme) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme,
            typography = CalmaTypography,
            shapes = CalmaShapes,
            content = content
        )
    }
}

val CalmaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.6).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.4).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Manrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)

val CalmaShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
