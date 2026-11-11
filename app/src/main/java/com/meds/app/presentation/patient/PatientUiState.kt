package com.meds.app.presentation.patient

import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.Medication
import com.meds.app.domain.model.Nudge
import com.meds.app.domain.model.PlantState
import com.meds.app.domain.model.Schedule

/** Estado de la pantalla Hoy y del módulo de medicamentos del paciente. */
data class PatientUiState(
    val loading: Boolean = true,
    val medications: List<Medication> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val todayDoses: List<DoseLog> = emptyList(),
    val recentDoses: List<DoseLog> = emptyList(),
    val plant: PlantState = PlantState(),
    val nudges: List<Nudge> = emptyList(),
    val error: String? = null,
    val saving: Boolean = false,
    val saveSuccess: Boolean = false,
    val pendingChanges: Int = 0
)
