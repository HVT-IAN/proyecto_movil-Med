package com.meds.app.presentation.companion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.meds.app.domain.model.CompanionDay
import com.meds.app.domain.model.LinkedPatient
import com.meds.app.data.repository.CompanionRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompanionUiState(
    val loading: Boolean = true,
    val linkedPatients: List<LinkedPatient> = emptyList(),
    val selectedPatient: LinkedPatient? = null,
    val day: CompanionDay? = null,
    val linking: Boolean = false,
    val nudgeSending: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)

private data class CompanionExtras(
    val linking: Boolean = false,
    val nudgeSending: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class CompanionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CompanionRepository()
    private val companionUid = FirebaseAuth.getInstance().currentUser?.uid
    private val extras = MutableStateFlow(CompanionExtras())
    private val currentDate = MutableStateFlow(todayKey())

    init {
        viewModelScope.launch {
            while (true) {
                delay(30_000L)
                currentDate.value = todayKey()
            }
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
                .retryWhen { cause, attempt ->
                    val permissionDenied = (cause as? FirebaseFirestoreException)?.code ==
                        FirebaseFirestoreException.Code.PERMISSION_DENIED
                    if (permissionDenied && attempt < 3) {
                        // La escritura local del vínculo puede emitirse antes de que
                        // Firestore confirme el lote y aplique las reglas.
                        delay(700L * (attempt + 1))
                        true
                    } else {
                        false
                    }
                }
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
        val message = throwable.message.orEmpty()
        return if (message.contains("PERMISSION_DENIED", ignoreCase = true)) {
            "El acceso a este paciente fue retirado. Puedes desvincularte para volver a empezar."
        } else {
            message.ifBlank { "No se pudieron cargar los datos del paciente." }
        }
    }
}
