package com.meds.app.presentation.root

import com.meds.app.domain.model.PatientNavBarStyle
import com.meds.app.domain.model.ThemeMode
import com.meds.app.domain.model.UserRole

/** Estado global de sesión, preferencias y autenticación. */
data class AppUiState(
    val loading: Boolean = true,
    val userId: String? = null,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val role: UserRole? = null,
    val name: String = "",
    val onboarded: Boolean = false,
    val tutorialSeen: Boolean = false,
    val tutorialLoaded: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricAsked: Boolean = false,
    val navBarStyle: PatientNavBarStyle = PatientNavBarStyle.STATIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    val graphLabels: Boolean = false,
    val authLoading: Boolean = false,
    val authError: String? = null,
    val authNotice: String? = null
)
