package com.meds.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.meds.app.domain.model.Medication
import com.meds.app.domain.model.MedicationDraft
import com.meds.app.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.medsOfflineDataStore by preferencesDataStore(name = "meds_offline_cache")

/** Cambio local de un medicamento que todavía no ha confirmado Firestore. */
data class PendingMedicationSave(
    val draft: MedicationDraft,
    val isNew: Boolean
)

/** Borrado local de un medicamento que todavía no ha confirmado Firestore. */
data class PendingMedicationDelete(
    val medicationId: String,
    val scheduleId: String?
)

/**
 * Copia local del plan del paciente y cola de cambios pendientes.
 *
 * La instantánea permite mostrar medicamentos y horarios sin conexión o tras
 * cerrar la app. Los cambios pendientes se aplican por encima de la instantánea
 * hasta que Firestore los confirma, de modo que la interfaz nunca pierde un
 * guardado hecho sin red.
 */
data class OfflinePlanState(
    val medications: List<Medication> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val pendingSaves: List<PendingMedicationSave> = emptyList(),
    val pendingDeletes: List<PendingMedicationDelete> = emptyList()
) {
    val pendingCount: Int
        get() = pendingSaves.size + pendingDeletes.size

    fun pendingMedications(): List<Medication> =
        pendingSaves.map { pending -> pending.draft.toMedicationModel() }

    fun pendingSchedules(): List<Schedule> =
        pendingSaves.mapNotNull { pending -> pending.draft.toScheduleModel() }

    fun pendingMedicationIds(): Set<String> =
        pendingSaves.mapNotNull { it.draft.id?.takeIf(String::isNotBlank) }.toSet()

    fun deletedMedicationIds(): Set<String> =
        pendingDeletes.map { it.medicationId }.toSet()

    fun mergeMedications(remote: List<Medication>): List<Medication> {
        val deleted = deletedMedicationIds()
        val pending = pendingMedicationIds()
        return (remote.filter { it.id !in deleted && it.id !in pending } + pendingMedications())
            .distinctBy { it.id }
    }

    fun mergeSchedules(remote: List<Schedule>): List<Schedule> {
        val deleted = deletedMedicationIds()
        val pending = pendingMedicationIds()
        return (remote.filter {
            it.medicationId !in deleted && it.medicationId !in pending
        } + pendingSchedules()).distinctBy { it.id }
    }
}

class OfflineMedicationStore(private val context: Context) {
    private val ownerKey = stringPreferencesKey("owner_uid")
    private val medicationsKey = stringPreferencesKey("medications")
    private val schedulesKey = stringPreferencesKey("schedules")
    private val pendingSavesKey = stringPreferencesKey("pending_saves")
    private val pendingDeletesKey = stringPreferencesKey("pending_deletes")

    /** Instantánea y cola del usuario indicado; vacía si la caché es de otro usuario. */
    fun observe(uid: String): Flow<OfflinePlanState> =
        context.medsOfflineDataStore.data
            .map { preferences -> preferences.toPlanState(uid) }
            .distinctUntilChanged()

    suspend fun read(uid: String): OfflinePlanState =
        context.medsOfflineDataStore.data.first().toPlanState(uid)

    /** Guarda la última lista confirmada por Firestore. */
    suspend fun writeSnapshot(
        uid: String,
        medications: List<Medication>? = null,
        schedules: List<Schedule>? = null
    ) {
        editFor(uid) { preferences ->
            if (medications != null) {
                preferences[medicationsKey] = OfflinePlanJsonCodec.encodeMedications(medications)
            }
            if (schedules != null) {
                preferences[schedulesKey] = OfflinePlanJsonCodec.encodeSchedules(schedules)
            }
        }
    }

    /** Encola un alta o edición antes de intentar la escritura remota. */
    suspend fun enqueueSave(uid: String, draft: MedicationDraft, isNew: Boolean) {
        editFor(uid) { preferences ->
            val pending = OfflinePlanJsonCodec.decodePendingSaves(preferences[pendingSavesKey])
                .filterNot { it.draft.id == draft.id } + PendingMedicationSave(draft, isNew)
            preferences[pendingSavesKey] = OfflinePlanJsonCodec.encodePendingSaves(pending)

            val deletes = OfflinePlanJsonCodec.decodePendingDeletes(preferences[pendingDeletesKey])
                .filterNot { it.medicationId == draft.id }
            preferences[pendingDeletesKey] = OfflinePlanJsonCodec.encodePendingDeletes(deletes)
        }
    }

