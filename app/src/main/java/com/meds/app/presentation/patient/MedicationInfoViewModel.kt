package com.meds.app.presentation.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meds.app.domain.model.MedicationInfo
import com.meds.app.domain.model.MedicationInfoDetail
import com.meds.app.data.repository.MedicationInfoRepositoryImpl
import com.meds.app.domain.repository.MedicationInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedicationInfoViewModel(
    private val repository: MedicationInfoRepository = MedicationInfoRepositoryImpl()
) : ViewModel() {
    private val _state = MutableStateFlow(MedicationInfoUiState())
    val state: StateFlow<MedicationInfoUiState> = _state.asStateFlow()

    fun initialize(initialQuery: String) {
        val clean = initialQuery.trim()
        if (clean.isBlank() || _state.value.query.isNotBlank()) return
        _state.update { it.copy(query = clean) }
        search()
    }

    fun onQueryChange(value: String) {
        _state.update {
            it.copy(
                query = value.take(80),
                error = null
            )
        }
    }

    fun search() {
        val query = _state.value.query.trim()
        if (query.length < 2) {
            _state.update {
                it.copy(
                    searched = true,
                    error = "Escribe al menos 2 caracteres para buscar."
                )
            }
            return
        }
        if (_state.value.loading) return

        _state.update {
            it.copy(
                loading = true,
                searched = true,
                error = null,
                results = emptyList(),
                total = 0
            )
        }

        viewModelScope.launch {
            repository.search(query)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            loading = false,
                            results = result.items,
                            total = result.total,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            loading = false,
                            results = emptyList(),
                            total = 0,
                            error = throwable.toMedicationInfoMessage()
                        )
                    }
                }
        }
    }

    fun openDetail(registry: String) {
        if (_state.value.detailLoading) return
        _state.update {
            it.copy(
                detailLoading = true,
                detail = null,
                detailError = null
            )
        }
        viewModelScope.launch {
            repository.detail(registry)
                .onSuccess { detail ->
                    _state.update {
                        it.copy(
                            detailLoading = false,
                            detail = detail
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            detailLoading = false,
                            detailError = throwable.toMedicationInfoMessage()
                        )
                    }
                }
        }
    }

    fun closeDetail() {
        _state.update {
            it.copy(
                detail = null,
                detailError = null,
                detailLoading = false
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
