package com.meds.app.presentation.settings

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
    val authLoading: Boolean = false,
    val authError: String? = null,
    val authNotice: String? = null
)
