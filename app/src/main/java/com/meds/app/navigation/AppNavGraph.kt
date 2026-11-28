package com.meds.app.navigation

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meds.app.core.ui.components.LoadingScreen
import com.meds.app.core.ui.theme.LocalReduceMotion
import com.meds.app.core.ui.theme.MedsTheme
import com.meds.app.core.ui.theme.calma.CalmaMotion
import com.meds.app.di.MedsViewModelFactory
import com.meds.app.domain.model.UserRole
import com.meds.app.domain.model.resolveDarkTheme
import com.meds.app.integration.biometric.BiometricGate
import com.meds.app.integration.biometric.BiometricOptInDialog
import com.meds.app.integration.biometric.isBiometricAvailable
import com.meds.app.integration.biometric.isDeviceCredentialAvailable
import com.meds.app.presentation.auth.AuthMode
import com.meds.app.presentation.auth.AuthScreen
import com.meds.app.presentation.companion.CompanionApp
import com.meds.app.presentation.onboarding.OnboardingScreen
import com.meds.app.presentation.patient.PatientApp
import com.meds.app.presentation.role.RoleScreen
import com.meds.app.presentation.root.AppViewModel
import com.meds.app.presentation.settings.AccountUiState
import kotlinx.coroutines.delay

private const val SPLASH_MAX_SAFETY_MILLIS = 8_000L

private enum class AppStage {
    SPLASH,
    AUTH,
    ROLE,
    ONBOARDING,
    PATIENT,
    COMPANION
}

