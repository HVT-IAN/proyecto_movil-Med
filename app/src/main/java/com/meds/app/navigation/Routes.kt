package com.meds.app.navigation

import android.net.Uri

/**
 * Definición central de rutas de Navigation Compose.
 *
 * Ninguna pantalla debe escribir cadenas de navegación a mano: usa estas
 * constantes y funciones para construir destinos con argumentos seguros.
 */
object Routes {

    object Patient {
        const val HOME = "home"
        const val MEDICATIONS = "medications"
        const val GARDEN = "garden"
        const val SETTINGS = "settings"
        const val HISTORY = "history"
        const val COMPANIONS = "companions"

        const val EDITOR_NEW = "medication_editor/new"
        const val EDITOR_PATTERN = "medication_editor/{medicationId}"
        const val MEDICATION_INFO_PATTERN = "medication_info?query={query}"

        const val ARG_MEDICATION_ID = "medicationId"
        const val ARG_QUERY = "query"

        const val RESULT_SELECTED_MEDICATION_NAME = "selected_medication_name"

        val topLevelDestinations = setOf(HOME, MEDICATIONS, GARDEN, SETTINGS)

        fun medicationEditor(medicationId: String): String =
            "medication_editor/${Uri.encode(medicationId)}"

        fun medicationInfo(query: String): String =
            "medication_info?query=${Uri.encode(query)}"
    }
}
