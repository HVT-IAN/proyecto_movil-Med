package com.meds.app.domain.repository

import com.meds.app.domain.model.ReminderSettings
import kotlinx.coroutines.flow.Flow

interface ReminderSettingsRepository {
    val settings: Flow<ReminderSettings>

    fun current(): ReminderSettings

    suspend fun setMasterEnabled(enabled: Boolean)

    suspend fun setMedicationEnabled(medicationId: String, enabled: Boolean)

    suspend fun setQuietEnabled(enabled: Boolean)

    suspend fun setQuietHours(startMinutes: Int, endMinutes: Int)

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun clear()
}
