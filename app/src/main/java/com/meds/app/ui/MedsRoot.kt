package com.meds.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.meds.app.ui.screens.companion.CompanionApp
import com.meds.app.ui.screens.onboarding.OnboardingScreen
import com.meds.app.ui.screens.patient.PatientApp
import com.meds.app.ui.screens.role.RoleScreen

private const val ROUTE_ROLE = "role"
private const val ROUTE_ONBOARDING = "onboarding/{role}"
private const val ROUTE_PATIENT = "patient"
private const val ROUTE_COMPANION = "companion"

@Composable
fun MedsRoot() {
    val navController = rememberNavController()
    var patientName by rememberSaveable { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = ROUTE_ROLE
    ) {
        composable(ROUTE_ROLE) {
            RoleScreen(
                onPatient = { navController.navigate("onboarding/PATIENT") },
                onCompanion = { navController.navigate("onboarding/COMPANION") }
            )
        }

        composable(ROUTE_ONBOARDING) { entry ->
            val role = entry.arguments?.getString("role").orEmpty()
            OnboardingScreen(
                role = role,
                onContinue = { name ->
                    if (role == "PATIENT") {
                        patientName = name
                    }
                    val destination = if (role == "PATIENT") ROUTE_PATIENT else ROUTE_COMPANION
                    navController.navigate(destination) {
                        popUpTo(ROUTE_ROLE) { inclusive = true }
                    }
                }
            )
        }

        composable(ROUTE_PATIENT) {
            PatientApp(name = patientName.ifBlank { "Paciente" })
        }

        composable(ROUTE_COMPANION) {
            CompanionApp()
        }
    }
}
