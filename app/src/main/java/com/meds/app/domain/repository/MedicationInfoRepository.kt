package com.meds.app.domain.repository

import com.meds.app.domain.model.MedicationInfoDetail
import com.meds.app.domain.model.MedicationSearchResult

/** Contrato de consulta de información de medicamentos en CIMA. */
interface MedicationInfoRepository {
    suspend fun search(rawQuery: String): Result<MedicationSearchResult>

    suspend fun detail(registry: String): Result<MedicationInfoDetail>
}
