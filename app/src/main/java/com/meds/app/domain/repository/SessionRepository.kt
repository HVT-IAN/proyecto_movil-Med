package com.meds.app.domain.repository

import com.meds.app.domain.model.LocalSession
import com.meds.app.domain.model.PatientNavBarStyle
import com.meds.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** Contrato de la sesión local: rol, onboarding, biometría, apariencia y navegación. */
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

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setReduceMotion(enabled: Boolean)

    suspend fun setHighContrast(enabled: Boolean)

    suspend fun setGraphLabels(enabled: Boolean)

    fun tutorialSeen(uid: String): Flow<Boolean>

    suspend fun markTutorialSeen(uid: String)

    suspend fun clear()
}
