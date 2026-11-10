package com.meds.app.presentation.patient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.Medication
import com.meds.app.domain.model.MedicationDraft
import com.meds.app.domain.model.Nudge
import com.meds.app.domain.model.PlantState
import com.meds.app.domain.model.Schedule
import com.meds.app.data.repository.CompanionRepository
import com.meds.app.data.repository.PatientRepository
import com.meds.app.core.common.todayKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PatientUiState(
    val loading: Boolean = true,
    val medications: List<Medication> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val todayDoses: List<DoseLog> = emptyList(),
    val recentDoses: List<DoseLog> = emptyList(),
    val plant: PlantState = PlantState(),
    val nudges: List<Nudge> = emptyList(),
    val error: String? = null,
    val saving: Boolean = false,
    val saveSuccess: Boolean = false
)

private data class PatientCore(
    val medications: List<Medication>,
    val schedules: List<Schedule>,
    val todayDoses: List<DoseLog>,
    val plant: PlantState
)

private data class PatientSecondary(
    val recentDoses: List<DoseLog>,
    val nudges: List<Nudge>
)

private data class PatientExtras(
    val error: String? = null,
    val saving: Boolean = false,
    val saveSuccess: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class PatientViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PatientRepository(application)
    private val companionRepository = CompanionRepository()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val extras = MutableStateFlow(PatientExtras())
    private var ensuring = false

    private val currentDate = MutableStateFlow(todayKey())

    private val nudgesFlow: Flow<List<Nudge>> = if (uid.isNullOrBlank()) {
        flowOf(emptyList())
    } else {
        companionRepository.observeNudges(uid)
    }

    private val todayDosesFlow = currentDate.flatMapLatest { date ->
        repository.observeDoses(date)
    }

    private val recentDosesFlow = currentDate.flatMapLatest { date ->
        val weekStart = LocalDate.parse(date).minusDays(6).toString()
        repository.observeRecentDoses(weekStart)
    }

    init {
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                val newDate = todayKey()
                if (newDate != currentDate.value) {
                    currentDate.value = newDate
                    ensureTodayDoses()
                }
            }
        }
    }

    private val primaryFlow = combine(
        repository.observeMedications(),
        repository.observeSchedules(),
        todayDosesFlow,
        repository.observePlant()
    ) { medications, schedules, todayDoses, plant ->
        PatientCore(
            medications = medications.sortedBy { it.name.lowercase() },
            schedules = schedules,
            todayDoses = todayDoses.sortedBy { it.scheduledAt },
            plant = plant
        )
    }

    private val secondaryFlow = combine(
        recentDosesFlow,
        nudgesFlow
    ) { recentDoses, nudges ->
        PatientSecondary(
            recentDoses = recentDoses.sortedByDescending { it.scheduledAt },
            nudges = nudges
        )
    }

    private val coreFlow = combine(primaryFlow, secondaryFlow) { primary, secondary ->
        PatientUiState(
            loading = false,
            medications = primary.medications,
            schedules = primary.schedules,
            todayDoses = primary.todayDoses,
            recentDoses = secondary.recentDoses,
            plant = primary.plant,
            nudges = secondary.nudges
        )
    }.catch { throwable ->
        extras.update {
            it.copy(error = throwable.message ?: "No se pudieron cargar tus datos.")
        }
        emit(PatientUiState(loading = false))
    }

    val state: StateFlow<PatientUiState> = combine(coreFlow, extras) { core, extra ->
        core.copy(
            error = extra.error,
            saving = extra.saving,
            saveSuccess = extra.saveSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PatientUiState()
    )

    fun ensureTodayDoses() {
        if (ensuring) return
        ensuring = true
        viewModelScope.launch {
            repository.ensureTodayDoses().onFailure { throwable ->
                extras.update {
                    it.copy(error = throwable.message ?: "No se pudieron preparar las tomas de hoy.")
                }
            }
            ensuring = false
        }
    }

    fun saveMedication(draft: MedicationDraft) {
        if (extras.value.saving) return
        extras.update { it.copy(saving = true, saveSuccess = false, error = null) }
        viewModelScope.launch {
            repository.saveMedication(draft)
                .onSuccess {
                    extras.update { it.copy(saving = false, saveSuccess = true) }
                }
                .onFailure { throwable ->
                    extras.update {
                        it.copy(
                            saving = false,
                            error = throwable.message ?: "No se pudo guardar el medicamento."
                        )
                    }
                }
        }
    }

    fun consumeSaveSuccess() {
        extras.update { it.copy(saveSuccess = false) }
    }

    fun takeDose(dose: DoseLog) {
        viewModelScope.launch {
            repository.takeDose(dose).onFailure { throwable ->
                extras.update {
                    it.copy(error = throwable.message ?: "No se pudo registrar la toma.")
                }
            }
        }
    }

    fun snoozeDose(dose: DoseLog) {
        viewModelScope.launch {
            repository.snoozeDose(dose).onFailure { throwable ->
                extras.update {
                    it.copy(error = throwable.message ?: "No se pudo posponer la toma.")
                }
            }
        }
    }

    fun deleteMedication(medicationId: String, scheduleId: String?) {
        viewModelScope.launch {
            repository.deleteMedication(medicationId, scheduleId).onFailure { throwable ->
                extras.update {
                    it.copy(error = throwable.message ?: "No se pudo eliminar el medicamento.")
                }
            }
        }
    }

    fun markNudgeRead(nudgeId: String) {
        val patientUid = uid ?: return
        if (nudgeId.isBlank()) return
        viewModelScope.launch {
            companionRepository.markNudgeRead(patientUid, nudgeId).onFailure { throwable ->
                extras.update {
                    it.copy(error = throwable.message ?: "No se pudo marcar el recordatorio.")
                }
            }
        }
    }

    fun clearError() {
        extras.update { it.copy(error = null) }
    }
}
