package com.meds.app.presentation.patient.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class QuickAddMedicationSheetTest {

    @Test
    fun ahoraFormateaHoraActualEn24Horas() {
        val sampleNow = LocalTime.of(9, 5)
        assertEquals("09:05", resolveQuickTime(QuickTimeOption.NOW, now = sampleNow))
    }

    @Test
    fun chipsPredefinidosMapeanAHorasFijas() {
        assertEquals("08:00", resolveQuickTime(QuickTimeOption.MORNING))
        assertEquals("14:00", resolveQuickTime(QuickTimeOption.NOON))
        assertEquals("21:00", resolveQuickTime(QuickTimeOption.NIGHT))
    }

    @Test
    fun otraUsaHoraPersonalizadaODevuelveNullSiNoHay() {
        assertNull(resolveQuickTime(QuickTimeOption.OTHER, customTime = null))
        assertNull(resolveQuickTime(QuickTimeOption.OTHER, customTime = "   "))
        assertEquals("07:30", resolveQuickTime(QuickTimeOption.OTHER, customTime = "7:30"))
        assertEquals("18:45", resolveQuickTime(QuickTimeOption.OTHER, customTime = "18:45"))
    }
}
