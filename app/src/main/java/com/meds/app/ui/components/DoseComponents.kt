package com.meds.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.data.model.DoseLog
import com.meds.app.data.model.DoseStatus
import com.meds.app.ui.theme.LocalMedsColors
import com.meds.app.util.formatTime

@Composable
fun DoseStatusChip(status: String) {
    val colors = LocalMedsColors.current
    val label: String
    val container: androidx.compose.ui.graphics.Color
    val content: androidx.compose.ui.graphics.Color

    when (status) {
        DoseStatus.TAKEN -> {
            label = "Tomada"
            container = colors.takenContainer
            content = colors.onTakenContainer
        }
        DoseStatus.SNOOZED -> {
            label = "Pospuesta"
            container = colors.pendingContainer
            content = colors.onPendingContainer
        }
        DoseStatus.SKIPPED -> {
            label = "Omitida"
            container = MaterialTheme.colorScheme.surfaceVariant
            content = MaterialTheme.colorScheme.onSurfaceVariant
        }
        else -> {
            label = "Pendiente"
            container = colors.pendingContainer
            content = colors.onPendingContainer
        }
    }

    Surface(
        shape = CircleShape,
        color = container
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = content
        )
    }
}

@Composable
fun DoseLogRow(
    log: DoseLog,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTime(log.scheduledAt),
            modifier = Modifier.width(52.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.medicationName.ifBlank { "Medicamento" },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (log.dose.isNotBlank()) {
                Text(
                    text = log.dose,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DoseStatusChip(status = log.status)
    }
}
