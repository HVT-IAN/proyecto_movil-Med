package com.meds.app.ui.screens.patient

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private const val ROUTE_HOME = "home"
private const val ROUTE_MEDICATIONS = "medications"
private const val ROUTE_GARDEN = "garden"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_COMPANIONS = "companions"
private const val ROUTE_EDITOR_NEW = "medication_editor/new"
private const val ROUTE_EDITOR_PATTERN = "medication_editor/{medicationId}"

@Composable
fun PatientApp(name: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelRoutes = setOf(ROUTE_HOME, ROUTE_MEDICATIONS, ROUTE_GARDEN)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
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
                    onAddMedication = { navController.navigate(ROUTE_EDITOR_NEW) },
                    onOpenCompanions = { navController.navigate(ROUTE_COMPANIONS) },
                    onViewHistory = { navController.navigate(ROUTE_HISTORY) }
                )
            }
            composable(ROUTE_MEDICATIONS) {
                MedicationsScreen(
                    onAddMedication = { navController.navigate(ROUTE_EDITOR_NEW) },
                    onEditMedication = { medicationId ->
                        navController.navigate("medication_editor/$medicationId")
                    }
                )
            }
            composable(ROUTE_GARDEN) {
                GardenScreen()
            }
            composable(ROUTE_HISTORY) {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_COMPANIONS) {
                PatientCompanionScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_EDITOR_NEW) {
                MedicationEditorScreen(
                    medicationId = null,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(ROUTE_EDITOR_PATTERN) { entry ->
                MedicationEditorScreen(
                    medicationId = entry.arguments?.getString("medicationId"),
                    onBack = { navController.popBackStack() }
                )
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
