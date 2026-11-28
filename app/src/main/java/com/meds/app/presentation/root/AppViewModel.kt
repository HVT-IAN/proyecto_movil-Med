package com.meds.app.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meds.app.domain.model.PatientNavBarStyle
import com.meds.app.domain.model.ThemeMode
import com.meds.app.domain.model.UserRole
import com.meds.app.domain.repository.AuthRepository
import com.meds.app.domain.repository.MedicationCacheRepository
import com.meds.app.domain.repository.SessionRepository
import com.meds.app.domain.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AppViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val medicationCacheRepository: MedicationCacheRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()
    private var profileRestoredForUid: String? = null
    private var restoreJob: Job? = null
    private var tutorialJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.authStateFlow().collect { user ->
                if (user == null) {
                    restoreJob?.cancel()
                    restoreJob = null
                    tutorialJob?.cancel()
                    tutorialJob = null
                    profileRestoredForUid = null
                    _state.update {
                        it.copy(
                            loading = false,
                            userId = null,
                            email = null,
                            isAnonymous = false,
                            authLoading = false,
                            tutorialSeen = false,
                            tutorialLoaded = false
                        )
                    }
                } else {
                    val alreadySignedIn = _state.value.userId != null
                    _state.update {
                        it.copy(
                            loading = if (alreadySignedIn) it.loading else true,
                            userId = user.uid,
                            email = user.email,
                            isAnonymous = user.isAnonymous,
                            authLoading = false,
                            authError = null,
                            tutorialSeen = if (alreadySignedIn) it.tutorialSeen else false,
                            tutorialLoaded = if (alreadySignedIn) it.tutorialLoaded else false
                        )
                    }
                    restoreProfileIfNeeded(user.uid)
                    observeTutorial(user.uid)
                }
            }
        }
        viewModelScope.launch {
            sessionRepository.session.collect { local ->
                _state.update {
                    it.copy(
                        role = local.role?.let { value ->
                            UserRole.entries.firstOrNull { entry -> entry.name == value }
                        },
                        name = local.name,
                        onboarded = local.onboarded,
                        biometricEnabled = local.biometricEnabled,
                        biometricAsked = local.biometricAsked,
                        navBarStyle = local.navBarStyle,
                        themeMode = local.themeMode,
                        reduceMotion = local.reduceMotion,
                        highContrast = local.highContrast,
                        graphLabels = local.graphLabels
                    )
                }
            }
        }
    }

    private fun restoreProfileIfNeeded(uid: String) {
        if (profileRestoredForUid == uid) return
        profileRestoredForUid = uid
        restoreJob?.cancel()
        restoreJob = viewModelScope.launch {
            val profile = withTimeoutOrNull(8_000) {
                runCatching { userRepository.loadProfile(uid) }.getOrNull()
            }
            if (_state.value.userId != uid) return@launch
            val role = profile?.role?.let { value ->
                UserRole.entries.firstOrNull { entry -> entry.name == value }
            }
            if (profile != null && role != null) {
                sessionRepository.restoreProfile(role.name, profile.name.orEmpty())
                _state.update {
                    it.copy(
                        role = role,
                        name = profile.name.orEmpty(),
                        onboarded = true,
                        loading = false
                    )
                }
            } else {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    private fun observeTutorial(uid: String) {
        tutorialJob?.cancel()
        tutorialJob = viewModelScope.launch {
            sessionRepository.tutorialSeen(uid).collect { seen ->
                _state.update {
                    it.copy(tutorialSeen = seen, tutorialLoaded = true)
                }
            }
        }
    }

    fun markTutorialSeen() {
        val uid = _state.value.userId ?: return
        _state.update { it.copy(tutorialSeen = true) }
        viewModelScope.launch {
            sessionRepository.markTutorialSeen(uid)
        }
    }

    fun login(email: String, password: String) {
        launchAuth { authRepository.login(email, password) }
    }

    fun register(email: String, password: String) {
        launchAuth { authRepository.register(email, password) }
    }

    fun continueAnonymously() {
        launchAuth { authRepository.continueAnonymously() }
    }

    fun linkEmail(email: String, password: String) {
        _state.update { it.copy(authLoading = true, authError = null, authNotice = null) }
        viewModelScope.launch {
            authRepository.linkAnonymousToEmail(email, password)
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            userId = user.uid,
                            email = user.email,
                            isAnonymous = user.isAnonymous,
                            authLoading = false,
                            authError = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            authLoading = false,
                            authError = throwable.message ?: "No se pudo vincular el correo."
                        )
                    }
                }
        }
    }

    fun sendPasswordReset(email: String) {
        _state.update { it.copy(authError = null, authNotice = null) }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _state.update {
                        it.copy(
                            authNotice = "Te enviamos un enlace para restablecer tu contraseña."
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            authError = throwable.message
                                ?: "No se pudo enviar el correo de recuperación."
                        )
                    }
                }
        }
    }

    private fun launchAuth(block: suspend () -> Result<*>) {
        _state.update { it.copy(authLoading = true, authError = null, authNotice = null) }
        viewModelScope.launch {
            block()
                .onSuccess {
                    _state.update { it.copy(authLoading = false, authError = null) }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            authLoading = false,
                            authError = throwable.message ?: "Ocurrió un error inesperado."
                        )
                    }
                }
        }
    }

    fun clearAuthMessages() {
        _state.update { it.copy(authError = null, authNotice = null) }
    }

    fun signOut() {
        restoreJob?.cancel()
        restoreJob = null
        tutorialJob?.cancel()
        tutorialJob = null
        profileRestoredForUid = null
        viewModelScope.launch {
            authRepository.currentUserId?.let { uid ->
                userRepository.unregisterCurrentDevice(uid)
            }
            authRepository.signOut()
            sessionRepository.clear()
            medicationCacheRepository.clearAll()
            _state.update { AppUiState(loading = false) }
        }
    }

    fun selectRole(role: UserRole) {
        viewModelScope.launch {
            sessionRepository.setRole(role.name)
            authRepository.currentUserId?.let { uid ->
                runCatching { userRepository.saveRole(uid, role.name) }
            }
        }
    }

    fun completeOnboarding(name: String) {
        val resolvedName = name.trim().ifBlank { DEFAULT_USER_NAME }
        viewModelScope.launch {
            sessionRepository.completeOnboarding(resolvedName)
            val uid = authRepository.currentUserId ?: return@launch
            val role = sessionRepository.currentRole() ?: return@launch
            runCatching { userRepository.saveProfile(uid, role, resolvedName) }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            sessionRepository.setBiometricEnabled(enabled)
        }
    }

    fun markBiometricAsked() {
        viewModelScope.launch {
            sessionRepository.markBiometricAsked()
        }
    }

    fun setNavBarStyle(style: PatientNavBarStyle) {
        viewModelScope.launch {
            sessionRepository.setNavBarStyle(style)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            sessionRepository.setThemeMode(mode)
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch {
            sessionRepository.setReduceMotion(enabled)
        }
    }

    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch {
            sessionRepository.setHighContrast(enabled)
        }
    }

    fun setGraphLabels(enabled: Boolean) {
        viewModelScope.launch {
            sessionRepository.setGraphLabels(enabled)
        }
    }

    fun goBackToRoleSelection() {
        viewModelScope.launch {
            sessionRepository.clearRole()
            _state.update { it.copy(role = null, onboarded = false) }
        }
    }

    private companion object {
        const val DEFAULT_USER_NAME = "Hola"
    }
}
