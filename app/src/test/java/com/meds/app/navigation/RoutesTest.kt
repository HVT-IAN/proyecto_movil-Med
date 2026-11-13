package com.meds.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutesTest {

    @Test
    fun defineRutasDeNivelSuperiorDelPaciente() {
        assertEquals(
            setOf("home", "medications", "garden", "settings"),
            Routes.Patient.topLevelDestinations
        )
    }

    @Test
    fun elEditorDeclaraArgumentoDeMedicamento() {
        assertEquals("medication_editor/{medicationId}", Routes.Patient.EDITOR_PATTERN)
        assertEquals("medicationId", Routes.Patient.ARG_MEDICATION_ID)
        assertTrue(Routes.Patient.MEDICATION_INFO_PATTERN.contains("query={query}"))
    }

    @Test
    fun elResultadoDeSeleccionTieneClavePropia() {
        assertEquals(
            "selected_medication_name",
            Routes.Patient.RESULT_SELECTED_MEDICATION_NAME
        )
    }
}