    /** Confirma un guardado: sale de la cola y pasa a la instantánea local. */
    suspend fun completeSave(uid: String, draft: MedicationDraft) {
        editFor(uid) { preferences ->
            val pending = OfflinePlanJsonCodec.decodePendingSaves(preferences[pendingSavesKey])
                .filterNot { it.draft.id == draft.id }
            preferences[pendingSavesKey] = OfflinePlanJsonCodec.encodePendingSaves(pending)

            val medication = draft.toMedicationModel()
            val medications = OfflinePlanJsonCodec.decodeMedications(preferences[medicationsKey])
                .filterNot { it.id == medication.id } + medication
            preferences[medicationsKey] = OfflinePlanJsonCodec.encodeMedications(medications)

            draft.toScheduleModel()?.let { schedule ->
                val schedules = OfflinePlanJsonCodec.decodeSchedules(preferences[schedulesKey])
                    .filterNot {
                        it.id == schedule.id || it.medicationId == schedule.medicationId
                    } + schedule
                preferences[schedulesKey] = OfflinePlanJsonCodec.encodeSchedules(schedules)
            }
        }
    }

    /** Descarta un guardado local que Firestore rechazó por un error no recuperable. */
    suspend fun discardSave(uid: String, medicationId: String) {
        editFor(uid) { preferences ->
            val pending = OfflinePlanJsonCodec.decodePendingSaves(preferences[pendingSavesKey])
                .filterNot { it.draft.id == medicationId }
            preferences[pendingSavesKey] = OfflinePlanJsonCodec.encodePendingSaves(pending)
        }
    }

    /** Encola un borrado antes de intentar la escritura remota. */
    suspend fun enqueueDelete(uid: String, medicationId: String, scheduleId: String?) {
        editFor(uid) { preferences ->
            val pending = OfflinePlanJsonCodec.decodePendingSaves(preferences[pendingSavesKey])
                .filterNot { it.draft.id == medicationId }
            preferences[pendingSavesKey] = OfflinePlanJsonCodec.encodePendingSaves(pending)

            val deletes = OfflinePlanJsonCodec.decodePendingDeletes(preferences[pendingDeletesKey])
                .filterNot { it.medicationId == medicationId } +
                PendingMedicationDelete(medicationId, scheduleId)
            preferences[pendingDeletesKey] = OfflinePlanJsonCodec.encodePendingDeletes(deletes)
        }
    }

    /** Confirma un borrado y lo elimina también de la instantánea. */
    suspend fun completeDelete(uid: String, medicationId: String, scheduleId: String?) {
        editFor(uid) { preferences ->
            val deletes = OfflinePlanJsonCodec.decodePendingDeletes(preferences[pendingDeletesKey])
                .filterNot { it.medicationId == medicationId }
            preferences[pendingDeletesKey] = OfflinePlanJsonCodec.encodePendingDeletes(deletes)

            preferences[medicationsKey] = OfflinePlanJsonCodec.encodeMedications(
                OfflinePlanJsonCodec.decodeMedications(preferences[medicationsKey])
                    .filterNot { it.id == medicationId }
            )
            preferences[schedulesKey] = OfflinePlanJsonCodec.encodeSchedules(
                OfflinePlanJsonCodec.decodeSchedules(preferences[schedulesKey]).filterNot {
                    it.medicationId == medicationId ||
                        (!scheduleId.isNullOrBlank() && it.id == scheduleId)
                }
            )
        }
    }

    suspend fun removePendingDelete(uid: String, medicationId: String) {
        editFor(uid) { preferences ->
            val deletes = OfflinePlanJsonCodec.decodePendingDeletes(preferences[pendingDeletesKey])
                .filterNot { it.medicationId == medicationId }
            preferences[pendingDeletesKey] = OfflinePlanJsonCodec.encodePendingDeletes(deletes)
        }
    }

    suspend fun clearAll() {
        context.medsOfflineDataStore.edit { preferences -> preferences.clear() }
    }

    private suspend fun editFor(uid: String, block: (MutablePreferences) -> Unit) {
        context.medsOfflineDataStore.edit { preferences ->
            if (preferences[ownerKey] != uid) {
                preferences.clear()
                preferences[ownerKey] = uid
            }
            block(preferences)
        }
    }

    private fun Preferences.toPlanState(uid: String): OfflinePlanState {
        if (this[ownerKey] != uid) return OfflinePlanState()
        return OfflinePlanState(
            medications = OfflinePlanJsonCodec.decodeMedications(this[medicationsKey]),
            schedules = OfflinePlanJsonCodec.decodeSchedules(this[schedulesKey]),
            pendingSaves = OfflinePlanJsonCodec.decodePendingSaves(this[pendingSavesKey]),
            pendingDeletes = OfflinePlanJsonCodec.decodePendingDeletes(this[pendingDeletesKey])
        )
    }
}

internal fun MedicationDraft.toMedicationModel(createdAt: Long = 0L): Medication = Medication(
    id = id.orEmpty(),
    name = name.trim(),
    dose = dose.trim(),
    form = form,
    stock = stock,
    active = true,
    createdAt = createdAt
)

internal fun MedicationDraft.toScheduleModel(): Schedule? {
    val medicationId = id.orEmpty()
    if (medicationId.isBlank() || times.isEmpty()) return null
    return Schedule(
        id = scheduleId.orEmpty(),
        medicationId = medicationId,
        times = times.distinct().sorted(),
        daysMask = 127,
        anchor = anchor,
        enabled = true
    )
}
