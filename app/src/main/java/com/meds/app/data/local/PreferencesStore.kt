package com.meds.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.medsDataStore by preferencesDataStore(name = "meds_prefs")

class PreferencesStore(private val context: Context) {
    private val roleKey = stringPreferencesKey("role")
    private val nameKey = stringPreferencesKey("name")
    private val onboardedKey = booleanPreferencesKey("onboarded")
    private val biometricEnabledKey = booleanPreferencesKey("biometric_enabled")
    private val biometricAskedKey = booleanPreferencesKey("biometric_asked")
    val role: Flow<String?> = context.medsDataStore.data.map { it[roleKey] }
    val name: Flow<String> = context.medsDataStore.data.map { it[nameKey].orEmpty() }
    val onboarded: Flow<Boolean> = context.medsDataStore.data.map { it[onboardedKey] ?: false }
    val biometricEnabled: Flow<Boolean> =
        context.medsDataStore.data.map { it[biometricEnabledKey] ?: false }
    val biometricAsked: Flow<Boolean> =
        context.medsDataStore.data.map { it[biometricAskedKey] ?: false }

    suspend fun setRole(role: String) {
        context.medsDataStore.edit { preferences ->
            if (preferences[roleKey] != role) {
                preferences[onboardedKey] = false
            }
            preferences[roleKey] = role
        }
    }

    suspend fun completeOnboarding(name: String) {
        context.medsDataStore.edit { preferences ->
            preferences[nameKey] = name.trim()
            preferences[onboardedKey] = true
        }
    }

    suspend fun restoreProfile(role: String, name: String) {
        context.medsDataStore.edit { preferences ->
            preferences[roleKey] = role
            preferences[nameKey] = name.trim()
            preferences[onboardedKey] = true
        }
    }

    suspend fun clearRole() {
        context.medsDataStore.edit { preferences ->
            preferences.remove(roleKey)
            preferences[onboardedKey] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.medsDataStore.edit { preferences ->
            preferences[biometricEnabledKey] = enabled
            preferences[biometricAskedKey] = true
        }
    }

    suspend fun markBiometricAsked() {
        context.medsDataStore.edit { preferences ->
            preferences[biometricAskedKey] = true
        }
    }

    suspend fun clearAll() {
        context.medsDataStore.edit { preferences ->
            preferences.remove(roleKey)
            preferences.remove(nameKey)
            preferences.remove(onboardedKey)
            preferences.remove(biometricEnabledKey)
            preferences.remove(biometricAskedKey)
        }
    }
}
