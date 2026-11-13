package com.meds.app.domain.repository

import com.meds.app.domain.model.LocalSession
import kotlinx.coroutines.flow.Flow

/** Contrato de la sesión local: rol, onboarding y preferencia de biometría. */
interface SessionRepository {
    val session: Flow<LocalSession>

    suspend fun currentRole(): String?

    suspend fun setRole(role: String)

    suspend fun completeOnboarding(name: String)

    suspend fun restoreProfile(role: String, name: String)

    suspend fun clearRole()

    suspend fun setBiometricEnabled(enabled: Boolean)

    suspend fun markBiometricAsked()

    suspend fun clear()
}
