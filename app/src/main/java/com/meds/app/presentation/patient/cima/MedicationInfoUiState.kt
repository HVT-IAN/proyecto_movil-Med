package com.meds.app.presentation.patient.cima

import com.meds.app.domain.model.MedicationInfo
import com.meds.app.domain.model.MedicationInfoDetail

/** Estado de búsqueda y ficha de la consulta CIMA. */
data class MedicationInfoUiState(
    val query: String = "",
    val loading: Boolean = false,
    val searched: Boolean = false,
    val results: List<MedicationInfo> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
    val detailLoading: Boolean = false,
    val detail: MedicationInfoDetail? = null,
    val detailError: String? = null
)
