package com.meds.app.domain.model

/**
 * Preferencias locales de recordatorios.
 *
 * `disabledMedicationIds` guarda los medicamentos con avisos apagados; por
 * defecto todos están activos. Las horas de silencio se expresan en minutos
 * desde medianoche y pueden cruzar el cambio de día.
 */
data class ReminderSettings(
    val masterEnabled: Boolean = true,
    val disabledMedicationIds: Set<String> = emptySet(),
    val quietEnabled: Boolean = false,
    val quietStartMinutes: Int = 22 * 60,
    val quietEndMinutes: Int = 7 * 60,
    val soundEnabled: Boolean = true
) {
    fun isMedicationEnabled(medicationId: String): Boolean =
        medicationId.isBlank() || medicationId !in disabledMedicationIds

    fun shouldSchedule(medicationId: String): Boolean =
        masterEnabled && isMedicationEnabled(medicationId)
}
