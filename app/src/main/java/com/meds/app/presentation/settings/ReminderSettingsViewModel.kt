package com.meds.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meds.app.domain.model.ReminderSettings
import com.meds.app.domain.repository.ReminderSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderSettingsViewModel(
    private val repository: ReminderSettingsRepository
) : ViewModel() {

    val settings: StateFlow<ReminderSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.current()
    )

    fun setMasterEnabled(enabled: Boolean) =
        update { repository.setMasterEnabled(enabled) }

    fun setMedicationEnabled(medicationId: String, enabled: Boolean) =
        update { repository.setMedicationEnabled(medicationId, enabled) }

    fun setQuietEnabled(enabled: Boolean) =
        update { repository.setQuietEnabled(enabled) }

    fun setQuietHours(startMinutes: Int, endMinutes: Int) =
        update { repository.setQuietHours(startMinutes, endMinutes) }

    fun setSoundEnabled(enabled: Boolean) =
        update { repository.setSoundEnabled(enabled) }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
