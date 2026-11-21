package com.meds.app.domain.repository

import com.meds.app.domain.model.LocalSession
import com.meds.app.domain.model.PatientNavBarStyle
import kotlinx.coroutines.flow.Flow

/** Contrato de la sesión local: rol, onboarding, biometría y estilo de navegación. */
interface SessionRepository {
    val session: Flow<LocalSession>

    suspend fun currentRole(): String?

    suspend fun setRole(role: String)

    suspend fun completeOnboarding(name: String)

    suspend fun restoreProfile(role: String, name: String)

    suspend fun clearRole()

    suspend fun setBiometricEnabled(enabled: Boolean)

    suspend fun markBiometricAsked()

    suspend fun setNavBarStyle(style: PatientNavBarStyle)

    fun tutorialSeen(uid: String): Flow<Boolean>

    suspend fun markTutorialSeen(uid: String)

    suspend fun clear()
}
