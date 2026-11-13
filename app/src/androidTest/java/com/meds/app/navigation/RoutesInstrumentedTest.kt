package com.meds.app.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoutesInstrumentedTest {

    @Test
    fun codificaElIdentificadorDelMedicamento() {
        assertEquals(
            "medication_editor/medicamento%20con%20espacio",
            Routes.Patient.medicationEditor("medicamento con espacio")
        )
    }

    @Test
    fun codificaElArgumentoDeConsultaCima() {
        assertEquals(
            "medication_info?query=ibuprofeno%20200",
            Routes.Patient.medicationInfo("ibuprofeno 200")
        )
    }
}
