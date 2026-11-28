package com.meds.app.presentation.settings

import android.app.TimePickerDialog
import android.content.Context
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SecondaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.di.MedsViewModelFactory
import com.meds.app.domain.model.Medication
import com.meds.app.integration.notifications.NotificationHelper
import java.util.Locale

@Composable
fun ReminderSettingsScreen(
    medications: List<Medication>,
    onBack: () -> Unit,
    viewModel: ReminderSettingsViewModel = viewModel(factory = MedsViewModelFactory)
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var notificationsEnabled by remember {
        mutableStateOf(
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        )
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsEnabled =
            NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val activeMedications = remember(medications) {
        medications.filter { it.active }.sortedBy { it.name.lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Recordatorios",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(12.dp))

        if (!notificationsEnabled) {
            MedsCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Las notificaciones están bloqueadas",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "MEDS no puede mostrarte recordatorios hasta que las " +
                        "actives en los ajustes del sistema.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                PrimaryActionButton(
                    text = "Abrir ajustes de notificaciones",
                    onClick = {
                        context.startActivity(
                            NotificationHelper.notificationSettingsIntent(context)
                        )
                    }
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        SectionLabel(text = "Recordatorios")
        Spacer(Modifier.height(10.dp))
        MedsCard {
            SwitchRow(
                title = "Recordatorios de medicación",
                subtitle = if (settings.masterEnabled) {
                    "Activos. Puedes apagarlos por medicamento."
                } else {
                    "Apagados. No se mostrará ningún aviso."
                },
                checked = settings.masterEnabled,
                onCheckedChange = viewModel::setMasterEnabled
            )
        }

        Spacer(Modifier.height(24.dp))
        SectionLabel(text = "Por medicamento")
        Spacer(Modifier.height(10.dp))
        MedsCard {
            if (activeMedications.isEmpty()) {
                Text(
                    text = "Agrega un medicamento para controlar sus avisos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                activeMedications.forEachIndexed { index, medication ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                    }
                    SwitchRow(
                        title = medication.name.ifBlank { "Medicamento" },
                        subtitle = medication.dose.ifBlank { "Sin dosis indicada" },
                        checked = settings.isMedicationEnabled(medication.id),
                        enabled = settings.masterEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setMedicationEnabled(medication.id, enabled)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionLabel(text = "Horas de silencio")
        Spacer(Modifier.height(10.dp))
        MedsCard {
            SwitchRow(
                title = "Silenciar avisos",
                subtitle = "Sin sonido ni aviso emergente dentro del rango.",
                checked = settings.quietEnabled,
                onCheckedChange = viewModel::setQuietEnabled
            )
            if (settings.quietEnabled) {
                Spacer(Modifier.height(14.dp))
                TimeRow(
                    label = "Desde",
                    minutes = settings.quietStartMinutes,
                    onPicked = { value ->
                        viewModel.setQuietHours(value, settings.quietEndMinutes)
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp
                )
                TimeRow(
                    label = "Hasta",
                    minutes = settings.quietEndMinutes,
                    onPicked = { value ->
                        viewModel.setQuietHours(settings.quietStartMinutes, value)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionLabel(text = "Sonido y prueba")
        Spacer(Modifier.height(10.dp))
        MedsCard {
            SwitchRow(
                title = "Sonido",
                subtitle = "Si está apagado, el aviso aparece en silencio.",
                checked = settings.soundEnabled,
                onCheckedChange = viewModel::setSoundEnabled
            )
            Spacer(Modifier.height(14.dp))
            SecondaryActionButton(
                text = "Probar aviso",
                onClick = {
                    NotificationHelper.showTestReminder(
                        context = context,
                        soundEnabled = settings.soundEnabled
                    )
                },
                enabled = notificationsEnabled
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun TimeRow(
    label: String,
    minutes: Int,
    onPicked: (Int) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { showTimePicker(context, minutes, onPicked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = formatMinutes(minutes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun showTimePicker(
    context: Context,
    initialMinutes: Int,
    onPicked: (Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(hour * 60 + minute) },
        initialMinutes / 60,
        initialMinutes % 60,
        true
    ).show()
}

private fun formatMinutes(minutes: Int): String {
    val normalized = ((minutes % 1440) + 1440) % 1440
    return String.format(Locale.US, "%02d:%02d", normalized / 60, normalized % 60)
}
