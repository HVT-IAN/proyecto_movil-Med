package com.meds.app.ui.screens.patient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.ui.components.LabelValue
import com.meds.app.ui.components.MedsCard
import com.meds.app.ui.components.SectionLabel
import com.meds.app.ui.screens.shared.BackHeader

@Composable
fun MedicationEditorScreen(
    medicationId: String?,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        BackHeader(
            title = if (medicationId == null) "Nuevo medicamento" else "Editar medicamento",
            onBack = onBack
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "El formulario completo se implementará en develop.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        MedsCard {
            SectionLabel(text = "Datos del medicamento")
            Spacer(Modifier.height(16.dp))
            LabelValue(label = "Nombre", value = "Pendiente")
            Spacer(Modifier.height(14.dp))
            LabelValue(label = "Dosis", value = "Pendiente")
            Spacer(Modifier.height(14.dp))
            LabelValue(label = "Forma", value = "Pendiente")
            Spacer(Modifier.height(14.dp))
            LabelValue(label = "Stock", value = "Pendiente")
        }

        Spacer(Modifier.height(16.dp))

        MedsCard {
            SectionLabel(text = "Horarios")
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Sin horarios definidos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Aquí se agregarán una o varias horas para generar las tomas del día.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = if (medicationId == null) {
                "Ruta activa: medication_editor/new"
            } else {
                "Ruta activa: medication_editor/{medicationId}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
