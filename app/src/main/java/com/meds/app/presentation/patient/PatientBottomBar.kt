package com.meds.app.presentation.patient

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.meds.app.navigation.Routes

private data class PatientDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val patientDestinations = listOf(
    PatientDestination(Routes.Patient.HOME, "Hoy", Icons.Outlined.Home),
    PatientDestination(Routes.Patient.MEDICATIONS, "Medicamentos", Icons.Outlined.Medication),
    PatientDestination(Routes.Patient.SETTINGS, "Ajustes", Icons.Outlined.Settings)
)

/**
 * Barra inferior fija con los tres destinos del día a día.
 *
 * Historial, Jardín y Acompañante se abren desde enlaces contextuales, no
 * desde aquí. El indicador animado se desactiva para mantener la barra plana.
 */
@Composable
internal fun PatientBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            patientDestinations.forEach { destination ->
                NavigationBarItem(
                    selected = currentRoute == destination.route,
                    onClick = { onNavigate(destination.route) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null
                        )
                    },
                    label = { Text(destination.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
