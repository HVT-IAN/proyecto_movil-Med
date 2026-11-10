package com.meds.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meds.app.ui.screens.auth.AnonymousWarningScreen
import com.meds.app.ui.screens.auth.AuthMode
import com.meds.app.ui.screens.auth.AuthScreen
import com.meds.app.ui.screens.companion.CompanionApp
import com.meds.app.ui.screens.onboarding.OnboardingScreen
import com.meds.app.ui.screens.patient.PatientApp
import com.meds.app.ui.screens.role.RoleScreen
import com.meds.app.ui.screens.settings.AccountUiState
import com.meds.app.ui.screens.shared.BiometricGate
import com.meds.app.ui.screens.shared.LoadingScreen
import kotlinx.coroutines.delay

@Composable
fun MedsRoot(
    viewModel: AppViewModel = viewModel(),
    onExit: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val role = state.role
    var authMode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
    var showAnonymousWarning by rememberSaveable { mutableStateOf(false) }
    var splashVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(600L)
        splashVisible = false
    }

    LaunchedEffect(state.userId) {
        if (state.userId != null) {
            authMode = AuthMode.LOGIN
            showAnonymousWarning = false
        }
    }

    BackHandler(
        enabled = state.userId == null &&
            showAnonymousWarning &&
            !state.authLoading
    ) {
        showAnonymousWarning = false
        viewModel.clearAuthMessages()
    }
    BackHandler(
        enabled = state.userId == null &&
            !showAnonymousWarning &&
            authMode == AuthMode.REGISTER &&
            !state.authLoading
    ) {
        authMode = AuthMode.LOGIN
        viewModel.clearAuthMessages()
    }

    when {
        state.userId == null -> {
            if (state.loading || splashVisible) {
                LoadingScreen()
            } else if (showAnonymousWarning) {
                AnonymousWarningScreen(
                    authLoading = state.authLoading,
                    authError = state.authError,
                    onContinueAnonymously = viewModel::continueAnonymously,
                    onCreateAccount = {
                        showAnonymousWarning = false
                        authMode = AuthMode.REGISTER
                        viewModel.clearAuthMessages()
                    },
                    onBack = {
                        showAnonymousWarning = false
                        viewModel.clearAuthMessages()
                    }
                )
            } else {
                AuthScreen(
                    mode = authMode,
                    authLoading = state.authLoading,
                    authError = state.authError,
                    authNotice = state.authNotice,
                    onModeChange = { mode ->
                        authMode = mode
                        viewModel.clearAuthMessages()
                    },
                    onLogin = viewModel::login,
                    onRegister = viewModel::register,
                    onAnonymous = {
                        viewModel.clearAuthMessages()
                        showAnonymousWarning = true
                    },
                    onSendPasswordReset = viewModel::sendPasswordReset,
                    onClearAuthMessages = viewModel::clearAuthMessages
                )
            }
        }

        state.onboarded && role != null -> {
            val account = AccountUiState(
                userId = state.userId.orEmpty(),
                name = state.name,
                role = role,
                email = state.email,
                isAnonymous = state.isAnonymous,
                authLoading = state.authLoading,
                authError = state.authError,
                authNotice = state.authNotice
            )
            BiometricGate {
                if (state.loading) {
                    LoadingScreen()
                } else if (role == UserRole.PATIENT) {
                    PatientApp(
                        account = account,
                        onLinkEmail = viewModel::linkEmail,
                        onSignOut = viewModel::signOut,
                        onClearAuthMessages = viewModel::clearAuthMessages
                    )
                } else {
                    CompanionApp(
                        account = account,
                        onLinkEmail = viewModel::linkEmail,
                        onSignOut = viewModel::signOut,
                        onClearAuthMessages = viewModel::clearAuthMessages
                    )
                }
            }
        }

        state.loading -> LoadingScreen()

        role == null -> RoleScreen(
            onSelectRole = viewModel::selectRole,
            onBack = onExit
        )

        !state.onboarded -> OnboardingScreen(
            role = role,
            onFinish = viewModel::completeOnboarding,
            onBack = viewModel::goBackToRoleSelection
        )

        else -> LoadingScreen()
    }
}
