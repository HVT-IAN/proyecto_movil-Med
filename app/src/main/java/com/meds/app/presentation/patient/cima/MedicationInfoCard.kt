package com.meds.app.presentation.patient.cima

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.domain.model.MedicationInfo

@Composable
internal fun MedicationInfoCard(
    info: MedicationInfo,
    onDetail: () -> Unit,
    onUseName: (String) -> Unit
) {
    MedsCard {
        Text(
            text = info.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (info.laboratory.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = info.laboratory,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (info.activeSubstance.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Principio activo: ${info.activeSubstance}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val details = listOf(info.dose, info.form).filter { it.isNotBlank() }
        if (details.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = details.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (info.prescription.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = info.prescription,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill(
                text = if (info.commercialized) "Comercializado" else "No comercializado"
            )
            if (info.generic) {
                StatusPill(text = "Genérico")
            }
        }

        Spacer(Modifier.height(8.dp))
        TextActionButton(
            text = "Ver ficha y documentos",
            onClick = onDetail,
            modifier = Modifier.fillMaxWidth()
        )
        TextActionButton(
            text = "Usar este nombre",
            onClick = { onUseName(info.displayName) },
            modifier = Modifier.fillMaxWidth()
        )
        if (
            info.activeSubstance.isNotBlank() &&
            !info.activeSubstance.equals(info.displayName, ignoreCase = true)
        ) {
            TextActionButton(
                text = "Usar principio activo",
                onClick = { onUseName(info.activeSubstance) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
