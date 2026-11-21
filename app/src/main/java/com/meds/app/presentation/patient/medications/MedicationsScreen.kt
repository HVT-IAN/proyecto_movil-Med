package com.meds.app.presentation.patient.medications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.meds.app.core.common.formatTimeOfDay
import com.meds.app.core.ui.components.BreathingDots
import com.meds.app.core.ui.components.EmptyState
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.SecondaryActionButton
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.domain.model.Medication
import com.meds.app.domain.model.Schedule
import com.meds.app.presentation.patient.PatientUiState

/**
 * Lista de medicamentos.
 *
 * Cada tarjeta resume lo necesario para reconocer el tratamiento de un
 * vistazo: nombre, dosis, horas y rutina. La edición se abre tocando la
 * tarjeta, sin iconos adicionales que compitan con el contenido.
 */
@Composable
fun MedicationsScreen(
    state: PatientUiState,
    onAddMedication: () -> Unit,
    onEditMedication: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Medicamentos",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Tu tratamiento, en un solo lugar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (state.medications.isNotEmpty()) {
                SecondaryActionButton(
                    text = "Agregar",
                    onClick = onAddMedication,
                    icon = Icons.Outlined.Add,
                    fillWidth = false,
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        when {
            state.medications.isEmpty() && state.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BreathingDots(
                        color = MaterialTheme.colorScheme.primary,
                        dotSize = 6.dp,
                        spacing = 6.dp
                    )
                }
            }

            state.medications.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.Medication,
                    title = "Aún no hay medicamentos",
                    message = "Agrega el primero para tener un horario claro y recibir recordatorios.",
                    actionLabel = "Agregar medicamento",
                    onAction = onAddMedication
                )
            }

            else -> {
                if (state.medications.size > 1) {
                    Text(
                        text = "${state.medications.size} medicamentos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }
                state.medications.forEach { medication ->
                    val schedule = state.schedules.firstOrNull {
                        it.medicationId == medication.id
                    }
                    MedicationCard(
                        medication = medication,
                        schedule = schedule,
                        onEdit = { onEditMedication(medication.id) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    schedule: Schedule?,
    onEdit: () -> Unit
) {
    val colors = LocalMedsColors.current
    val times = schedule?.times.orEmpty()
    val hasAnchor = !schedule?.anchor.isNullOrBlank() && schedule.anchor != "Sin rutina"
    val lowStock = medication.stock in 1..5

    MedsCard(
        modifier = Modifier.clickable(role = Role.Button, onClick = onEdit)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val detail = buildString {
                    if (medication.dose.isNotBlank()) append(medication.dose)
                    if (medication.form.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(medication.form)
                    }
                }
                if (detail.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        if (times.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = times.map(::formatTimeOfDay).joinToString(" · "),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (hasAnchor) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = schedule.anchor,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (medication.stock > 0) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (medication.stock == 1) {
                    "Queda 1 unidad"
                } else {
                    "Quedan ${medication.stock} unidades"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (lowStock) colors.pending else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
