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
import org.json.JSONArray
import org.json.JSONObject

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
                preferences[medicationsKey] = encodeMedications(medications)
            }
            if (schedules != null) {
                preferences[schedulesKey] = encodeSchedules(schedules)
            }
        }
    }

    /** Encola un alta o edición antes de intentar la escritura remota. */
    suspend fun enqueueSave(uid: String, draft: MedicationDraft, isNew: Boolean) {
        editFor(uid) { preferences ->
            val pending = decodePendingSaves(preferences[pendingSavesKey])
                .filterNot { it.draft.id == draft.id } + PendingMedicationSave(draft, isNew)
            preferences[pendingSavesKey] = encodePendingSaves(pending)

            val deletes = decodePendingDeletes(preferences[pendingDeletesKey])
                .filterNot { it.medicationId == draft.id }
            preferences[pendingDeletesKey] = encodePendingDeletes(deletes)
        }
    }

    /** Confirma un guardado: sale de la cola y pasa a la instantánea local. */
    suspend fun completeSave(uid: String, draft: MedicationDraft) {
        editFor(uid) { preferences ->
            val pending = decodePendingSaves(preferences[pendingSavesKey])
                .filterNot { it.draft.id == draft.id }
            preferences[pendingSavesKey] = encodePendingSaves(pending)

            val medication = draft.toMedicationModel()
            val medications = decodeMedications(preferences[medicationsKey])
                .filterNot { it.id == medication.id } + medication
            preferences[medicationsKey] = encodeMedications(medications)

            draft.toScheduleModel()?.let { schedule ->
                val schedules = decodeSchedules(preferences[schedulesKey])
                    .filterNot {
                        it.id == schedule.id || it.medicationId == schedule.medicationId
                    } + schedule
                preferences[schedulesKey] = encodeSchedules(schedules)
            }
        }
    }

    /** Descarta un guardado local que Firestore rechazó por un error no recuperable. */
    suspend fun discardSave(uid: String, medicationId: String) {
        editFor(uid) { preferences ->
            val pending = decodePendingSaves(preferences[pendingSavesKey])
                .filterNot { it.draft.id == medicationId }
            preferences[pendingSavesKey] = encodePendingSaves(pending)
        }
    }

    /** Encola un borrado antes de intentar la escritura remota. */
    suspend fun enqueueDelete(uid: String, medicationId: String, scheduleId: String?) {
        editFor(uid) { preferences ->
            val pending = decodePendingSaves(preferences[pendingSavesKey])
                .filterNot { it.draft.id == medicationId }
            preferences[pendingSavesKey] = encodePendingSaves(pending)

            val deletes = decodePendingDeletes(preferences[pendingDeletesKey])
                .filterNot { it.medicationId == medicationId } +
                PendingMedicationDelete(medicationId, scheduleId)
            preferences[pendingDeletesKey] = encodePendingDeletes(deletes)
        }
    }

    /** Confirma un borrado y lo elimina también de la instantánea. */
    suspend fun completeDelete(uid: String, medicationId: String, scheduleId: String?) {
        editFor(uid) { preferences ->
            val deletes = decodePendingDeletes(preferences[pendingDeletesKey])
                .filterNot { it.medicationId == medicationId }
            preferences[pendingDeletesKey] = encodePendingDeletes(deletes)

            preferences[medicationsKey] = encodeMedications(
                decodeMedications(preferences[medicationsKey])
                    .filterNot { it.id == medicationId }
            )
            preferences[schedulesKey] = encodeSchedules(
                decodeSchedules(preferences[schedulesKey]).filterNot {
                    it.medicationId == medicationId ||
                        (!scheduleId.isNullOrBlank() && it.id == scheduleId)
                }
            )
        }
    }

    suspend fun removePendingDelete(uid: String, medicationId: String) {
        editFor(uid) { preferences ->
            val deletes = decodePendingDeletes(preferences[pendingDeletesKey])
                .filterNot { it.medicationId == medicationId }
            preferences[pendingDeletesKey] = encodePendingDeletes(deletes)
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
            medications = decodeMedications(this[medicationsKey]),
            schedules = decodeSchedules(this[schedulesKey]),
            pendingSaves = decodePendingSaves(this[pendingSavesKey]),
            pendingDeletes = decodePendingDeletes(this[pendingDeletesKey])
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

private fun encodeMedications(medications: List<Medication>): String =
    JSONArray().apply {
        medications.forEach { medication ->
            put(
                JSONObject().apply {
                    put("id", medication.id)
                    put("name", medication.name)
                    put("dose", medication.dose)
                    put("form", medication.form)
                    put("stock", medication.stock)
                    put("active", medication.active)
                    put("createdAt", medication.createdAt)
                }
            )
        }
    }.toString()

private fun decodeMedications(raw: String?): List<Medication> {
    val array = runCatching { JSONArray(raw.orEmpty()) }.getOrNull() ?: return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id")
            if (id.isBlank()) continue
            add(
                Medication(
                    id = id,
                    name = item.optString("name"),
                    dose = item.optString("dose"),
                    form = item.optString("form", "Tableta"),
                    stock = item.optInt("stock"),
                    active = item.optBoolean("active", true),
                    createdAt = item.optLong("createdAt")
                )
            )
        }
    }
}

