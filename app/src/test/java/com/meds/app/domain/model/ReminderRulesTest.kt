package com.meds.app.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderRulesTest {

    private fun minute(hour: Int, minute: Int = 0): Int = hour * 60 + minute

    @Test
    fun `rango normal dentro y fuera`() {
        assertTrue(ReminderRules.isQuiet(minute(14), minute(13), minute(15)))
        assertFalse(ReminderRules.isQuiet(minute(15), minute(13), minute(15)))
        assertFalse(ReminderRules.isQuiet(minute(12), minute(13), minute(15)))
    }

    @Test
    fun `rango que cruza medianoche`() {
        assertTrue(ReminderRules.isQuiet(minute(23), minute(22), minute(7)))
        assertTrue(ReminderRules.isQuiet(minute(3), minute(22), minute(7)))
        assertFalse(ReminderRules.isQuiet(minute(8), minute(22), minute(7)))
        assertFalse(ReminderRules.isQuiet(minute(21), minute(22), minute(7)))
    }

    @Test
    fun `inicio igual a fin no es silencio`() {
        assertFalse(ReminderRules.isQuiet(minute(10), minute(8), minute(8)))
    }

    @Test
    fun `master apagado no notifica`() {
        val settings = ReminderSettings(masterEnabled = false)
        assertFalse(ReminderRules.shouldNotify(settings, "med-1", minute(10)))
    }

    @Test
    fun `medicamento apagado no notifica`() {
        val settings = ReminderSettings(disabledMedicationIds = setOf("med-1"))
        assertFalse(ReminderRules.shouldNotify(settings, "med-1", minute(10)))
        assertTrue(ReminderRules.shouldNotify(settings, "med-2", minute(10)))
    }

    @Test
    fun `silencio apagado no bloquea`() {
        val settings = ReminderSettings(
            quietEnabled = false,
            quietStartMinutes = minute(13),
            quietEndMinutes = minute(15)
        )
        assertTrue(ReminderRules.shouldNotify(settings, "med-1", minute(14)))
    }

    @Test
    fun `silencio encendido bloquea dentro del rango`() {
        val settings = ReminderSettings(
            quietEnabled = true,
            quietStartMinutes = minute(13),
            quietEndMinutes = minute(15)
        )
        assertFalse(ReminderRules.shouldNotify(settings, "med-1", minute(14)))
        assertTrue(ReminderRules.shouldNotify(settings, "med-1", minute(16)))
    }
}
