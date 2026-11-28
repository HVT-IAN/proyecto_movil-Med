package com.meds.app.presentation.settings

import com.meds.app.domain.model.PatientNavBarStyle
import com.meds.app.domain.model.ThemeMode
import com.meds.app.domain.model.UserRole

/** Datos de cuenta que consume la pantalla de Ajustes. */
data class AccountUiState(
    val userId: String = "",
    val name: String = "",
    val role: UserRole = UserRole.PATIENT,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = true,
    val deviceCredentialAvailable: Boolean = false,
    val navBarStyle: PatientNavBarStyle = PatientNavBarStyle.STATIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    val graphLabels: Boolean = false,
    val authLoading: Boolean = false,
    val authError: String? = null,
    val authNotice: String? = null
)
