package com.meds.app.domain.repository

/** Contrato de la caché local de medicamentos y su cola de sincronización. */
interface MedicationCacheRepository {
    suspend fun clearAll()
}
