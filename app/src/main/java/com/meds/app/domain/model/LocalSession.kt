package com.meds.app.domain.model

/** Preferencias locales de sesión del usuario actual. */
data class LocalSession(
    val role: String? = null,
    val name: String = "",
    val onboarded: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricAsked: Boolean = false
)
