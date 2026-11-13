package com.meds.app.domain.model

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantStateTest {

    private val today = LocalDate.of(2026, 11, 12)

    @Test
    fun estadoInicialEmpiezaEnPrimeraEtapa() {
        val state = PlantState()

        assertEquals(1, state.stage)
        assertEquals(0, state.stageXp)
        assertEquals(0f, state.stageProgress, 0.001f)
        assertFalse(state.isMaxLevel)
    }

    @Test
    fun progresoDeEtapaSeCalculaConElRestoDelXp() {
        val state = PlantState(level = 2, xp = 60)

        assertEquals(2, state.stage)
        assertEquals(10, state.stageXp)
        assertEquals(0.2f, state.stageProgress, 0.001f)
    }

    @Test
    fun nivelMaximoMuestraProgresoCompleto() {
        val state = PlantState(level = PlantState.MAX_LEVEL, xp = 320)

        assertTrue(state.isMaxLevel)
        assertEquals(PlantState.XP_PER_LEVEL, state.stageXp)
        assertEquals(1f, state.stageProgress, 0.001f)
    }

    @Test
    fun primeraTomaIniciaRachaYSumaXp() {
        val state = PlantState().registerDoseTaken(today)

        assertEquals(1, state.streak)
        assertEquals(PlantState.XP_PER_DOSE, state.xp)
        assertEquals(1, state.level)
        assertEquals(1, state.totalTaken)
        assertEquals(today.toString(), state.lastTakenDate)
    }

    @Test
    fun tomarDosVecesElMismoDiaNoDuplicaRacha() {
        val state = PlantState(lastTakenDate = today.toString(), streak = 4)
            .registerDoseTaken(today)

        assertEquals(4, state.streak)
        assertEquals(1, state.totalTaken)
    }

    @Test
    fun diaConsecutivoAumentaRacha() {
        val state = PlantState(lastTakenDate = today.minusDays(1).toString(), streak = 3)
            .registerDoseTaken(today)

        assertEquals(4, state.streak)
    }

    @Test
    fun unDiaPerdidoConsumeEscudoYMantieneRacha() {
        val state = PlantState(
            lastTakenDate = today.minusDays(2).toString(),
            streak = 3,
            freezeAvailable = 1
        ).registerDoseTaken(today)

        assertEquals(4, state.streak)
        assertEquals(0, state.freezeAvailable)
    }

    @Test
    fun ausenciaLargaReiniciaRacha() {
        val state = PlantState(
            lastTakenDate = today.minusDays(4).toString(),
            streak = 8,
            freezeAvailable = 1
        ).registerDoseTaken(today)

        assertEquals(1, state.streak)
        assertEquals(1, state.freezeAvailable)
    }

    @Test
    fun alCompletarXpSubeDeEtapa() {
        val state = PlantState(xp = PlantState.XP_PER_LEVEL - PlantState.XP_PER_DOSE)
            .registerDoseTaken(today)

        assertEquals(2, state.level)
        assertEquals(PlantState.XP_PER_LEVEL, state.xp)
    }

    @Test
    fun noSuperaElNivelMaximo() {
        val state = PlantState(level = PlantState.MAX_LEVEL, xp = 900)
            .registerDoseTaken(today)

        assertEquals(PlantState.MAX_LEVEL, state.stage)
        assertTrue(state.isMaxLevel)
    }
}
