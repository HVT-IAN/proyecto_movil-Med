package com.meds.app.presentation.companion

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.components.DoseLogRow
import com.meds.app.core.ui.components.HeroCard
import com.meds.app.core.ui.components.InfoRow
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.MedsWordmark
import com.meds.app.core.ui.components.PlantArt
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.domain.model.DoseStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CompanionPanelContent(
    name: String,
    state: CompanionUiState,
    modifier: Modifier = Modifier,
    onNudge: () -> Unit,
    onUnlink: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val patient = state.selectedPatient
    val day = state.day
    val locale = Locale.forLanguageTag("es")
    val dateText = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale))
        .replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase(locale) else character.toString()
        }
    val colors = LocalMedsColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedsWordmark()
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Ajustes",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Acompañas a",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = patient?.name?.ifBlank { day?.patientName.orEmpty() }
                        ?.ifBlank { "tu paciente" }
                        ?: "tu paciente",
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
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(colors.companionContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (patient?.name ?: day?.patientName ?: "A")
                        .trim()
                        .take(1)
                        .uppercase()
                        .ifBlank { "A" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.companion
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        when {
            day == null && state.loading -> {
                MedsCard {
                    Text(
                        text = "Cargando el día...",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            day == null -> {
                MedsCard {
                    Text(
                        text = "No pudimos acceder a los datos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Es posible que la persona haya retirado tu acceso o que la conexión no esté disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryActionButton(
                        text = "Desvincular",
                        onClick = onUnlink
                    )
                }
            }

            else -> {
                val taken = day.doses.count { it.status == DoseStatus.TAKEN }
                val total = day.doses.size
                val progress = if (total > 0) taken.toFloat() / total.toFloat() else 0f
                val allDone = total > 0 && taken == total

                HeroCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SectionLabel(text = "Hoy")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$taken de $total tomas",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = when {
                                    total == 0 -> "Todavía no hay tomas registradas."
                                    allDone -> "Todo al día. No hace falta insistir."
                                    else -> "Puedes enviar un recordatorio si hace falta."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        PlantArt(
                            stage = day.plant.level,
                            modifier = Modifier.size(84.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tomas de hoy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$taken de $total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))

                if (day.doses.isEmpty()) {
                    MedsCard {
                        Text(
                            text = "Aún no hay tomas para hoy.",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Cuando el paciente registre sus medicamentos, aparecerán aquí en tiempo real.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    MedsCard {
                        day.doses.forEachIndexed { index, dose ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 1.dp
                                )
                            }
                            DoseLogRow(log = dose)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                MedsCard {
                    SectionLabel(text = "Apoyo")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Enviar un recordatorio",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Un mensaje breve y amable puede ayudar a retomar el ritmo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryActionButton(
                        text = "Enviar recordatorio",
                        onClick = onNudge,
                        enabled = !state.nudgeSending,
                        icon = Icons.Outlined.Notifications
                    )
                }

                Spacer(Modifier.height(12.dp))

                TextActionButton(
                    text = "Desvincular a ${patient?.name?.ifBlank { "este paciente" } ?: "este paciente"}",
                    onClick = onUnlink,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                MedsCard {
                    InfoRow(
                        icon = Icons.Outlined.CheckCircle,
                        text = "Los cambios del paciente se actualizan en tiempo real mientras la app está abierta."
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Sesión de ${name.trim().ifBlank { "acompañante" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
