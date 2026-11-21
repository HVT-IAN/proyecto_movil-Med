package com.meds.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.meds.app.core.ui.theme.calma.CalmaMotion
import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.PatientNavBarStyle
import com.meds.app.presentation.patient.cima.MedicationInfoScreen
import com.meds.app.presentation.patient.companion.PatientCompanionScreen
import com.meds.app.presentation.patient.companion.PatientCompanionViewModel
import com.meds.app.presentation.patient.garden.GardenScreen
import com.meds.app.presentation.patient.history.HistoryScreen
import com.meds.app.presentation.patient.home.HomeScreen
import com.meds.app.presentation.patient.medications.MedicationEditorScreen
import com.meds.app.presentation.patient.medications.MedicationsScreen
import com.meds.app.presentation.patient.PatientUiState
import com.meds.app.presentation.patient.PatientViewModel
import com.meds.app.presentation.settings.AccountSettingsScreen
import com.meds.app.presentation.settings.AccountUiState
import com.meds.app.presentation.settings.ReminderSettingsScreen

/**
 * Destinos internos del módulo paciente.
 *
 * La estructura visual (barra inferior, snackbars y efectos) vive en
 * `PatientApp`; este grafo solo declara pantallas y transiciones.
 */
@Composable
fun PatientNavGraph(
    navController: NavHostController,
    account: AccountUiState,
    state: PatientUiState,
    patientViewModel: PatientViewModel,
    companionViewModel: PatientCompanionViewModel,
    hasCompanion: Boolean,
    companionsLoading: Boolean,
    onShowTutorial: () -> Unit,
    onLinkEmail: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onClearAuthMessages: () -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onNavBarStyleChange: (PatientNavBarStyle) -> Unit = {},
    onFocusDose: (DoseLog) -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Patient.HOME,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(CalmaMotion.NAV_FADE_MILLIS))
        },
        exitTransition = {
            fadeOut(tween(CalmaMotion.NAV_FADE_MILLIS))
        },
        popEnterTransition = {
            fadeIn(tween(CalmaMotion.NAV_FADE_MILLIS))
        },
        popExitTransition = {
            fadeOut(tween(CalmaMotion.NAV_FADE_MILLIS))
        }
    ) {
        composable(Routes.Patient.HOME) {
            LaunchedEffect(state.saveSuccess) {
                if (state.saveSuccess) {
                    patientViewModel.consumeSaveSuccess()
                }
            }
            HomeScreen(
                name = account.name,
                state = state,
                hasCompanion = hasCompanion,
                companionsLoading = companionsLoading,
                onAddMedication = {
                    navController.navigate(Routes.Patient.EDITOR_NEW)
                },
                onTakeDose = patientViewModel::takeDose,
                onSnoozeDose = patientViewModel::snoozeDose,
                onOpenFocus = onFocusDose,
                onViewAll = {
                    navController.navigate(Routes.Patient.HISTORY)
                },
                onOpenCompanions = {
                    navController.navigate(Routes.Patient.COMPANIONS)
                },
                onMarkNudgeRead = patientViewModel::markNudgeRead,
                onQuickAddMedication = patientViewModel::saveMedication
            )
        }

        composable(Routes.Patient.MEDICATIONS) {
            MedicationsScreen(
                state = state,
                onAddMedication = {
                    navController.navigate(Routes.Patient.EDITOR_NEW)
                },
                onEditMedication = { medicationId ->
                    navController.navigate(Routes.Patient.medicationEditor(medicationId))
                }
            )
        }

        composable(Routes.Patient.GARDEN) {
            GardenScreen(
                plant = state.plant
            )
        }

        composable(Routes.Patient.SETTINGS) {
            AccountSettingsScreen(
                account = account,
                onBack = { navController.popBackStack() },
                onOpenCompanions = {
                    navController.navigate(Routes.Patient.COMPANIONS)
                },
                onShowTutorial = onShowTutorial,
                onLinkEmail = onLinkEmail,
                onSignOut = onSignOut,
                onClearAuthMessages = onClearAuthMessages,
                onBiometricChange = onBiometricChange,
                onNavBarStyleChange = onNavBarStyleChange,
                onOpenReminders = {
                    navController.navigate(Routes.Patient.REMINDERS)
                }
            )
        }

        composable(Routes.Patient.HISTORY) {
            HistoryScreen(
                doses = state.recentDoses,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Patient.REMINDERS) {
            ReminderSettingsScreen(
                medications = state.medications,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Patient.COMPANIONS) {
            PatientCompanionScreen(
                viewModel = companionViewModel,
                patientName = account.name,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.Patient.MEDICATION_INFO_PATTERN,
            arguments = listOf(
                navArgument(Routes.Patient.ARG_QUERY) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entry ->
            val query = entry.arguments
                ?.getString(Routes.Patient.ARG_QUERY)
                .orEmpty()
            MedicationInfoScreen(
                initialQuery = query,
                onBack = { navController.popBackStack() },
                onUseName = { selectedName ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(Routes.Patient.RESULT_SELECTED_MEDICATION_NAME, selectedName)
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.Patient.EDITOR_NEW) { entry ->
            val selectedName by entry.savedStateHandle
                .getStateFlow<String?>(
                    Routes.Patient.RESULT_SELECTED_MEDICATION_NAME,
                    null
                )
                .collectAsStateWithLifecycle()
            MedicationEditorScreen(
                viewModel = patientViewModel,
                medication = null,
                schedule = null,
                selectedName = selectedName,
                onOpenInfo = { query ->
                    navController.navigate(Routes.Patient.medicationInfo(query))
                },
                onConsumeSelectedName = {
                    entry.savedStateHandle[Routes.Patient.RESULT_SELECTED_MEDICATION_NAME] = null
                },
                onDone = { navController.popBackStack() }
            )
        }

        composable(Routes.Patient.EDITOR_PATTERN) { entry ->
            val medicationId = entry.arguments
                ?.getString(Routes.Patient.ARG_MEDICATION_ID)
            val medication = state.medications.firstOrNull { it.id == medicationId }
            val schedule = state.schedules.firstOrNull { it.medicationId == medicationId }
            if (medication == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                val selectedName by entry.savedStateHandle
                    .getStateFlow<String?>(
                        Routes.Patient.RESULT_SELECTED_MEDICATION_NAME,
                        null
                    )
                    .collectAsStateWithLifecycle()
                MedicationEditorScreen(
                    viewModel = patientViewModel,
                    medication = medication,
                    schedule = schedule,
                    selectedName = selectedName,
                    onOpenInfo = { query ->
                        navController.navigate(Routes.Patient.medicationInfo(query))
                    },
                    onConsumeSelectedName = {
                        entry.savedStateHandle[Routes.Patient.RESULT_SELECTED_MEDICATION_NAME] = null
                    },
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}
