package com.meds.app.presentation.patient.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.meds.app.core.common.currentGreeting
import com.meds.app.core.common.formatDateKey
import com.meds.app.core.common.formatTime
import com.meds.app.core.common.timeUntil
import com.meds.app.core.common.todayKey
import com.meds.app.core.ui.components.BreathingDots
import com.meds.app.core.ui.components.DoseLogRow
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.core.ui.theme.calma.CalmaMotion
import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.MedicationDraft
import com.meds.app.presentation.patient.PatientUiState
import com.meds.app.presentation.patient.focus.SnoozeSheet
import kotlinx.coroutines.delay

/**
 * Pantalla Hoy.
 *
 * Una sola acción principal para resolver la toma pendiente. El resto son
 * enlaces secundarios y contexto, sin elementos decorativos que compitan
 * con la tarea del momento.
 */
@Composable
fun HomeScreen(
    name: String,
    state: PatientUiState,
    hasCompanion: Boolean,
    companionsLoading: Boolean,
    onAddMedication: () -> Unit,
    onTakeDose: (DoseLog) -> Unit,
    onSnoozeDose: (DoseLog, Long) -> Unit,
    onOpenFocus: (DoseLog) -> Unit,
    onViewAll: () -> Unit,
    onOpenCompanions: () -> Unit,
    onMarkNudgeRead: (String) -> Unit,
    onQuickAddMedication: (MedicationDraft) -> Unit = {}
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            now = System.currentTimeMillis()
        }
    }

    var showAllDoses by rememberSaveable { mutableStateOf(false) }
    var showQuickAdd by rememberSaveable { mutableStateOf(false) }
    var snoozeDose by remember { mutableStateOf<DoseLog?>(null) }

    val dateText = remember(now / 60_000L) { formatDateKey(todayKey()) }
    val greeting = currentGreeting()
    val displayName = name.trim()
    val nextDose = state.nextPendingDose(now)
    val takenCount = state.takenCount
    val totalCount = state.totalCount
    val isLate = nextDose != null && nextDose.scheduledAt <= now
    val latestNudge = state.latestUnreadNudge

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = displayName.ifBlank { "Hola" },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (latestNudge != null) {
            Spacer(Modifier.height(18.dp))
            NudgeBanner(
                nudge = latestNudge,
                onDismiss = { onMarkNudgeRead(latestNudge.id) }
            )
        }

        Spacer(Modifier.height(24.dp))

        MedsCard {
            when {
                nextDose != null -> {
                    SectionLabel(
                        text = if (isLate) "Toma pendiente" else "Próxima toma"
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = nextDose.medicationName.ifBlank { "Medicamento" },
                        style = MaterialTheme.typography.headlineMedium,
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
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = timeUntil(nextDose.scheduledAt, now),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isLate) {
                            LocalMedsColors.current.pending
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryActionButton(
                        text = "Ya la tomé",
                        onClick = { onTakeDose(nextDose) },
                        icon = Icons.Outlined.Check
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextActionButton(
                            text = "Posponer",
                            onClick = { snoozeDose = nextDose },
                            modifier = Modifier.heightIn(min = 48.dp),
                            compact = true
                        )
                        Spacer(Modifier.width(8.dp))
                        TextActionButton(
                            text = "Solo esta toma",
                            onClick = { onOpenFocus(nextDose) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            compact = true
                        )
                    }
                }

                state.loading -> {
                    SectionLabel(text = "Hoy")
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Preparando tus tomas",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(12.dp))
                        BreathingDots(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                state.medications.isEmpty() -> {
                    SectionLabel(text = "Empieza aquí")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Aún no hay tomas",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Agrega tu primer medicamento para empezar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryActionButton(
                        text = "Agregar medicamento",
                        onClick = { showQuickAdd = true },
                        icon = Icons.Outlined.Add
                    )
                }

                totalCount > 0 -> {
                    SectionLabel(text = "Hoy")
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = LocalMedsColors.current.taken,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Todo al día",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tomaste $takenCount de $totalCount tomas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    SectionLabel(text = "Hoy")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Sin tomas para hoy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Revisa tus horarios o agrega un medicamento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    TextActionButton(
                        text = "Ver historial",
                        onClick = onViewAll,
                        modifier = Modifier.align(Alignment.End),
                        compact = true
                    )
                }
            }
        }

        if (totalCount > 0) {
            Spacer(Modifier.height(10.dp))
            TextActionButton(
                text = if (showAllDoses) {
                    "Ocultar tomas"
                } else {
                    "Ver todas las tomas ($totalCount)"
                },
                onClick = { showAllDoses = !showAllDoses },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(min = 48.dp),
                compact = true
            )
            AnimatedVisibility(
                visible = showAllDoses,
                enter = fadeIn(tween(CalmaMotion.NAV_FADE_MILLIS)) + expandVertically(),
                exit = fadeOut(tween(CalmaMotion.NAV_FADE_MILLIS)) + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hoy",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "$takenCount de $totalCount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { takenCount.toFloat() / totalCount.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    MedsCard {
                        state.todayDoses.forEachIndexed { index, dose ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 1.dp
                                )
                            }
                            DoseLogRow(log = dose)
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                        TextActionButton(
                            text = "Ver historial",
                            onClick = onViewAll,
                            modifier = Modifier.align(Alignment.End),
                            compact = true
                        )
                    }
                }
            }
        }

        if (state.medications.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            TextActionButton(
                text = "Agregar rápido",
                onClick = { showQuickAdd = true },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(min = 48.dp),
                compact = true
            )
        }

        if (!hasCompanion && !companionsLoading) {
            Spacer(Modifier.height(16.dp))
            MedsCard(
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = onOpenCompanions
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.QrCode2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Invita a un acompañante",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Puede ver tu progreso y enviarte recordatorios.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    SnoozeSheet(
        dose = snoozeDose,
        onDismiss = { snoozeDose = null },
        onSelect = { minutes ->
            snoozeDose?.let { onSnoozeDose(it, minutes) }
            snoozeDose = null
        }
    )

    if (showQuickAdd) {
        QuickAddMedicationSheet(
            onDismiss = { showQuickAdd = false },
            onSave = { medName, medDose, hora ->
                onQuickAddMedication(
                    MedicationDraft(
                        id = null,
                        scheduleId = null,
                        name = medName,
                        dose = medDose,
                        times = listOf(hora),
                        stock = 0,
                        anchor = "Sin rutina"
                    )
                )
                showQuickAdd = false
            },
            onMoreOptions = {
                showQuickAdd = false
                onAddMedication()
            }
        )
    }
}
