package com.meds.app.core.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun aceptaCorreosValidos() {
        assertTrue(isValidEmail("paciente@correo.com"))
        assertTrue(isValidEmail("nombre.apellido+etiqueta@dominio.mx"))
        assertTrue(isValidEmail("  usuario@correo.es  "))
    }

    @Test
    fun rechazaCorreosInvalidos() {
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("sin-arroba.com"))
        assertFalse(isValidEmail("usuario@"))
        assertFalse(isValidEmail("@correo.com"))
        assertFalse(isValidEmail("usuario@correo"))
        assertFalse(isValidEmail("usuario con espacio@correo.com"))
    }
}
