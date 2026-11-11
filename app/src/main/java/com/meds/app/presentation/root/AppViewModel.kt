package com.meds.app.presentation.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.messaging.FirebaseMessaging
import com.meds.app.data.local.OfflineMedicationStore
import com.meds.app.data.local.PreferencesStore
import com.meds.app.domain.model.UserRole
import com.meds.app.domain.repository.AuthRepository
import com.meds.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

private data class LocalPreferences(
    val role: String?,
    val name: String,
    val onboarded: Boolean,
    val biometricEnabled: Boolean,
    val biometricAsked: Boolean
)

class AppViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val preferences: PreferencesStore,
    private val offlineStore: OfflineMedicationStore
) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()
    private var profileRestoredForUid: String? = null
    private var restoreJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.authStateFlow().collect { user ->
                if (user == null) {
                    restoreJob?.cancel()
                    restoreJob = null
                    profileRestoredForUid = null
                    _state.update {
                        it.copy(
                            loading = false,
                            userId = null,
                            email = null,
                            isAnonymous = false,
                            authLoading = false
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
                            authError = null
                        )
                    }
                    restoreProfileIfNeeded(user.uid)
                }
            }
        }
        viewModelScope.launch {
            combine(
                preferences.role,
                preferences.name,
                preferences.onboarded,
                preferences.biometricEnabled,
                preferences.biometricAsked
            ) { role, name, onboarded, biometricEnabled, biometricAsked ->
                LocalPreferences(role, name, onboarded, biometricEnabled, biometricAsked)
            }.collect { local ->
                _state.update {
                    it.copy(
                        role = local.role?.let { value ->
                            UserRole.entries.firstOrNull { entry -> entry.name == value }
                        },
                        name = local.name,
                        onboarded = local.onboarded,
                        biometricEnabled = local.biometricEnabled,
                        biometricAsked = local.biometricAsked
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
                preferences.restoreProfile(role.name, profile.name.orEmpty())
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
                    val message = if (throwable is FirebaseAuthUserCollisionException) {
                        "Ese correo ya está en uso. Prueba con otro para no perder los datos de esta sesión."
                    } else {
                        authErrorMessage(throwable)
                    }
                    _state.update {
                        it.copy(authLoading = false, authError = message)
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
                    _state.update { it.copy(authError = authErrorMessage(throwable)) }
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
                            authError = authErrorMessage(throwable)
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
        profileRestoredForUid = null
        viewModelScope.launch {
            val uid = authRepository.currentUserId
            if (uid != null) {
                runCatching {
                    withTimeoutOrNull(3_000) {
                        val token = FirebaseMessaging.getInstance().token.await()
                        userRepository.deleteDeviceToken(uid, token)
                    }
                }
            }
            authRepository.signOut()
            preferences.clearAll()
            offlineStore.clearAll()
            _state.update { AppUiState(loading = false) }
        }
    }

    fun selectRole(role: UserRole) {
        viewModelScope.launch {
            preferences.setRole(role.name)
            authRepository.currentUserId?.let { uid ->
                runCatching { userRepository.saveRole(uid, role.name) }
            }
        }
    }

    fun completeOnboarding(name: String) {
        viewModelScope.launch {
            preferences.completeOnboarding(name)
            val uid = authRepository.currentUserId ?: return@launch
            val role = preferences.role.first() ?: return@launch
            runCatching { userRepository.saveProfile(uid, role, name.trim()) }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBiometricEnabled(enabled)
        }
    }

    fun markBiometricAsked() {
        viewModelScope.launch {
            preferences.markBiometricAsked()
        }
    }

    fun goBackToRoleSelection() {
        viewModelScope.launch {
            preferences.clearRole()
            _state.update { it.copy(role = null, onboarded = false) }
        }
    }

    private fun authErrorMessage(throwable: Throwable): String = when (throwable) {
        is FirebaseAuthWeakPasswordException ->
            "La contraseña es demasiado débil. Usa al menos 6 caracteres."
        is FirebaseAuthInvalidCredentialsException ->
            "Correo o contraseña incorrectos."
        is FirebaseAuthInvalidUserException ->
            "No encontramos una cuenta con este correo."
        is FirebaseAuthUserCollisionException ->
            "Ese correo ya está registrado. Inicia sesión con él."
        is FirebaseAuthRecentLoginRequiredException ->
            "Por seguridad, vuelve a iniciar sesión e inténtalo de nuevo."
        is FirebaseNetworkException ->
            "No pudimos conectar. Revisa tu conexión a internet."
        is FirebaseTooManyRequestsException ->
            "Demasiados intentos. Espera un momento y vuelve a intentarlo."
        is FirebaseAuthException ->
            if (throwable.errorCode == "ERROR_OPERATION_NOT_ALLOWED") {
                "Este método de inicio de sesión no está habilitado. Revisa la configuración de Firebase."
            } else {
                throwable.message ?: "No pudimos completar el inicio de sesión."
            }
        else -> throwable.message ?: "Ocurrió un error inesperado."
    }
}
