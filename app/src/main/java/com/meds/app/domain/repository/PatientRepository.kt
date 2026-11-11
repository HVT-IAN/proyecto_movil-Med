package com.meds.app.domain.repository

import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.Medication
import com.meds.app.domain.model.MedicationDraft
import com.meds.app.domain.model.PlantState
import com.meds.app.domain.model.Schedule
import kotlinx.coroutines.flow.Flow

/** Contrato de medicamentos, tomas, alarmas y jardín del paciente. */
interface PatientRepository {
    fun observeMedications(): Flow<List<Medication>>

    fun observeSchedules(): Flow<List<Schedule>>

    fun observeDoses(dateKey: String): Flow<List<DoseLog>>

    fun observeRecentDoses(startDateKey: String): Flow<List<DoseLog>>

    fun observePlant(): Flow<PlantState>

    suspend fun saveMedication(draft: MedicationDraft): Result<Unit>

    suspend fun ensureTodayDoses(): Result<Unit>

    suspend fun takeDose(dose: DoseLog, source: String = "APP"): Result<Unit>

    suspend fun isDoseTaken(logId: String): Boolean

    suspend fun snoozeDose(dose: DoseLog, minutes: Long = 10L): Result<Unit>

    suspend fun deleteMedication(medicationId: String, scheduleId: String?): Result<Unit>

    /** Número de cambios locales pendientes de confirmar en Firestore. */
    fun observePendingChanges(): Flow<Int>

    /** Intenta enviar a Firestore los cambios guardados sin conexión. */
    suspend fun syncPendingChanges()
}
