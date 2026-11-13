package com.meds.app.data.repository

import com.meds.app.data.local.PreferencesStore
import com.meds.app.domain.model.LocalSession
import com.meds.app.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/** Implementación de la sesión local sobre DataStore. */
class SessionRepositoryImpl(
    private val preferences: PreferencesStore
) : SessionRepository {

    override val session: Flow<LocalSession> = combine(
        preferences.role,
        preferences.name,
        preferences.onboarded,
        preferences.biometricEnabled,
        preferences.biometricAsked
    ) { role, name, onboarded, biometricEnabled, biometricAsked ->
        LocalSession(
            role = role,
            name = name,
            onboarded = onboarded,
            biometricEnabled = biometricEnabled,
            biometricAsked = biometricAsked
        )
    }

    override suspend fun currentRole(): String? = preferences.role.first()

    override suspend fun setRole(role: String) {
        preferences.setRole(role)
    }

    override suspend fun completeOnboarding(name: String) {
        preferences.completeOnboarding(name)
    }

    override suspend fun restoreProfile(role: String, name: String) {
        preferences.restoreProfile(role, name)
    }

    override suspend fun clearRole() {
        preferences.clearRole()
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        preferences.setBiometricEnabled(enabled)
    }

    override suspend fun markBiometricAsked() {
        preferences.markBiometricAsked()
    }

    override suspend fun clear() {
        preferences.clearAll()
    }
}
