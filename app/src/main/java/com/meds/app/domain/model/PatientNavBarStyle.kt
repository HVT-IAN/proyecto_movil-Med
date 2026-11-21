package com.meds.app.domain.model

/** Estilo de la barra de navegación inferior del paciente. */
enum class PatientNavBarStyle {
    STATIC,
    GRAPH_ICONS;

    companion object {
        fun fromName(value: String?): PatientNavBarStyle =
            entries.firstOrNull { it.name == value } ?: STATIC
    }
}
