package com.meds.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val TealPrimary = Color(0xFF0F766E)
val TealPrimaryDark = Color(0xFF6ED0BE)
val Ink = Color(0xFF1F2A30)
val Slate = Color(0xFF5C6B73)
val Cloud = Color(0xFFF7FAF9)
val Paper = Color(0xFFFFFFFF)
val Mint = Color(0xFFEDF4F1)
val Border = Color(0xFFDCE5E2)
val Amber = Color(0xFFB7791F)
val Green = Color(0xFF2F855A)
val Clay = Color(0xFFB4553F)
val CompanionBlue = Color(0xFF5B6C9E)
val DeepNight = Color(0xFF0F171A)
val NightSurface = Color(0xFF172226)
val NightInk = Color(0xFFE8F0ED)

@Immutable
data class MedsSemanticColors(
    val taken: Color,
    val takenContainer: Color,
    val onTakenContainer: Color,
    val pending: Color,
    val pendingContainer: Color,
    val onPendingContainer: Color,
    val companion: Color,
    val companionContainer: Color,
    val onCompanionContainer: Color,
    val cardBorder: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val pot: Color,
    val leaf: Color,
    val leafDeep: Color
)

val LightMedsColors = MedsSemanticColors(
    taken = Green,
    takenContainer = Color(0xFFDDF0E6),
    onTakenContainer = Color(0xFF0E3B27),
    pending = Amber,
    pendingContainer = Color(0xFFF7E7C6),
    onPendingContainer = Color(0xFF4A2E00),
    companion = CompanionBlue,
    companionContainer = Color(0xFFE3E8F5),
    onCompanionContainer = Color(0xFF1C2643),
    cardBorder = Border,
    heroStart = Paper,
    heroEnd = Mint,
    pot = Color(0xFF8FA8A2),
    leaf = TealPrimary,
    leafDeep = Color(0xFF0A5C55)
)

val DarkMedsColors = MedsSemanticColors(
    taken = Color(0xFF7FD3A6),
    takenContainer = Color(0xFF1B3A2A),
    onTakenContainer = Color(0xFFD8F5E4),
    pending = Color(0xFFE3B341),
    pendingContainer = Color(0xFF4A3A12),
    onPendingContainer = Color(0xFFF7E7C6),
    companion = Color(0xFFB8C4E8),
    companionContainer = Color(0xFF2C3650),
    onCompanionContainer = Color(0xFFDCE3F7),
    cardBorder = Color(0xFF2A3A3F),
    heroStart = NightSurface,
    heroEnd = Color(0xFF1D2C2A),
    pot = Color(0xFF6F8C86),
    leaf = TealPrimaryDark,
    leafDeep = Color(0xFFA8F0E3)
)

val LocalMedsColors = staticCompositionLocalOf { LightMedsColors }
