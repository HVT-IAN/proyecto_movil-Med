package com.meds.app.presentation.patient

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.meds.app.BuildConfig
import com.meds.app.data.repository.UserRepository
import com.meds.app.integration.widget.NextDoseWidgetProvider
import com.meds.app.navigation.PatientNavGraph
import com.meds.app.navigation.Routes
import com.meds.app.presentation.settings.AccountUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

/**
 * Contenedor del módulo paciente.
 *
 * Se encarga de la sesión, los efectos globales (token FCM, tomas del día y
 * widget), los avisos y la barra inferior. Las pantallas y transiciones están
 * declaradas en `navigation/PatientNavGraph.kt`.
 */
@Composable
fun PatientApp(
    account: AccountUiState,
    onLinkEmail: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onClearAuthMessages: () -> Unit,
    onBiometricChange: (Boolean) -> Unit
) {
    val patientViewModel: PatientViewModel = viewModel(key = "patient_${account.userId}")
    val companionViewModel: PatientCompanionViewModel =
        viewModel(key = "patient_companions_${account.userId}")
    val state by patientViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
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
        PatientNavGraph(
            navController = navController,
            account = account,
            state = state,
            patientViewModel = patientViewModel,
            companionViewModel = companionViewModel,
            onLinkEmail = onLinkEmail,
            onSignOut = onSignOut,
            onClearAuthMessages = onClearAuthMessages,
            onBiometricChange = onBiometricChange,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
