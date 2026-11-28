package com.meds.app.core.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationSearchTest {

    @Test
    fun `normaliza mayusculas y acentos`() {
        assertTrue(medicationMatchesQuery("Metilfenidato", "18 mg", "Cápsula", "metilfenidato"))
        assertTrue(medicationMatchesQuery("Metilfenidato", "18 mg", "Cápsula", "CAPSULA"))
    }

    @Test
    fun `encuentra por dosis y forma`() {
        assertTrue(medicationMatchesQuery("Metilfenidato", "18 mg", "Cápsula", "18"))
        assertTrue(medicationMatchesQuery("Metilfenidato", "18 mg", "Cápsula", "caps"))
    }

    @Test
    fun `consulta vacia coincide con todo`() {
        assertTrue(medicationMatchesQuery("Metilfenidato", "18 mg", "Cápsula", ""))
        assertTrue(medicationMatchesQuery("Metilfenidato", "18 mg", "Cápsula", "   "))
    }

    @Test
    fun `no coincide cuando no hay relacion`() {
        assertFalse(medicationMatchesQuery("Metilfenidato", "18 mg", "Cápsula", "ibuprofeno"))
    }
}
