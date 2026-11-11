package com.meds.app.presentation.companion

import com.meds.app.domain.model.CompanionDay
import com.meds.app.domain.model.LinkedPatient

/** Estado del panel del acompañante. */
data class CompanionUiState(
    val loading: Boolean = true,
    val linkedPatients: List<LinkedPatient> = emptyList(),
    val selectedPatient: LinkedPatient? = null,
    val day: CompanionDay? = null,
    val linking: Boolean = false,
    val nudgeSending: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)
