package com.meds.app.ui.screens.companion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.ui.components.EmptyState
import com.meds.app.ui.components.LabelValue
import com.meds.app.ui.components.MedsCard
import com.meds.app.ui.components.SecondaryActionButton
import com.meds.app.ui.components.SectionLabel
import com.meds.app.ui.screens.shared.BackHeader

@Composable
fun CompanionPanelScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        BackHeader(title = "Panel del acompañante", onBack = onBack)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "El día de tu persona de confianza.",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Solo verás la información del día actual.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        MedsCard {
            SectionLabel(text = "Resumen de hoy")
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                LabelValue(
                    label = "Tomas",
                    value = "0",
                    modifier = Modifier.weight(1f)
                )
                LabelValue(
                    label = "Confirmadas",
                    value = "0",
                    modifier = Modifier.weight(1f)
                )
                LabelValue(
                    label = "Pendientes",
                    value = "0",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        EmptyState(
            icon = Icons.Outlined.CheckCircle,
            title = "Sin información todavía",
            message = "Cuando el paciente vincule su cuenta y registre sus tomas, aparecerán aquí."
        )

        Spacer(Modifier.height(8.dp))

        MedsCard {
            SectionLabel(text = "Acciones")
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Enviar un recordatorio",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "El aviso al paciente y la actualización en tiempo real se conectarán en develop.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            SecondaryActionButton(
                text = "Volver a la vinculación",
                onClick = onBack
            )
        }
    }
}