private fun encodeSchedules(schedules: List<Schedule>): String =
    JSONArray().apply {
        schedules.forEach { schedule ->
            put(
                JSONObject().apply {
                    put("id", schedule.id)
                    put("medicationId", schedule.medicationId)
                    put("times", JSONArray(schedule.times))
                    put("daysMask", schedule.daysMask)
                    put("anchor", schedule.anchor)
                    put("enabled", schedule.enabled)
                }
            )
        }
    }.toString()

private fun decodeSchedules(raw: String?): List<Schedule> {
    val array = runCatching { JSONArray(raw.orEmpty()) }.getOrNull() ?: return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id")
            val medicationId = item.optString("medicationId")
            if (id.isBlank() || medicationId.isBlank()) continue
            add(
                Schedule(
                    id = id,
                    medicationId = medicationId,
                    times = item.optJSONArray("times").toStringList(),
                    daysMask = item.optInt("daysMask", 127),
                    anchor = item.optString("anchor", "Sin rutina"),
                    enabled = item.optBoolean("enabled", true)
                )
            )
        }
    }
}

private fun encodePendingSaves(pending: List<PendingMedicationSave>): String =
    JSONArray().apply {
        pending.forEach { save ->
            val draft = save.draft
            put(
                JSONObject().apply {
                    put("id", draft.id ?: JSONObject.NULL)
                    put("scheduleId", draft.scheduleId ?: JSONObject.NULL)
                    put("name", draft.name)
                    put("dose", draft.dose)
                    put("form", draft.form)
                    put("times", JSONArray(draft.times))
                    put("stock", draft.stock)
                    put("anchor", draft.anchor)
                    put("isNew", save.isNew)
                }
            )
        }
    }.toString()

private fun decodePendingSaves(raw: String?): List<PendingMedicationSave> {
    val array = runCatching { JSONArray(raw.orEmpty()) }.getOrNull() ?: return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id").takeUnless { it.isBlank() || it == "null" }
            if (id == null) continue
            val draft = MedicationDraft(
                id = id,
                scheduleId = item.optString("scheduleId")
                    .takeUnless { it.isBlank() || it == "null" },
                name = item.optString("name"),
                dose = item.optString("dose"),
                form = item.optString("form", "Tableta"),
                times = item.optJSONArray("times").toStringList(),
                stock = item.optInt("stock"),
                anchor = item.optString("anchor", "Sin rutina")
            )
            add(PendingMedicationSave(draft, item.optBoolean("isNew", true)))
        }
    }
}

private fun encodePendingDeletes(pending: List<PendingMedicationDelete>): String =
    JSONArray().apply {
        pending.forEach { delete ->
            put(
                JSONObject().apply {
                    put("medicationId", delete.medicationId)
                    put("scheduleId", delete.scheduleId ?: JSONObject.NULL)
                }
            )
        }
    }.toString()

private fun decodePendingDeletes(raw: String?): List<PendingMedicationDelete> {
    val array = runCatching { JSONArray(raw.orEmpty()) }.getOrNull() ?: return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val medicationId = item.optString("medicationId")
            if (medicationId.isBlank()) continue
            add(
                PendingMedicationDelete(
                    medicationId = medicationId,
                    scheduleId = item.optString("scheduleId")
                        .takeUnless { it.isBlank() || it == "null" }
                )
            )
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val value = optString(index)
            if (value.isNotBlank()) add(value)
        }
    }
}
