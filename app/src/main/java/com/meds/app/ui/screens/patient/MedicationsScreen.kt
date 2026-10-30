package com.meds.app.ui.screens.patient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.ui.components.EmptyState
import com.meds.app.ui.components.MedsCard
import com.meds.app.ui.components.SectionLabel
import com.meds.app.ui.components.TextActionButton

@Composable
fun MedicationsScreen(
    onAddMedication: () -> Unit,
    onEditMedication: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Medicamentos",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Tus tratamientos y horarios en un solo lugar.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        EmptyState(
            icon = Icons.Outlined.Medication,
            title = "Aún no hay medicamentos",
            message = "Agrega el primero para definir su dosis y sus horarios.",
            actionLabel = "Agregar medicamento",
            onAction = onAddMedication
        )

        Spacer(Modifier.height(8.dp))

        MedsCard {
            SectionLabel(text = "Vista previa")
            Spacer(Modifier.height(10.dp))
            Text(
                text = "La ruta de edición también está conectada.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Abre el editor de ejemplo para comprobar la navegación inicial.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            TextActionButton(
                text = "Abrir editor de ejemplo",
                onClick = { onEditMedication("demo") }
            )
        }
    }
}
