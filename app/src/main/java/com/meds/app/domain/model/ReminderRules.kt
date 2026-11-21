package com.meds.app.domain.model

/** Reglas puras de silencio y gating. No dependen de Android. */
object ReminderRules {
    private const val MINUTES_PER_DAY = 24 * 60

    private fun normalize(minutes: Int): Int =
        ((minutes % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY

    /**
     * `start == end` significa "sin rango" y devuelve false.
     * Si `start > end`, el rango cruza medianoche.
     */
    fun isQuiet(minuteOfDay: Int, startMinutes: Int, endMinutes: Int): Boolean {
        val minute = normalize(minuteOfDay)
        val start = normalize(startMinutes)
        val end = normalize(endMinutes)
        if (start == end) return false
        return if (start < end) {
            minute >= start && minute < end
        } else {
            minute >= start || minute < end
        }
    }

    fun shouldNotify(
        settings: ReminderSettings,
        medicationId: String,
        minuteOfDay: Int
    ): Boolean {
        if (!settings.masterEnabled) return false
        if (!settings.isMedicationEnabled(medicationId)) return false
        if (
            settings.quietEnabled &&
            isQuiet(
                minuteOfDay = minuteOfDay,
                startMinutes = settings.quietStartMinutes,
                endMinutes = settings.quietEndMinutes
            )
        ) {
            return false
        }
        return true
    }
}
