package com.meds.app.domain.model

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

fun doseLogId(scheduleId: String, dateKey: String, time: String): String {
    return "${scheduleId}_${dateKey}_${time.replace(":", "")}"
}
