package com.meds.app.data.repository

import com.meds.app.data.local.PreferencesStore
import com.meds.app.domain.model.LocalSession
import com.meds.app.domain.model.PatientNavBarStyle
import com.meds.app.domain.model.ThemeMode
import com.meds.app.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/** Implementación de la sesión local sobre DataStore. */
class SessionRepositoryImpl(
    private val preferences: PreferencesStore
) : SessionRepository {

    private val baseSession: Flow<LocalSession> = combine(
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

    private val appearanceSession: Flow<LocalSession> = combine(
        baseSession,
        preferences.navBarStyle,
        preferences.themeMode,
        preferences.reduceMotion,
        preferences.highContrast
    ) { base, navBarStyle, themeMode, reduceMotion, highContrast ->
        base.copy(
            navBarStyle = PatientNavBarStyle.fromName(navBarStyle),
            themeMode = ThemeMode.fromName(themeMode),
            reduceMotion = reduceMotion,
            highContrast = highContrast
        )
    }

    override val session: Flow<LocalSession> = combine(
        appearanceSession,
        preferences.graphLabels
    ) { session, graphLabels ->
        session.copy(graphLabels = graphLabels)
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

    override suspend fun setNavBarStyle(style: PatientNavBarStyle) {
        preferences.setNavBarStyle(style.name)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        preferences.setThemeMode(mode.name)
    }

    override suspend fun setReduceMotion(enabled: Boolean) {
        preferences.setReduceMotion(enabled)
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        preferences.setHighContrast(enabled)
    }

    override suspend fun setGraphLabels(enabled: Boolean) {
        preferences.setGraphLabels(enabled)
    }

    override fun tutorialSeen(uid: String): Flow<Boolean> =
        preferences.tutorialSeen(uid)

    override suspend fun markTutorialSeen(uid: String) {
        preferences.markTutorialSeen(uid)
    }

    override suspend fun clear() {
        preferences.clearAll()
    }
}
