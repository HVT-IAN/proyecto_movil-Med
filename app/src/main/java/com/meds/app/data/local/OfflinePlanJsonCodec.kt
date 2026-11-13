package com.meds.app.data.local

import com.meds.app.domain.model.Medication
import com.meds.app.domain.model.MedicationDraft
import com.meds.app.domain.model.Schedule
import org.json.JSONArray
import org.json.JSONObject

/**
 * Codificador/decodificador JSON de las estructuras guardadas en [OfflineMedicationStore].
 * Mantener el formato de serialización aislado permite que [OfflineMedicationStore]
 * se enfoque únicamente en la gestión de estado y colas en DataStore.
 */
internal object OfflinePlanJsonCodec {

    fun encodeMedications(medications: List<Medication>): String =
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

    fun decodeMedications(raw: String?): List<Medication> {
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

    fun encodeSchedules(schedules: List<Schedule>): String =
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

    fun decodeSchedules(raw: String?): List<Schedule> {
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

    fun encodePendingSaves(pending: List<PendingMedicationSave>): String =
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

    fun decodePendingSaves(raw: String?): List<PendingMedicationSave> {
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

    fun encodePendingDeletes(pending: List<PendingMedicationDelete>): String =
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

    fun decodePendingDeletes(raw: String?): List<PendingMedicationDelete> {
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
}
