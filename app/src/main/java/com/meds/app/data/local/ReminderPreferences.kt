package com.meds.app.data.local

import android.content.Context
import com.meds.app.domain.model.ReminderSettings
import com.meds.app.domain.repository.ReminderSettingsSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias de recordatorios en SharedPreferences.
 *
 * Es síncrono a propósito: `AlarmScheduler` programa alarmas desde métodos
 * que no son suspend. El `StateFlow` mantiene reactiva la pantalla de ajustes.
 */
class ReminderPreferences(context: Context) : ReminderSettingsSource {
    private val prefs = context.applicationContext
        .getSharedPreferences(NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<ReminderSettings> = _settings.asStateFlow()

    override fun current(): ReminderSettings = _settings.value

    fun setMasterEnabled(enabled: Boolean) = write { it.copy(masterEnabled = enabled) }

    fun setMedicationEnabled(medicationId: String, enabled: Boolean) {
        if (medicationId.isBlank()) return
        write { current ->
            val disabled = current.disabledMedicationIds.toMutableSet()
            if (enabled) disabled.remove(medicationId) else disabled.add(medicationId)
            current.copy(disabledMedicationIds = disabled)
        }
    }

    fun setQuietEnabled(enabled: Boolean) = write { it.copy(quietEnabled = enabled) }

    fun setQuietHours(startMinutes: Int, endMinutes: Int) {
        write {
            it.copy(
                quietStartMinutes = startMinutes.coerceIn(0, MINUTES_PER_DAY - 1),
                quietEndMinutes = endMinutes.coerceIn(0, MINUTES_PER_DAY - 1)
            )
        }
    }

    fun setSoundEnabled(enabled: Boolean) = write { it.copy(soundEnabled = enabled) }

    fun clear() {
        prefs.edit().clear().apply()
        _settings.value = ReminderSettings()
    }

    private fun write(transform: (ReminderSettings) -> ReminderSettings) {
        val updated = transform(_settings.value)
        prefs.edit()
            .putBoolean(KEY_MASTER, updated.masterEnabled)
            .putStringSet(KEY_DISABLED, updated.disabledMedicationIds)
            .putBoolean(KEY_QUIET, updated.quietEnabled)
            .putInt(KEY_QUIET_START, updated.quietStartMinutes)
            .putInt(KEY_QUIET_END, updated.quietEndMinutes)
            .putBoolean(KEY_SOUND, updated.soundEnabled)
            .apply()
        _settings.value = updated
    }

    private fun read(): ReminderSettings {
        val defaults = ReminderSettings()
        return ReminderSettings(
            masterEnabled = prefs.getBoolean(KEY_MASTER, defaults.masterEnabled),
            disabledMedicationIds = prefs
                .getStringSet(KEY_DISABLED, emptySet())
                .orEmpty()
                .toSet(),
            quietEnabled = prefs.getBoolean(KEY_QUIET, defaults.quietEnabled),
            quietStartMinutes = prefs.getInt(
                KEY_QUIET_START,
                defaults.quietStartMinutes
            ),
            quietEndMinutes = prefs.getInt(
                KEY_QUIET_END,
                defaults.quietEndMinutes
            ),
            soundEnabled = prefs.getBoolean(KEY_SOUND, defaults.soundEnabled)
        )
    }

    private companion object {
        const val NAME = "meds_reminder_prefs"
        const val MINUTES_PER_DAY = 24 * 60
        const val KEY_MASTER = "master_enabled"
        const val KEY_DISABLED = "disabled_medication_ids"
        const val KEY_QUIET = "quiet_enabled"
        const val KEY_QUIET_START = "quiet_start_minutes"
        const val KEY_QUIET_END = "quiet_end_minutes"
        const val KEY_SOUND = "sound_enabled"
    }
}
