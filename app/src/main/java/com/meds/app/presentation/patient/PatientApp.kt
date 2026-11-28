package com.meds.app.presentation.patient

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.meds.app.core.ui.theme.LocalReduceMotion
import com.meds.app.core.ui.theme.calma.CalmaMotion
import com.meds.app.core.ui.theme.calma.CalmaTheme
import com.meds.app.di.MedsViewModelFactory
import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.DoseStatus
import com.meds.app.domain.model.PatientNavBarStyle
import com.meds.app.domain.model.ThemeMode
import com.meds.app.integration.widget.NextDoseWidgetProvider
import com.meds.app.navigation.PatientNavGraph
import com.meds.app.navigation.Routes
import com.meds.app.presentation.patient.companion.PatientCompanionViewModel
import com.meds.app.presentation.patient.focus.FocusDoseScreen
import com.meds.app.presentation.patient.tutorial.FirstRunTutorial
import com.meds.app.presentation.settings.AccountUiState

/**
 * Contenedor del módulo paciente.
 *
 * Se encarga de la sesión, los efectos globales (tomas del día y widget),
 * los avisos y la barra inferior. Las pantallas y transiciones están
 * declaradas en `navigation/PatientNavGraph.kt`.
 */
@Composable
fun PatientApp(
    account: AccountUiState,
    tutorialSeen: Boolean,
    tutorialLoaded: Boolean,
    onTutorialSeen: () -> Unit,
    onLinkEmail: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onClearAuthMessages: () -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onNavBarStyleChange: (PatientNavBarStyle) -> Unit = {},
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onReduceMotionChange: (Boolean) -> Unit = {},
    onHighContrastChange: (Boolean) -> Unit = {},
    onGraphLabelsChange: (Boolean) -> Unit = {},
    initialFocusLogId: String? = null,
    onFocusConsumed: () -> Unit = {}
) {
    val patientViewModel: PatientViewModel =
        viewModel(key = "patient_${account.userId}", factory = MedsViewModelFactory)
    val companionViewModel: PatientCompanionViewModel =
        viewModel(
            key = "patient_companions_${account.userId}",
            factory = MedsViewModelFactory
        )
    val state by patientViewModel.state.collectAsStateWithLifecycle()
    val companionState by companionViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var tutorialVisible by rememberSaveable(account.userId) { mutableStateOf(false) }
    var tutorialIsFirstMedication by rememberSaveable(account.userId) { mutableStateOf(false) }
    var tutorialAutoChecked by rememberSaveable(account.userId) { mutableStateOf(false) }
    var hadEmptyMedications by rememberSaveable(account.userId) { mutableStateOf(false) }
    var focusDoseId by rememberSaveable(account.userId) { mutableStateOf<String?>(null) }

    val focusDose = focusDoseId?.let { id ->
        state.todayDoses.firstOrNull { dose ->
            dose.id == id &&
                (dose.status == DoseStatus.PENDING || dose.status == DoseStatus.SNOOZED)
        }
    }

    // Conserva el último contenido para que la animación de salida tenga algo que mostrar.
    var displayedDose by remember { mutableStateOf<DoseLog?>(null) }
    LaunchedEffect(focusDose) {
        if (focusDose != null) displayedDose = focusDose
    }

    // Solo limpia el id cuando sabemos que la toma ya se completó.
    LaunchedEffect(focusDoseId, state.todayDoses) {
        val id = focusDoseId ?: return@LaunchedEffect
        val match = state.todayDoses.firstOrNull { it.id == id }
        if (match != null && match.status == DoseStatus.TAKEN) {
            focusDoseId = null
        }
    }

    BackHandler(enabled = focusDose != null) {
        focusDoseId = null
    }

    LaunchedEffect(initialFocusLogId) {
        if (initialFocusLogId != null) {
            focusDoseId = initialFocusLogId
            onFocusConsumed()
        }
    }

    LaunchedEffect(
        tutorialSeen,
        tutorialLoaded,
        state.loading,
        state.medications.size,
        currentRoute
    ) {
        if (!state.loading && tutorialLoaded) {
            if (state.medications.isEmpty()) {
                hadEmptyMedications = true
            } else if (
                hadEmptyMedications &&
                !tutorialSeen &&
                !tutorialAutoChecked &&
                currentRoute in Routes.Patient.topLevelDestinations
            ) {
                tutorialAutoChecked = true
                tutorialIsFirstMedication = true
                tutorialVisible = true
            }
        }
    }

    LaunchedEffect(state.medications, state.schedules, state.loading) {
        if (!state.loading) {
            patientViewModel.ensureTodayDoses()
        }
    }

    LaunchedEffect(state.todayDoses, state.loading) {
        if (!state.loading) {
            NextDoseWidgetProvider.update(context.applicationContext, state.todayDoses)
        }
    }

    LaunchedEffect(state.error) {
        val message = state.error
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            patientViewModel.clearError()
        }
    }

    val showBottomBar = currentRoute in Routes.Patient.topLevelDestinations ||
        (account.navBarStyle == PatientNavBarStyle.GRAPH_ICONS &&
            currentRoute == Routes.Patient.COMPANIONS)
    val bottomBarVisible = showBottomBar && focusDose == null

    CalmaTheme {
        val reduceMotion = LocalReduceMotion.current
        val focusFadeMillis = if (reduceMotion) 0 else CalmaMotion.SCREEN_FADE_MILLIS
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = if (bottomBarVisible) {
                        Modifier
                            .navigationBarsPadding()
                            .padding(bottom = 84.dp)
                    } else {
                        Modifier
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(bottom = if (bottomBarVisible) 80.dp else 0.dp)
                ) {
                    PendingSyncBanner(
                        pendingChanges = state.pendingChanges
                    )
                    PatientNavGraph(
                        navController = navController,
                        account = account,
                        state = state,
                        patientViewModel = patientViewModel,
                        companionViewModel = companionViewModel,
                        hasCompanion = companionState.companions.isNotEmpty(),
                        companionsLoading = companionState.loading,
                        onShowTutorial = {
                            tutorialIsFirstMedication = false
                            tutorialVisible = true
                        },
                        onLinkEmail = onLinkEmail,
                        onSignOut = onSignOut,
                        onClearAuthMessages = onClearAuthMessages,
                        onBiometricChange = onBiometricChange,
                        onNavBarStyleChange = onNavBarStyleChange,
                        onThemeModeChange = onThemeModeChange,
                        onReduceMotionChange = onReduceMotionChange,
                        onHighContrastChange = onHighContrastChange,
                        onGraphLabelsChange = onGraphLabelsChange,
                        onFocusDose = { dose -> focusDoseId = dose.id },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (bottomBarVisible) {
                    PatientBottomBar(
                        currentRoute = currentRoute,
                        style = account.navBarStyle,
                        onNavigate = { route ->
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(Routes.Patient.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                AnimatedVisibility(
                    visible = focusDose != null,
                    enter = if (reduceMotion) {
                        fadeIn(tween(0))
                    } else {
                        fadeIn(tween(focusFadeMillis)) +
                            scaleIn(
                                initialScale = 0.96f,
                                animationSpec = tween(focusFadeMillis)
                            )
                    },
                    exit = if (reduceMotion) {
                        fadeOut(tween(0))
                    } else {
                        fadeOut(tween(focusFadeMillis)) +
                            scaleOut(
                                targetScale = 0.98f,
                                animationSpec = tween(focusFadeMillis)
                            )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                ) {
                    val dose = displayedDose
                    if (dose != null) {
                        FocusDoseScreen(
                            dose = dose,
                            onTake = {
                                patientViewModel.takeDose(dose)
                                focusDoseId = null
                            },
                            onSnooze = { minutes ->
                                patientViewModel.snoozeDose(dose, minutes)
                                focusDoseId = null
                            },
                            onClose = { focusDoseId = null },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (tutorialVisible) {
            FirstRunTutorial(
                isFirstMedication = tutorialIsFirstMedication,
                onDismiss = {
                    tutorialVisible = false
                    tutorialIsFirstMedication = false
                    if (!tutorialSeen) {
                        onTutorialSeen()
                    }
                }
            )
        }
    }
}
