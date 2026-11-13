package com.meds.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.meds.app.domain.model.CompanionLink
import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.DoseStatus
import com.meds.app.domain.model.LinkedPatient
import com.meds.app.domain.model.Medication
import com.meds.app.domain.model.MedicationDraft
import com.meds.app.domain.model.Nudge
import com.meds.app.domain.model.PairingInvite
import com.meds.app.domain.model.PlantState
import com.meds.app.domain.model.Schedule
import java.util.Date

fun DocumentSnapshot.toMedication(): Medication? {
    if (!exists()) return null
    return Medication(
        id = id,
        name = getString("name").orEmpty(),
        dose = getString("dose").orEmpty(),
        form = getString("form") ?: "Tableta",
        stock = getLong("stock")?.toInt() ?: 0,
        active = getBoolean("active") ?: true,
        createdAt = getTimestamp("createdAt")?.toDate()?.time ?: 0L
    )
}

fun DocumentSnapshot.toSchedule(): Schedule? {
    if (!exists()) return null
    val times = (get("times") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
    return Schedule(
        id = id,
        medicationId = getString("medicationId").orEmpty(),
        times = times,
        daysMask = getLong("daysMask")?.toInt() ?: 127,
        anchor = getString("anchor") ?: "Sin rutina",
        enabled = getBoolean("enabled") ?: true
    )
}

fun DocumentSnapshot.toDoseLog(): DoseLog? {
    if (!exists()) return null
    return DoseLog(
        id = id,
        medicationId = getString("medicationId").orEmpty(),
        scheduleId = getString("scheduleId").orEmpty(),
        medicationName = getString("medicationName").orEmpty(),
        dose = getString("dose").orEmpty(),
        scheduledAt = getTimestamp("scheduledAt")?.toDate()?.time ?: 0L,
        takenAt = getTimestamp("takenAt")?.toDate()?.time,
        status = getString("status") ?: DoseStatus.PENDING,
        source = getString("source") ?: "APP",
        dateKey = getString("dateKey").orEmpty()
    )
}

fun DocumentSnapshot.toPlantState(): PlantState {
    if (!exists()) return PlantState()
    return PlantState(
        level = getLong("level")?.toInt() ?: 1,
        xp = getLong("xp")?.toInt() ?: 0,
        streak = getLong("streak")?.toInt() ?: 0,
        freezeAvailable = getLong("freezeAvailable")?.toInt() ?: 1,
        lastTakenDate = getString("lastTakenDate").orEmpty(),
        totalTaken = getLong("totalTaken")?.toInt() ?: 0
    )
}

fun DocumentSnapshot.toCompanionLink(): CompanionLink? {
    if (!exists()) return null
    return CompanionLink(
        uid = id,
        name = getString("name").orEmpty(),
        linkedAt = getTimestamp("linkedAt")?.toDate()?.time ?: 0L,
        inviteCode = getString("inviteCode").orEmpty()
    )
}

fun DocumentSnapshot.toLinkedPatient(): LinkedPatient? {
    if (!exists()) return null
    return LinkedPatient(
        uid = id,
        name = getString("name").orEmpty(),
        linkedAt = getTimestamp("linkedAt")?.toDate()?.time ?: 0L,
        inviteCode = getString("inviteCode").orEmpty()
    )
}

fun DocumentSnapshot.toNudge(): Nudge? {
    if (!exists()) return null
    return Nudge(
        id = id,
        fromUid = getString("fromUid").orEmpty(),
        fromName = getString("fromName").orEmpty(),
        message = getString("message").orEmpty(),
        createdAt = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        read = getBoolean("read") ?: false
    )
}

fun DocumentSnapshot.toPairingInvite(): PairingInvite? {
    if (!exists()) return null
    return PairingInvite(
        code = id,
        patientUid = getString("patientUid").orEmpty(),
        patientName = getString("patientName").orEmpty(),
        createdAt = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        expiresAt = getTimestamp("expiresAt")?.toDate()?.time ?: 0L,
        used = getBoolean("used") ?: false
    )
}

fun QuerySnapshot.toMedications(): List<Medication> =
    documents.mapNotNull { it.toMedication() }

fun QuerySnapshot.toSchedules(): List<Schedule> =
    documents.mapNotNull { it.toSchedule() }

fun QuerySnapshot.toDoseLogs(): List<DoseLog> =
    documents.mapNotNull { it.toDoseLog() }

fun QuerySnapshot.toCompanionLinks(): List<CompanionLink> =
    documents.mapNotNull { it.toCompanionLink() }

fun QuerySnapshot.toLinkedPatients(): List<LinkedPatient> =
    documents.mapNotNull { it.toLinkedPatient() }

fun QuerySnapshot.toNudges(): List<Nudge> =
    documents.mapNotNull { it.toNudge() }

internal fun MedicationDraft.toMedicationFirestoreMap(isNew: Boolean): Map<String, Any> =
    buildMap {
        put("name", name.trim())
        put("dose", dose.trim())
        put("form", form)
        put("stock", stock)
        put("active", true)
        if (isNew) {
            put("createdAt", FieldValue.serverTimestamp())
        }
    }

internal fun MedicationDraft.toScheduleFirestoreMap(medicationId: String): Map<String, Any> =
    mapOf(
        "medicationId" to medicationId,
        "times" to times.distinct().sorted(),
        "daysMask" to 127,
        "anchor" to anchor,
        "enabled" to true
    )

internal fun PlantState.toFirestoreMap(): Map<String, Any> =
    mapOf(
        "level" to level,
        "xp" to xp,
        "streak" to streak,
        "freezeAvailable" to freezeAvailable,
        "lastTakenDate" to lastTakenDate,
        "totalTaken" to totalTaken
    )

internal fun DoseLog.toFirestoreMap(
    scheduledMillis: Long,
    statusValue: String,
    sourceValue: String,
    dateKeyValue: String,
    takenTimestamp: Timestamp? = null
): Map<String, Any> = buildMap {
    put("medicationId", medicationId)
    put("scheduleId", scheduleId)
    put("medicationName", medicationName)
    put("dose", dose)
    put("scheduledAt", Timestamp(Date(scheduledMillis)))
    if (takenTimestamp != null) {
        put("takenAt", takenTimestamp)
    }
    put("status", statusValue)
    put("source", sourceValue)
    put("dateKey", dateKeyValue)
}
