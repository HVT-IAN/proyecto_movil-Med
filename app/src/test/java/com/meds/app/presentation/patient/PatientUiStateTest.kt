package com.meds.app.presentation.patient

import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.DoseStatus
import com.meds.app.domain.model.Nudge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PatientUiStateTest {

    @Test
    fun cuentaTomasRegistradasDelDia() {
        val state = PatientUiState(
            todayDoses = listOf(
                dose(id = "1", status = DoseStatus.TAKEN),
                dose(id = "2", status = DoseStatus.PENDING),
                dose(id = "3", status = DoseStatus.TAKEN)
            )
        )

        assertEquals(3, state.totalCount)
        assertEquals(2, state.takenCount)
    }

    @Test
    fun priorizaLaTomaAtrasadaMasAntigua() {
        val now = 1_000L
        val state = PatientUiState(
            todayDoses = listOf(
                dose(id = "futura", status = DoseStatus.PENDING, scheduledAt = 5_000L),
                dose(id = "atrasada-reciente", status = DoseStatus.PENDING, scheduledAt = 900L),
                dose(id = "atrasada-antigua", status = DoseStatus.SNOOZED, scheduledAt = 500L)
            )
        )

        assertEquals("atrasada-antigua", state.nextPendingDose(now)?.id)
    }

    @Test
    fun sinTomasAtrasadasDevuelveLaSiguiente() {
        val state = PatientUiState(
            todayDoses = listOf(
                dose(id = "lejana", status = DoseStatus.PENDING, scheduledAt = 9_000L),
                dose(id = "cercana", status = DoseStatus.PENDING, scheduledAt = 2_000L)
            )
        )

        assertEquals("cercana", state.nextPendingDose(nowMillis = 1_000L)?.id)
    }

    @Test
    fun ignoraTomasYaResueltas() {
        val state = PatientUiState(
            todayDoses = listOf(
                dose(id = "tomada", status = DoseStatus.TAKEN, scheduledAt = 100L),
                dose(id = "omitida", status = DoseStatus.SKIPPED, scheduledAt = 200L)
            )
        )

        assertNull(state.nextPendingDose(nowMillis = 1_000L))
    }

    @Test
    fun exponeElPrimerAvisoSinLeer() {
        val state = PatientUiState(
            nudges = listOf(
                nudge(id = "leido", read = true),
                nudge(id = "nuevo", read = false)
            )
        )

        assertEquals("nuevo", state.latestUnreadNudge?.id)
    }

    private fun dose(
        id: String,
        status: String,
        scheduledAt: Long = 0L
    ): DoseLog = DoseLog(
        id = id,
        medicationId = "med",
        scheduleId = "sched",
        medicationName = "Medicamento",
        dose = "1 tableta",
        scheduledAt = scheduledAt,
        status = status,
        dateKey = "2026-11-12"
    )

    private fun nudge(id: String, read: Boolean): Nudge = Nudge(
        id = id,
        fromUid = "acompanante",
        fromName = "Acompañante",
        message = "Recordatorio",
        createdAt = 0L,
        read = read
    )
}
