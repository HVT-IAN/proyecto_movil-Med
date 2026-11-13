package com.meds.app.data.repository

import com.meds.app.data.local.OfflineMedicationStore
import com.meds.app.domain.repository.MedicationCacheRepository

/** Implementación de la caché local de medicamentos sobre DataStore. */
class MedicationCacheRepositoryImpl(
    private val store: OfflineMedicationStore
) : MedicationCacheRepository {
    override suspend fun clearAll() {
        store.clearAll()
    }
}
