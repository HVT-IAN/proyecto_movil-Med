package com.meds.app.data.model

data class Medication(
    val id: String = "",
    val name: String = "",
    val dose: String = "",
    val form: String = "Tableta",
    val stock: Int = 0,
    val active: Boolean = true,
    val createdAt: Long = 0L
)

data class Schedule(
    val id: String = "",
    val medicationId: String = "",
    val times: List<String> = emptyList(),
    val daysMask: Int = 127,
    val anchor: String = "Sin rutina",
    val enabled: Boolean = true
)

data class DoseLog(
    val id: String = "",
    val medicationId: String = "",
    val scheduleId: String = "",
    val medicationName: String = "",
    val dose: String = "",
    val scheduledAt: Long = 0L,
    val takenAt: Long? = null,
    val status: String = DoseStatus.PENDING,
    val source: String = "APP",
    val dateKey: String = ""
)

object DoseStatus {
    const val PENDING = "PENDING"
    const val TAKEN = "TAKEN"
    const val SNOOZED = "SNOOZED"
    const val SKIPPED = "SKIPPED"
}

data class PlantState(
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    val freezeAvailable: Int = 1,
    val lastTakenDate: String = "",
    val totalTaken: Int = 0
)

data class MedicationDraft(
    val id: String? = null,
    val scheduleId: String? = null,
    val name: String = "",
    val dose: String = "",
    val form: String = "Tableta",
    val times: List<String> = emptyList(),
    val stock: Int = 0,
    val anchor: String = "Sin rutina"
)

data class CompanionLink(
    val uid: String = "",
    val name: String = "",
    val linkedAt: Long = 0L,
    val inviteCode: String = ""
)

data class LinkedPatient(
    val uid: String = "",
    val name: String = "",
    val linkedAt: Long = 0L,
    val inviteCode: String = ""
)

data class Nudge(
    val id: String = "",
    val fromUid: String = "",
    val fromName: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val read: Boolean = false
)

data class PairingInvite(
    val code: String = "",
    val patientUid: String = "",
    val patientName: String = "",
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val used: Boolean = false
)

data class CompanionDay(
    val patientName: String = "",
    val medications: List<Medication> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val doses: List<DoseLog> = emptyList(),
    val plant: PlantState = PlantState()
)

fun doseLogId(scheduleId: String, dateKey: String, time: String): String {
    return "${scheduleId}_${dateKey}_${time.replace(":", "")}"
}
