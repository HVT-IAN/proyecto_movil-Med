package com.meds.app.presentation.patient.companion

import com.meds.app.domain.model.CompanionLink
import com.meds.app.domain.model.Nudge
import com.meds.app.domain.model.PairingInvite

/** Estado de la pantalla de acompañantes del paciente. */
data class PatientCompanionUiState(
    val loading: Boolean = true,
    val companions: List<CompanionLink> = emptyList(),
    val invite: PairingInvite? = null,
    val nudges: List<Nudge> = emptyList(),
    val generating: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)
