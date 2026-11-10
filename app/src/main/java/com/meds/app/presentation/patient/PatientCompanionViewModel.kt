package com.meds.app.presentation.patient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.meds.app.domain.model.CompanionLink
import com.meds.app.domain.model.Nudge
import com.meds.app.domain.model.PairingInvite
import com.meds.app.data.repository.CompanionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PatientCompanionUiState(
    val loading: Boolean = true,
    val companions: List<CompanionLink> = emptyList(),
    val invite: PairingInvite? = null,
    val nudges: List<Nudge> = emptyList(),
    val generating: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)

private data class PatientCompanionExtras(
    val generating: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)

class PatientCompanionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CompanionRepository()
    private val patientUid = FirebaseAuth.getInstance().currentUser?.uid
    private val extras = MutableStateFlow(PatientCompanionExtras())
    private val invite = MutableStateFlow<PairingInvite?>(null)

    private val companionsFlow: Flow<List<CompanionLink>> = if (patientUid.isNullOrBlank()) {
        flowOf(emptyList())
    } else {
        repository.observeCompanions(patientUid)
    }

    private val nudgesFlow: Flow<List<Nudge>> = if (patientUid.isNullOrBlank()) {
        flowOf(emptyList())
    } else {
        repository.observeNudges(patientUid)
    }

    private val coreFlow = combine(
        companionsFlow,
        nudgesFlow,
        invite
    ) { companions, nudges, activeInvite ->
        PatientCompanionUiState(
            loading = false,
            companions = companions,
            nudges = nudges,
            invite = activeInvite
        )
    }.catch { throwable ->
        extras.update {
            it.copy(error = throwable.message ?: "No se pudo cargar la información de acompañantes.")
        }
        emit(PatientCompanionUiState(loading = false))
    }

    val state: StateFlow<PatientCompanionUiState> = combine(coreFlow, extras) { core, extra ->
        core.copy(
            generating = extra.generating,
            error = extra.error,
            infoMessage = extra.infoMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PatientCompanionUiState()
    )

    fun generateInvite(
        patientName: String,
        previousCode: String? = null
    ) {
        val uid = patientUid ?: return
        if (extras.value.generating) return
        extras.update { it.copy(generating = true, error = null, infoMessage = null) }
        viewModelScope.launch {
            if (!previousCode.isNullOrBlank()) {
                repository.invalidateInvite(previousCode)
            }
            repository.createInvite(uid, patientName)
                .onSuccess { created ->
                    invite.value = created
                    extras.update {
                        it.copy(
                            generating = false,
                            infoMessage = if (previousCode.isNullOrBlank()) {
                                "Código generado."
                            } else {
                                "Nuevo código generado. El anterior ya no funciona."
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    extras.update {
                        it.copy(
                            generating = false,
                            error = throwable.message ?: "No se pudo generar el código."
                        )
                    }
                }
        }
    }

    fun revokeCompanion(companionUid: String) {
        val uid = patientUid ?: return
        viewModelScope.launch {
            repository.revokeCompanion(uid, companionUid)
                .onFailure { throwable ->
                    extras.update {
                        it.copy(error = throwable.message ?: "No se pudo retirar el acceso.")
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
}
