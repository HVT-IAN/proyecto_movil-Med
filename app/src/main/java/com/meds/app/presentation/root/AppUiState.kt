package com.meds.app.presentation.root

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
    val biometricEnabled: Boolean = false,
    val biometricAsked: Boolean = false,
    val authLoading: Boolean = false,
    val authError: String? = null,
    val authNotice: String? = null
)
