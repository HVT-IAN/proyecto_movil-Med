package com.meds.app.presentation.patient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.meds.app.core.ui.theme.calma.CalmaTheme
import com.meds.app.di.MedsViewModelFactory
import com.meds.app.integration.widget.NextDoseWidgetProvider
import com.meds.app.navigation.PatientNavGraph
import com.meds.app.navigation.Routes
import com.meds.app.presentation.patient.companion.PatientCompanionViewModel
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
    onBiometricChange: (Boolean) -> Unit
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
    var tutorialAutoChecked by rememberSaveable(account.userId) { mutableStateOf(false) }

    LaunchedEffect(tutorialSeen, tutorialLoaded, state.loading) {
        if (
            tutorialLoaded &&
            !tutorialSeen &&
            !state.loading &&
            !tutorialAutoChecked
        ) {
            tutorialAutoChecked = true
            tutorialVisible = true
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

    CalmaTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (currentRoute in Routes.Patient.topLevelDestinations) {
                    PatientBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(Routes.Patient.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                    onShowTutorial = { tutorialVisible = true },
                    onLinkEmail = onLinkEmail,
                    onSignOut = onSignOut,
                    onClearAuthMessages = onClearAuthMessages,
                    onBiometricChange = onBiometricChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (tutorialVisible) {
            FirstRunTutorial(
                onDismiss = {
                    tutorialVisible = false
                    if (!tutorialSeen) {
                        onTutorialSeen()
                    }
                }
            )
        }
    }
}
