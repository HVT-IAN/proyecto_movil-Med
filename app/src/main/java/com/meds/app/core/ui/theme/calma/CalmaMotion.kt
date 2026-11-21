package com.meds.app.core.ui.theme.calma

/**
 * Duraciones del módulo paciente.
 *
 * La navegación usa un fundido corto para no añadir desplazamientos que
 * distraigan. El jardín conserva un movimiento propio, lento y opcional,
 * solo dentro de su pantalla.
 */
object CalmaMotion {
    const val NAV_FADE_MILLIS = 120
    const val GARDEN_SWAY_MILLIS = 3600
    const val GARDEN_PROGRESS_MILLIS = 900

    const val SPLASH_ENTRANCE_MILLIS = 520
    const val SPLASH_STAGGER_MILLIS = 90
    const val SPLASH_BREATH_MILLIS = 2600
    const val SPLASH_EXIT_MILLIS = 260
    const val SCREEN_FADE_MILLIS = 260
    const val SPLASH_MIN_VISIBLE_MILLIS = 1100
}
