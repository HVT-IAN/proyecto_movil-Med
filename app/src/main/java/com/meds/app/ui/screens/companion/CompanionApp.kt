package com.meds.app.ui.screens.companion

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val ROUTE_LINK = "link"
private const val ROUTE_PANEL = "panel"

@Composable
fun CompanionApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_LINK
    ) {
        composable(ROUTE_LINK) {
            CompanionLinkScreen(
                onContinue = { navController.navigate(ROUTE_PANEL) }
            )
        }
        composable(ROUTE_PANEL) {
            CompanionPanelScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
