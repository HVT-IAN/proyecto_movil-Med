package com.meds.app.data.repository

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.meds.app.core.common.todayKey
import com.meds.app.data.local.OfflineMedicationStore
import com.meds.app.data.local.PendingMedicationDelete
import com.meds.app.data.remote.firestore.OfflineWritePending
import com.meds.app.data.remote.firestore.asFlow
import com.meds.app.data.remote.firestore.isOfflineFirestoreError
import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.DoseStatus
import com.meds.app.domain.model.Medication
import com.meds.app.domain.model.MedicationDraft
import com.meds.app.domain.model.PlantState
import com.meds.app.domain.model.Schedule
import com.meds.app.domain.model.doseLogId
import com.meds.app.domain.repository.PatientRepository
import com.meds.app.integration.notifications.AlarmScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.UUID

class PatientRepositoryImpl(
    context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val alarmScheduler: AlarmScheduler = AlarmScheduler(context),
    private val offlineStore: OfflineMedicationStore = OfflineMedicationStore(context)
) : PatientRepository {
    private fun uidOrNull(): String? = auth.currentUser?.uid

    private fun uid(): String = uidOrNull() ?: error("No hay una sesión activa.")

    private fun userRef(uid: String): DocumentReference =
        firestore.collection("users").document(uid)

    private fun medicationsRef(uid: String) = userRef(uid).collection("medications")
    private fun schedulesRef(uid: String) = userRef(uid).collection("schedules")
    private fun doseLogsRef(uid: String) = userRef(uid).collection("doseLogs")
    private fun plantRef(uid: String) = userRef(uid).collection("plant").document("state")

    override fun observeMedications(): Flow<List<Medication>> {
        val uid = uidOrNull() ?: return flowOf(emptyList())
        val remote = medicationsRef(uid).asFlow()
            .map { snapshot -> snapshot.toMedications() }
            .onEach { medications -> offlineStore.writeSnapshot(uid, medications = medications) }
            .onStart { emit(offlineStore.read(uid).medications) }
            .catch { /* Sin red se conserva la última instantánea local. */ }
        return combine(remote, offlineStore.observe(uid)) { medications, local ->
            local.mergeMedications(medications)
        }.distinctUntilChanged()
    }

    override fun observeSchedules(): Flow<List<Schedule>> {
        val uid = uidOrNull() ?: return flowOf(emptyList())
        val remote = schedulesRef(uid).asFlow()
            .map { snapshot -> snapshot.toSchedules() }
            .onEach { schedules -> offlineStore.writeSnapshot(uid, schedules = schedules) }
            .onStart { emit(offlineStore.read(uid).schedules) }
            .catch { /* Sin red se conserva la última instantánea local. */ }
        return combine(remote, offlineStore.observe(uid)) { schedules, local ->
            local.mergeSchedules(schedules)
        }.distinctUntilChanged()
    }

    override fun observeDoses(dateKey: String): Flow<List<DoseLog>> {
        val uid = uidOrNull() ?: return flowOf(emptyList())
        return doseLogsRef(uid)
            .whereEqualTo("dateKey", dateKey)
            .asFlow()
            .map { snapshot -> snapshot.toDoseLogs() }
    }

    override fun observeRecentDoses(startDateKey: String): Flow<List<DoseLog>> {
        val uid = uidOrNull() ?: return flowOf(emptyList())
        return doseLogsRef(uid)
            .whereGreaterThanOrEqualTo("dateKey", startDateKey)
            .asFlow()
            .map { snapshot -> snapshot.toDoseLogs() }
    }

    override fun observePlant(): Flow<PlantState> {
        val uid = uidOrNull() ?: return flowOf(PlantState())
        return plantRef(uid).asFlow().map { snapshot -> snapshot.toPlantState() }
    }

    override suspend fun saveMedication(draft: MedicationDraft): Result<Unit> = runCatching {
        val uid = uid()
        val isNew = draft.id.isNullOrBlank()
        val normalized = draft.copy(
            id = draft.id?.takeIf(String::isNotBlank) ?: UUID.randomUUID().toString(),
            scheduleId = draft.scheduleId?.takeIf(String::isNotBlank)
                ?: UUID.randomUUID().toString()
        )

        offlineStore.enqueueSave(uid, normalized, isNew)
        scheduleAlarms(normalized)

        val outcome = runCatching {
            writeMedicationRemote(uid, normalized, isNew, WRITE_TIMEOUT_MS)
        }
        if (outcome.isSuccess) {
            offlineStore.completeSave(uid, normalized)
            withTimeoutOrNull(REFRESH_TIMEOUT_MS) {
                refreshTodayLogs(uid, normalized)
            }
        } else {
            val error = outcome.exceptionOrNull()
                ?: error("No se pudo guardar el medicamento.")
            if (!error.isOfflineFirestoreError()) {
                offlineStore.discardSave(uid, normalized.id.orEmpty())
                alarmScheduler.cancelForSchedule(normalized.scheduleId)
                throw error
            }
        }
    }

    override suspend fun ensureTodayDoses(): Result<Unit> = runCatching {
        withTimeoutOrNull(REFRESH_TIMEOUT_MS) {
            ensureTodayDosesInternal(uid())
        } ?: Unit
    }

    override suspend fun takeDose(dose: DoseLog, source: String): Result<Unit> = runCatching {
        val uid = uid()
        val logRef = doseLogsRef(uid).document(dose.id)
        val existing = logRef.get().await()
        if (existing.exists() && existing.getString("status") == DoseStatus.TAKEN) {
            alarmScheduler.cancelDoseNotification(dose.id)
            return@runCatching
        }

        val plantReference = plantRef(uid)
        val todayDate = LocalDate.now()
        val updatedPlant = plantReference.get().await()
            .toPlantState()
            .registerDoseTaken(todayDate)
        val scheduledAt = if (dose.scheduledAt > 0L) dose.scheduledAt else System.currentTimeMillis()
        val dateKey = dose.dateKey.ifBlank { todayDate.toString() }

        val medicationReference = medicationsRef(uid).document(dose.medicationId)
        val currentStock = runCatching {
            if (dose.medicationId.isBlank()) null
            else {
                val snapshot = medicationReference.get().await()
                if (snapshot.exists()) snapshot.getLong("stock")?.toInt() else null
            }
        }.getOrNull()

        val batch = firestore.batch()
        batch.set(
            logRef,
            dose.toFirestoreMap(
                scheduledMillis = scheduledAt,
                statusValue = DoseStatus.TAKEN,
                sourceValue = source,
                dateKeyValue = dateKey,
                takenTimestamp = Timestamp.now()
            ),
            SetOptions.merge()
        )
        batch.set(plantReference, updatedPlant.toFirestoreMap(), SetOptions.merge())
        if (currentStock != null && currentStock > 0) {
            batch.update(medicationReference, "stock", currentStock - 1)
        }
        batch.commit().await()
        alarmScheduler.cancelDoseNotification(dose.id)
    }

    override suspend fun isDoseTaken(logId: String): Boolean {
        if (logId.isBlank()) return false
        val uid = uidOrNull() ?: return false
        return runCatching {
            val snapshot = doseLogsRef(uid).document(logId).get().await()
            snapshot.exists() && snapshot.getString("status") == DoseStatus.TAKEN
        }.getOrDefault(false)
    }

    override suspend fun snoozeDose(dose: DoseLog, minutes: Long): Result<Unit> = runCatching {
        val uid = uid()
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        val dateKey = dose.dateKey.ifBlank { todayKey() }
        val logRef = doseLogsRef(uid).document(dose.id)

        firestore.batch().apply {
            set(
                logRef,
                dose.toFirestoreMap(
                    scheduledMillis = triggerAt,
                    statusValue = DoseStatus.PENDING,
                    sourceValue = "SNOOZE",
                    dateKeyValue = dateKey
                ),
                SetOptions.merge()
            )
        }.commit().await()

        alarmScheduler.cancelDoseNotification(dose.id)
        alarmScheduler.scheduleSnooze(dose.copy(scheduledAt = triggerAt, dateKey = dateKey), triggerAt)
    }

    override suspend fun deleteMedication(medicationId: String, scheduleId: String?): Result<Unit> = runCatching {
        val uid = uid()
        offlineStore.enqueueDelete(uid, medicationId, scheduleId)
        alarmScheduler.cancelForSchedule(scheduleId)

        val outcome = runCatching {
            deleteRemote(uid, PendingMedicationDelete(medicationId, scheduleId))
        }
        if (outcome.isSuccess) {
            offlineStore.completeDelete(uid, medicationId, scheduleId)
        } else {
            val error = outcome.exceptionOrNull() ?: error("No se pudo eliminar el medicamento.")
            if (!error.isOfflineFirestoreError()) {
                offlineStore.removePendingDelete(uid, medicationId)
                throw error
            }
        }
    }

    override fun observePendingChanges(): Flow<Int> {
        val uid = uidOrNull() ?: return flowOf(0)
        return offlineStore.observe(uid)
            .map { state -> state.pendingCount }
            .distinctUntilChanged()
    }

    override suspend fun syncPendingChanges() {
        val uid = uidOrNull() ?: return

        offlineStore.read(uid).pendingSaves.forEach { pending ->
            val synced = runCatching {
                writeMedicationRemote(uid, pending.draft, pending.isNew, SYNC_TIMEOUT_MS)
            }.isSuccess
            if (synced) {
                offlineStore.completeSave(uid, pending.draft)
                scheduleAlarms(pending.draft)
                withTimeoutOrNull(REFRESH_TIMEOUT_MS) {
                    refreshTodayLogs(uid, pending.draft)
                }
            }
        }

        offlineStore.read(uid).pendingDeletes.forEach { pending ->
            val deleted = runCatching { deleteRemote(uid, pending) }.isSuccess
            if (deleted) {
                alarmScheduler.cancelForSchedule(pending.scheduleId)
                offlineStore.completeDelete(uid, pending.medicationId, pending.scheduleId)
            }
        }
    }

    private suspend fun writeMedicationRemote(
        uid: String,
        draft: MedicationDraft,
        isNew: Boolean,
        timeoutMs: Long
    ) {
        val medicationId = draft.id?.takeIf(String::isNotBlank)
            ?: error("El medicamento no tiene identificador.")
        val scheduleId = draft.scheduleId?.takeIf(String::isNotBlank)
            ?: error("El horario no tiene identificador.")
        val medicationRef = medicationsRef(uid).document(medicationId)
        val scheduleRef = schedulesRef(uid).document(scheduleId)

        val completed = withTimeoutOrNull(timeoutMs) {
            val batch = firestore.batch()
            batch.set(medicationRef, draft.toMedicationFirestoreMap(isNew), SetOptions.merge())
            batch.set(scheduleRef, draft.toScheduleFirestoreMap(medicationRef.id), SetOptions.merge())
            batch.commit().await()
            true
        } ?: false

        if (!completed) throw OfflineWritePending()
    }

    private suspend fun deleteRemote(uid: String, pending: PendingMedicationDelete) {
        val completed = withTimeoutOrNull(DELETE_TIMEOUT_MS) {
            val todayLogs = doseLogsRef(uid)
                .whereEqualTo("dateKey", todayKey())
                .get()
                .await()

            val batch = firestore.batch()
            batch.delete(medicationsRef(uid).document(pending.medicationId))
            if (!pending.scheduleId.isNullOrBlank()) {
                batch.delete(schedulesRef(uid).document(pending.scheduleId))
            }
            todayLogs.documents
                .filter { it.getString("medicationId") == pending.medicationId }
                .filter { it.getString("status") != DoseStatus.TAKEN }
                .forEach { document ->
                    alarmScheduler.cancelDoseNotification(document.id)
                    batch.delete(document.reference)
                }
            batch.commit().await()
            true
        } ?: false

        if (!completed) throw OfflineWritePending()
    }

    private fun scheduleAlarms(draft: MedicationDraft) {
        runCatching {
            alarmScheduler.scheduleMedication(
                medicationId = draft.id.orEmpty(),
                medicationName = draft.name.trim(),
                dose = draft.dose.trim(),
                scheduleId = draft.scheduleId.orEmpty(),
                times = draft.times.distinct().sorted()
            )
        }
    }

    private suspend fun refreshTodayLogs(uid: String, draft: MedicationDraft) {
        ignoreFailure {
            syncTodayLogsForSchedule(
                uid = uid,
                scheduleId = draft.scheduleId.orEmpty(),
                times = draft.times.distinct().sorted(),
                medicationName = draft.name.trim(),
                dose = draft.dose.trim()
            )
        }
        ignoreFailure { ensureTodayDosesInternal(uid) }
    }

    private suspend fun ignoreFailure(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // El cambio ya está guardado en local; refrescar las tomas es secundario.
        }
    }

    private suspend fun syncTodayLogsForSchedule(
        uid: String,
        scheduleId: String,
        times: List<String>,
        medicationName: String,
        dose: String
    ) {
        val dateKey = todayKey()
        val newLogIds = times.map { time -> doseLogId(scheduleId, dateKey, time) }.toSet()
        val existing = doseLogsRef(uid)
            .whereEqualTo("dateKey", dateKey)
            .get()
            .await()
            .documents
            .filter { it.getString("scheduleId") == scheduleId }
            .filter { it.getString("status") != DoseStatus.TAKEN }

        if (existing.isEmpty()) return

        val batch = firestore.batch()
        var writes = 0
        existing.forEach { document ->
            if (document.id in newLogIds) {
                batch.update(
                    document.reference,
                    mapOf(
                        "medicationName" to medicationName,
                        "dose" to dose
                    )
                )
                writes++
            } else {
                alarmScheduler.cancelDoseNotification(document.id)
                batch.delete(document.reference)
                writes++
            }
        }
        if (writes > 0) {
            batch.commit().await()
        }
    }

    private suspend fun ensureTodayDosesInternal(uid: String) {
        val medications = medicationsRef(uid).get().await().toMedications()
            .filter { it.active }
        val schedules = schedulesRef(uid).get().await().toSchedules()
            .filter { it.enabled }
        val today = LocalDate.now()
        val dateKey = today.toString()
        val existingLogs = doseLogsRef(uid)
            .whereEqualTo("dateKey", dateKey)
            .get()
            .await()
            .documents
            .associateBy { it.id }

        val batch = firestore.batch()
        var writes = 0

        schedules.forEach { schedule ->
            val medication = medications.firstOrNull { it.id == schedule.medicationId } ?: return@forEach
            schedule.times.distinct().forEach { time ->
                val localTime = runCatching { LocalTime.parse(time) }.getOrNull() ?: return@forEach
                val logId = doseLogId(schedule.id, dateKey, time)
                if (existingLogs.containsKey(logId)) return@forEach

                val scheduledMoment = today.atTime(localTime)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                batch.set(
                    doseLogsRef(uid).document(logId),
                    mapOf(
                        "medicationId" to medication.id,
                        "scheduleId" to schedule.id,
                        "medicationName" to medication.name,
                        "dose" to medication.dose,
                        "scheduledAt" to Timestamp(Date(scheduledMoment)),
                        "status" to DoseStatus.PENDING,
                        "dateKey" to dateKey,
                        "source" to "APP"
                    )
                )
                writes++
            }
        }

        if (writes > 0) {
            batch.commit().await()
        }
    }

    private companion object {
        const val WRITE_TIMEOUT_MS = 6_000L
        const val SYNC_TIMEOUT_MS = 8_000L
        const val DELETE_TIMEOUT_MS = 6_000L
        const val REFRESH_TIMEOUT_MS = 8_000L
    }
}
