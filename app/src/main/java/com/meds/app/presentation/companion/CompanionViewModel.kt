package com.meds.app.presentation.companion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meds.app.core.common.AccessDeniedException
import com.meds.app.core.common.periodicTicker
import com.meds.app.domain.model.CompanionDay
import com.meds.app.domain.model.LinkedPatient
import com.meds.app.domain.repository.AuthRepository
import com.meds.app.domain.repository.CompanionRepository
import com.meds.app.core.common.todayKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class CompanionExtras(
    val linking: Boolean = false,
    val nudgeSending: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class CompanionViewModel(
    private val repository: CompanionRepository,
    authRepository: AuthRepository
) : ViewModel() {
    private val companionUid = authRepository.currentUserId
    private val extras = MutableStateFlow(CompanionExtras())
    private val currentDate = MutableStateFlow(todayKey())

    init {
        viewModelScope.launch {
            periodicTicker(DATE_CHECK_INTERVAL_MS, initialDelayMillis = 0L)
                .map { todayKey() }
                .distinctUntilChanged()
                .collect { date -> currentDate.value = date }
        }
    }

    private val patientsSource: Flow<List<LinkedPatient>> = if (companionUid.isNullOrBlank()) {
        flowOf(emptyList())
    } else {
        repository.observeLinkedPatients(companionUid)
    }

    private val linkedPatients = patientsSource.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val dayFlow = combine(linkedPatients, currentDate) { patients, date ->
        patients.firstOrNull() to date
    }.flatMapLatest { (patient, date) ->
        if (patient == null) {
            flowOf<CompanionDay?>(null)
        } else {
            repository.observePatientDay(patient.uid, date)
                .map<CompanionDay, CompanionDay?> { it }
                .catch { throwable ->
                    extras.update { it.copy(error = accessError(throwable)) }
                    emit(null)
                }
        }
    }

    private val coreFlow = combine(linkedPatients, dayFlow) { patients, day ->
        CompanionUiState(
            loading = false,
            linkedPatients = patients,
            selectedPatient = patients.firstOrNull(),
            day = day
        )
    }.catch { throwable ->
        extras.update { it.copy(error = accessError(throwable)) }
        emit(CompanionUiState(loading = false))
    }

    val state: StateFlow<CompanionUiState> = combine(coreFlow, extras) { core, extra ->
        core.copy(
            linking = extra.linking,
            nudgeSending = extra.nudgeSending,
            error = extra.error,
            infoMessage = extra.infoMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CompanionUiState()
    )

    fun linkWithCode(code: String, companionName: String) {
        if (extras.value.linking) return
        extras.update { it.copy(linking = true, error = null, infoMessage = null) }
        viewModelScope.launch {
            repository.linkWithCode(code, companionName)
                .onSuccess {
                    extras.update {
                        it.copy(
                            linking = false,
                            infoMessage = "Vinculación lista."
                        )
                    }
                }
                .onFailure { throwable ->
                    extras.update {
                        it.copy(
                            linking = false,
                            error = throwable.message ?: "No se pudo vincular el código."
                        )
                    }
                }
        }
    }

    fun sendNudge(patientUid: String, fromName: String, message: String) {
        if (extras.value.nudgeSending) return
        extras.update { it.copy(nudgeSending = true, error = null, infoMessage = null) }
        viewModelScope.launch {
            repository.sendNudge(patientUid, fromName, message)
                .onSuccess {
                    extras.update {
                        it.copy(
                            nudgeSending = false,
                            infoMessage = "Recordatorio enviado."
                        )
                    }
                }
                .onFailure { throwable ->
                    extras.update {
                        it.copy(
                            nudgeSending = false,
                            error = throwable.message ?: "No se pudo enviar el recordatorio."
                        )
                    }
                }
        }
    }

    fun unlinkPatient(patientUid: String) {
        val uid = companionUid ?: return
        viewModelScope.launch {
            repository.unlinkPatient(uid, patientUid)
                .onSuccess {
                    extras.update {
                        it.copy(infoMessage = "Te desvinculaste de este paciente.")
                    }
                }
                .onFailure { throwable ->
                    extras.update {
                        it.copy(error = throwable.message ?: "No se pudo desvincular.")
                    }
                }
        }
    }

    fun clearError() {
        extras.update { it.copy(error = null) }
    }

    fun clearInfo() {
        extras.update { it.copy(infoMessage = null) }
    }

    private fun accessError(throwable: Throwable): String {
        if (throwable is AccessDeniedException) {
            return "El acceso a este paciente fue retirado. Puedes desvincularte para volver a empezar."
        }
        val message = throwable.message.orEmpty()
        return message.ifBlank { "No se pudieron cargar los datos del paciente." }
    }

    private companion object {
        const val DATE_CHECK_INTERVAL_MS = 30_000L
    }
}
