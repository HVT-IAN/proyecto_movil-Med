package com.meds.app.data.repository

import com.meds.app.data.local.ReminderPreferences
import com.meds.app.domain.model.ReminderSettings
import com.meds.app.domain.repository.ReminderSettingsRepository
import com.meds.app.integration.notifications.AlarmScheduler
import kotlinx.coroutines.flow.Flow

class ReminderSettingsRepositoryImpl(
    private val preferences: ReminderPreferences,
    private val alarmScheduler: AlarmScheduler
) : ReminderSettingsRepository {

    override val settings: Flow<ReminderSettings> = preferences.settings

    override fun current(): ReminderSettings = preferences.current()

    override suspend fun setMasterEnabled(enabled: Boolean) {
        preferences.setMasterEnabled(enabled)
        alarmScheduler.applyReminderSettings()
    }

    override suspend fun setMedicationEnabled(medicationId: String, enabled: Boolean) {
        preferences.setMedicationEnabled(medicationId, enabled)
        alarmScheduler.applyReminderSettings()
    }

    override suspend fun setQuietEnabled(enabled: Boolean) {
        preferences.setQuietEnabled(enabled)
    }

    override suspend fun setQuietHours(startMinutes: Int, endMinutes: Int) {
        preferences.setQuietHours(startMinutes, endMinutes)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        preferences.setSoundEnabled(enabled)
    }

    override suspend fun clear() {
        preferences.clear()
        alarmScheduler.applyReminderSettings()
    }
}
