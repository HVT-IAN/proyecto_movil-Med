package com.meds.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    private val now = 1_800_000_000_000L
    private val minute = 60_000L
    private val hour = 60 * minute

    @Test
    fun `devuelve vacio para epoch invalido`() {
        assertEquals("", timeUntil(0L, now))
    }

    @Test
    fun `ahora cuando falta menos de dos minutos`() {
        assertEquals("ahora", timeUntil(now + 30_000L, now))
    }

    @Test
    fun `faltan minutos en el futuro cercano`() {
        assertEquals("faltan 25 min", timeUntil(now + 25 * minute, now))
    }

    @Test
    fun `faltan horas y minutos`() {
        assertEquals("faltan 2 h 15 min", timeUntil(now + 2 * hour + 15 * minute, now))
    }

    @Test
    fun `faltan horas exactas`() {
        assertEquals("faltan 3 h", timeUntil(now + 3 * hour, now))
    }

    @Test
    fun `hace minutos en el pasado`() {
        assertEquals("hace 10 min", timeUntil(now - 10 * minute, now))
    }

    @Test
    fun `hace horas y minutos en el pasado`() {
        assertEquals("hace 1 h 30 min", timeUntil(now - 90 * minute, now))
    }

    @Test
    fun `mas de un dia cae en la hora formateada`() {
        val target = now + 25 * hour
        assertEquals(formatTime(target), timeUntil(target, now))
    }
}