@Composable
fun AppNavGraph(
    viewModel: AppViewModel = viewModel(factory = MedsViewModelFactory),
    onExit: () -> Unit = {},
    initialFocusLogId: String? = null,
    onFocusConsumed: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val darkTheme = resolveDarkTheme(state.themeMode, isSystemInDarkTheme())
    val role = state.role
    val context = LocalContext.current
    val biometricAvailable = remember(context) { isBiometricAvailable(context) }
    val deviceCredentialAvailable = remember(context) { isDeviceCredentialAvailable(context) }
    val protectionAvailable = biometricAvailable || deviceCredentialAvailable
    var authMode by rememberSaveable { mutableStateOf(AuthMode.LOGIN) }
    var splashVisible by rememberSaveable { mutableStateOf(true) }
    val splashStartedAt = remember { SystemClock.elapsedRealtime() }

    LaunchedEffect(state.loading, splashVisible) {
        if (splashVisible && !state.loading) {
            if (!state.reduceMotion) {
                val elapsed = SystemClock.elapsedRealtime() - splashStartedAt
                delay((CalmaMotion.SPLASH_MIN_VISIBLE_MILLIS - elapsed).coerceAtLeast(0L))
            }
            splashVisible = false
        }
    }

    LaunchedEffect(splashVisible) {
        if (splashVisible) {
            delay(SPLASH_MAX_SAFETY_MILLIS)
            splashVisible = false
        }
    }

    LaunchedEffect(state.userId) {
        if (state.userId != null) {
            authMode = AuthMode.LOGIN
        }
    }

    LaunchedEffect(protectionAvailable, state.biometricAsked) {
        if (!state.biometricAsked && !protectionAvailable) {
            viewModel.markBiometricAsked()
        }
    }

    MedsTheme(
        darkTheme = darkTheme,
        reduceMotion = state.reduceMotion,
        highContrast = state.highContrast,
        graphLabels = state.graphLabels
    ) {
        val reduceMotion = LocalReduceMotion.current

        BackHandler(
            enabled = state.userId == null &&
                authMode == AuthMode.REGISTER &&
                !state.authLoading
        ) {
            authMode = AuthMode.LOGIN
            viewModel.clearAuthMessages()
        }

        val showSplash = state.loading || splashVisible
        val stage = when {
            state.userId == null && showSplash -> AppStage.SPLASH
            state.userId == null -> AppStage.AUTH
            state.onboarded && role != null && showSplash -> AppStage.SPLASH
            state.onboarded && role != null && role == UserRole.PATIENT -> AppStage.PATIENT
            state.onboarded && role != null -> AppStage.COMPANION
            showSplash -> AppStage.SPLASH
            role == null -> AppStage.ROLE
            !state.onboarded -> AppStage.ONBOARDING
            else -> AppStage.SPLASH
        }

        AnimatedContent(
            targetState = stage,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                if (reduceMotion) {
                    (fadeIn(tween(0)) togetherWith fadeOut(tween(0)))
                        .using(SizeTransform(clip = false))
                } else {
                    (fadeIn(tween(CalmaMotion.SCREEN_FADE_MILLIS)) togetherWith
                        fadeOut(tween(CalmaMotion.SCREEN_FADE_MILLIS - 60)))
                        .using(SizeTransform(clip = false))
                }
            },
            label = "app-stage"
        ) { target ->
            when (target) {
                AppStage.SPLASH -> {
                    LoadingScreen()
                }

                AppStage.AUTH -> {
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
                        onAnonymous = viewModel::continueAnonymously,
                        onSendPasswordReset = viewModel::sendPasswordReset,
                        onClearAuthMessages = viewModel::clearAuthMessages
                    )
                }

                AppStage.PATIENT -> {
                    val account = AccountUiState(
                        userId = state.userId.orEmpty(),
                        name = state.name,
                        role = UserRole.PATIENT,
                        email = state.email,
                        isAnonymous = state.isAnonymous,
                        biometricEnabled = state.biometricEnabled,
                        biometricAvailable = biometricAvailable,
                        deviceCredentialAvailable = deviceCredentialAvailable,
                        navBarStyle = state.navBarStyle,
                        themeMode = state.themeMode,
                        reduceMotion = state.reduceMotion,
                        highContrast = state.highContrast,
                        graphLabels = state.graphLabels,
                        authLoading = state.authLoading,
                        authError = state.authError,
                        authNotice = state.authNotice
                    )
                    BiometricGate(
                        enabled = state.biometricEnabled,
                        isAnonymous = state.isAnonymous,
                        onSignOut = viewModel::signOut
                    ) {
                        PatientApp(
                            account = account,
                            tutorialSeen = state.tutorialSeen,
                            tutorialLoaded = state.tutorialLoaded,
                            onTutorialSeen = viewModel::markTutorialSeen,
                            onLinkEmail = viewModel::linkEmail,
                            onSignOut = viewModel::signOut,
                            onClearAuthMessages = viewModel::clearAuthMessages,
                            onBiometricChange = viewModel::setBiometricEnabled,
                            onNavBarStyleChange = viewModel::setNavBarStyle,
                            onThemeModeChange = viewModel::setThemeMode,
                            onReduceMotionChange = viewModel::setReduceMotion,
                            onHighContrastChange = viewModel::setHighContrast,
                            onGraphLabelsChange = viewModel::setGraphLabels,
                            initialFocusLogId = initialFocusLogId,
                            onFocusConsumed = onFocusConsumed
                        )
                    }
                    if (!state.biometricAsked && protectionAvailable) {
                        BiometricOptInDialog(
                            onEnable = { viewModel.setBiometricEnabled(true) },
                            onDismiss = viewModel::markBiometricAsked
                        )
                    }
                }

                AppStage.COMPANION -> {
                    val account = AccountUiState(
                        userId = state.userId.orEmpty(),
                        name = state.name,
                        role = role ?: UserRole.COMPANION,
                        email = state.email,
                        isAnonymous = state.isAnonymous,
                        biometricEnabled = state.biometricEnabled,
                        biometricAvailable = biometricAvailable,
                        deviceCredentialAvailable = deviceCredentialAvailable,
                        themeMode = state.themeMode,
                        reduceMotion = state.reduceMotion,
                        highContrast = state.highContrast,
                        graphLabels = state.graphLabels,
                        authLoading = state.authLoading,
                        authError = state.authError,
                        authNotice = state.authNotice
                    )
                    BiometricGate(
                        enabled = state.biometricEnabled,
                        isAnonymous = state.isAnonymous,
                        onSignOut = viewModel::signOut
                    ) {
                        CompanionApp(
                            account = account,
                            onLinkEmail = viewModel::linkEmail,
                            onSignOut = viewModel::signOut,
                            onClearAuthMessages = viewModel::clearAuthMessages,
                            onBiometricChange = viewModel::setBiometricEnabled
                        )
                    }
                    if (!state.biometricAsked && protectionAvailable) {
                        BiometricOptInDialog(
                            onEnable = { viewModel.setBiometricEnabled(true) },
                            onDismiss = viewModel::markBiometricAsked
                        )
                    }
                }

                AppStage.ROLE -> {
                    RoleScreen(
                        onSelectRole = viewModel::selectRole,
                        onBack = onExit
                    )
                }

                AppStage.ONBOARDING -> {
                    OnboardingScreen(
                        role = role ?: UserRole.PATIENT,
                        onFinish = viewModel::completeOnboarding,
                        onBack = viewModel::goBackToRoleSelection
                    )
                }
            }
        }
    }
}
