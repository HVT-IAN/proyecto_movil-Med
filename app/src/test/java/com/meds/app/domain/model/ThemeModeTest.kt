package com.meds.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `fromName devuelve SYSTEM cuando es null`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
    }

    @Test
    fun `fromName devuelve DARK cuando recibe DARK`() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("DARK"))
    }

    @Test
    fun `fromName devuelve LIGHT cuando recibe LIGHT`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromName("LIGHT"))
    }

    @Test
    fun `fromName devuelve SYSTEM cuando recibe un valor desconocido`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("otro"))
    }

    @Test
    fun `resolveDarkTheme sigue al sistema cuando mode es SYSTEM`() {
        assertTrue(resolveDarkTheme(ThemeMode.SYSTEM, systemDark = true))
        assertFalse(resolveDarkTheme(ThemeMode.SYSTEM, systemDark = false))
    }

    @Test
    fun `resolveDarkTheme devuelve false cuando mode es LIGHT`() {
        assertFalse(resolveDarkTheme(ThemeMode.LIGHT, systemDark = true))
        assertFalse(resolveDarkTheme(ThemeMode.LIGHT, systemDark = false))
    }

    @Test
    fun `resolveDarkTheme devuelve true cuando mode es DARK`() {
        assertTrue(resolveDarkTheme(ThemeMode.DARK, systemDark = false))
        assertTrue(resolveDarkTheme(ThemeMode.DARK, systemDark = true))
    }
}
