package com.meds.app.ui.screens.patient

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.messaging.FirebaseMessaging
import com.meds.app.BuildConfig
import com.meds.app.data.repository.UserRepository
import com.meds.app.ui.screens.settings.AccountSettingsScreen
import com.meds.app.ui.screens.settings.AccountUiState
import com.meds.app.widget.NextDoseWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

private const val ROUTE_HOME = "home"
private const val ROUTE_MEDICATIONS = "medications"
private const val ROUTE_GARDEN = "garden"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_COMPANIONS = "companions"
private const val ROUTE_EDITOR_NEW = "medication_editor/new"
private const val ROUTE_EDITOR_PATTERN = "medication_editor/{medicationId}"
private const val ROUTE_MEDICATION_INFO = "medication_info?query={query}"

private fun medicationInfoRoute(query: String): String =
    "medication_info?query=${Uri.encode(query)}"

@Composable
fun PatientApp(
    account: AccountUiState,
    onLinkEmail: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onClearAuthMessages: () -> Unit,
    onBiometricChange: (Boolean) -> Unit
) {
    val name = account.name
    val patientViewModel: PatientViewModel = viewModel(key = "patient_${account.userId}")
    val companionViewModel: PatientCompanionViewModel =
        viewModel(key = "patient_companions_${account.userId}")
    val state by patientViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelRoutes = setOf(ROUTE_HOME, ROUTE_MEDICATIONS, ROUTE_GARDEN, ROUTE_SETTINGS)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }

    LaunchedEffect(account.userId) {
        if (BuildConfig.USE_FIREBASE_EMULATOR) return@LaunchedEffect
        repeat(3) { attempt ->
            val saved = runCatching {
                val token = FirebaseMessaging.getInstance().token.await()
                userRepository.saveDeviceToken(account.userId, token)
            }.isSuccess
            if (saved) return@LaunchedEffect
            delay(2_000L * (attempt + 1))
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == ROUTE_HOME,
                        onClick = {
                            navController.navigate(ROUTE_HOME) {
                                popUpTo(ROUTE_HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = null
                            )
                        },
                        label = { Text("Hoy") },
                        colors = navigationColors()
                    )
                    NavigationBarItem(
                        selected = currentRoute == ROUTE_MEDICATIONS,
                        onClick = {
                            navController.navigate(ROUTE_MEDICATIONS) {
                                popUpTo(ROUTE_HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Medication,
                                contentDescription = null
                            )
                        },
                        label = { Text("Medicamentos") },
                        colors = navigationColors()
                    )
                    NavigationBarItem(
                        selected = currentRoute == ROUTE_GARDEN,
                        onClick = {
                            navController.navigate(ROUTE_GARDEN) {
                                popUpTo(ROUTE_HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Eco,
                                contentDescription = null
                            )
                        },
                        label = { Text("Jardín") },
                        colors = navigationColors()
                    )
                    NavigationBarItem(
                        selected = currentRoute == ROUTE_SETTINGS,
                        onClick = {
                            navController.navigate(ROUTE_SETTINGS) {
                                popUpTo(ROUTE_HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null
                            )
                        },
                        label = { Text("Ajustes") },
                        colors = navigationColors()
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_HOME) {
                HomeScreen(
                    name = name,
                    state = state,
                    onAddMedication = { navController.navigate(ROUTE_EDITOR_NEW) },
                    onTakeDose = patientViewModel::takeDose,
                    onSnoozeDose = patientViewModel::snoozeDose,
                    onViewAll = { navController.navigate(ROUTE_HISTORY) },
                    onOpenCompanions = { navController.navigate(ROUTE_COMPANIONS) },
                    onMarkNudgeRead = patientViewModel::markNudgeRead
                )
            }
            composable(ROUTE_MEDICATIONS) {
                MedicationsScreen(
                    state = state,
                    onAddMedication = { navController.navigate(ROUTE_EDITOR_NEW) },
                    onEditMedication = { medicationId ->
                        navController.navigate("medication_editor/$medicationId")
                    }
                )
            }
            composable(ROUTE_GARDEN) {
                GardenScreen(plant = state.plant)
            }
            composable(ROUTE_SETTINGS) {
                AccountSettingsScreen(
                    account = account,
                    onBack = { navController.popBackStack() },
                    onLinkEmail = onLinkEmail,
                    onSignOut = onSignOut,
                    onClearAuthMessages = onClearAuthMessages,
                    onBiometricChange = onBiometricChange
                )
            }
            composable(ROUTE_HISTORY) {
                HistoryScreen(
                    doses = state.recentDoses,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ROUTE_COMPANIONS) {
                PatientCompanionScreen(
                    viewModel = companionViewModel,
                    patientName = name,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = ROUTE_MEDICATION_INFO,
                arguments = listOf(
                    navArgument("query") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entry ->
                val query = entry.arguments?.getString("query").orEmpty()
                MedicationInfoScreen(
                    initialQuery = query,
                    onBack = { navController.popBackStack() },
                    onUseName = { selectedName ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_medication_name", selectedName)
                        navController.popBackStack()
                    }
                )
            }
            composable(ROUTE_EDITOR_NEW) { entry ->
                val selectedName by entry.savedStateHandle
                    .getStateFlow<String?>("selected_medication_name", null)
                    .collectAsStateWithLifecycle()
                MedicationEditorScreen(
                    viewModel = patientViewModel,
                    medication = null,
                    schedule = null,
                    selectedName = selectedName,
                    onOpenInfo = { query ->
                        navController.navigate(medicationInfoRoute(query))
                    },
                    onConsumeSelectedName = {
                        entry.savedStateHandle["selected_medication_name"] = null
                    },
                    onDone = { navController.popBackStack() }
                )
            }
            composable(ROUTE_EDITOR_PATTERN) { entry ->
                val medicationId = entry.arguments?.getString("medicationId")
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
                        .getStateFlow<String?>("selected_medication_name", null)
                        .collectAsStateWithLifecycle()
                    MedicationEditorScreen(
                        viewModel = patientViewModel,
                        medication = medication,
                        schedule = schedule,
                        selectedName = selectedName,
                        onOpenInfo = { query ->
                            navController.navigate(medicationInfoRoute(query))
                        },
                        onConsumeSelectedName = {
                            entry.savedStateHandle["selected_medication_name"] = null
                        },
                        onDone = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun navigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)
