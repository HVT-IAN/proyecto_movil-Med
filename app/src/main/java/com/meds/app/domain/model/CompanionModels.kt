package com.meds.app.domain.model

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
