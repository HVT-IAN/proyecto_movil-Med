package com.meds.app.presentation.patient.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.core.common.currentGreeting
import com.meds.app.core.common.formatDateKey
import com.meds.app.core.common.formatTime
import com.meds.app.core.common.relativeTime
import com.meds.app.core.common.todayKey
import com.meds.app.core.ui.components.DoseLogRow
import com.meds.app.core.ui.components.HeroCard
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PlantArt
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SecondaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.domain.model.DoseLog
import com.meds.app.presentation.patient.PatientUiState
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    name: String,
    state: PatientUiState,
    onAddMedication: () -> Unit,
    onTakeDose: (DoseLog) -> Unit,
    onSnoozeDose: (DoseLog) -> Unit,
    onViewAll: () -> Unit,
    onOpenCompanions: () -> Unit,
    onMarkNudgeRead: (String) -> Unit
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = System.currentTimeMillis()
        }
    }

    val dateText = remember(now / 60_000L) { formatDateKey(todayKey()) }
    val greeting = currentGreeting()
    val displayName = name.trim()

    val nextDose = state.nextPendingDose(now)
    val takenCount = state.takenCount
    val totalCount = state.totalCount
    val isLate = nextDose != null && nextDose.scheduledAt <= now
    val pendingColor = LocalMedsColors.current.pending
    val latestNudge = state.latestUnreadNudge

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = displayName.ifBlank { "Hola" },
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenCompanions) {
                Icon(
                    imageVector = Icons.Outlined.QrCode2,
                    contentDescription = "Acompañante",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (latestNudge != null) {
            Spacer(Modifier.height(16.dp))
            NudgeBanner(
                nudge = latestNudge,
                onDismiss = { onMarkNudgeRead(latestNudge.id) }
            )
        }

        Spacer(Modifier.height(24.dp))

        HeroCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    when {
                        nextDose != null -> {
                            SectionLabel(text = if (isLate) "Toma pendiente" else "Próxima toma")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = nextDose.medicationName.ifBlank { "Medicamento" },
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = buildString {
                                    if (nextDose.dose.isNotBlank()) {
                                        append(nextDose.dose)
                                        append(" · ")
                                    }
                                    append(formatTime(nextDose.scheduledAt))
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = relativeTime(nextDose.scheduledAt),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isLate) pendingColor else MaterialTheme.colorScheme.primary
                            )
                        }

                        state.loading -> {
                            SectionLabel(text = "Tu día")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Preparando tus tomas",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        state.medications.isEmpty() -> {
                            SectionLabel(text = "Tu día")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Aún no hay tomas",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Agrega tu primer medicamento para empezar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        totalCount > 0 -> {
                            SectionLabel(text = "Hoy")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Todo al día",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Tomaste $takenCount de $totalCount tomas.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        else -> {
                            SectionLabel(text = "Hoy")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Sin tomas para hoy",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Revisa tus horarios o agrega un medicamento.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                PlantArt(
                    stage = state.plant.level,
                    modifier = Modifier.size(88.dp)
                )
            }

            if (nextDose != null) {
                Spacer(Modifier.height(18.dp))
                PrimaryActionButton(
                    text = "Ya la tomé",
                    onClick = { onTakeDose(nextDose) },
                    icon = Icons.Outlined.Check
                )
                Spacer(Modifier.height(8.dp))
                SecondaryActionButton(
                    text = "Posponer 10 min",
                    onClick = { onSnoozeDose(nextDose) },
                    icon = Icons.Outlined.Schedule
                )
            } else if (!state.loading && state.medications.isEmpty()) {
                Spacer(Modifier.height(16.dp))
                PrimaryActionButton(
                    text = "Agregar medicamento",
                    onClick = onAddMedication,
                    icon = Icons.Outlined.Add
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hoy",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$takenCount de $totalCount tomas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(12.dp))

        if (state.todayDoses.isEmpty()) {
            MedsCard {
                if (state.loading) {
                    Text(
                        text = "Preparando tus tomas de hoy...",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Text(
                        text = "Todavía no hay tomas para hoy.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (state.medications.isEmpty()) {
                            "Cuando agregues un medicamento, verás aquí su horario y podrás confirmar cada toma."
                        } else {
                            "Los horarios de tu tratamiento aparecerán aquí."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            MedsCard {
                state.todayDoses.take(3).forEachIndexed { index, dose ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                    }
                    DoseLogRow(log = dose)
                }
                if (state.todayDoses.size > 3) {
                    TextActionButton(
                        text = "Ver todas",
                        onClick = onViewAll,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
