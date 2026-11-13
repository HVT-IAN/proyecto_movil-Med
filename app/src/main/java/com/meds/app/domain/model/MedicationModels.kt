package com.meds.app.domain.model

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
