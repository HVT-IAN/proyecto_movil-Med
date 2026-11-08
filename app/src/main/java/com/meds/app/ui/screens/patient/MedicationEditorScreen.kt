package com.meds.app.ui.screens.patient

import android.app.TimePickerDialog
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meds.app.data.model.Medication
import com.meds.app.data.model.MedicationDraft
import com.meds.app.data.model.Schedule
import com.meds.app.ui.components.MedsCard
import com.meds.app.ui.components.PrimaryActionButton
import com.meds.app.ui.components.SectionLabel
import com.meds.app.ui.components.TextActionButton
import com.meds.app.util.formatTimeOfDay
import java.time.LocalTime
import java.util.Locale

private val formOptions = listOf("Tableta", "Cápsula", "Jarabe", "Inyección", "Otra")
private val routineOptions = listOf("Sin rutina", "Desayuno", "Comida", "Cena", "Al acostarme")

@Composable
fun MedicationEditorScreen(
    viewModel: PatientViewModel,
    medication: Medication?,
    schedule: Schedule?,
    onDone: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf(medication?.name.orEmpty()) }
    var dose by rememberSaveable { mutableStateOf(medication?.dose.orEmpty()) }
    var form by rememberSaveable { mutableStateOf(medication?.form ?: "Tableta") }
    var anchor by rememberSaveable { mutableStateOf(schedule?.anchor ?: "Sin rutina") }
    var stockText by rememberSaveable {
        mutableStateOf(medication?.stock?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var times by remember { mutableStateOf(schedule?.times?.sorted() ?: emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.consumeSaveSuccess()
            onDone()
        }
    }

    val openTimePicker: () -> Unit = {
        val now = LocalTime.now()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val formatted = String.format(Locale.US, "%02d:%02d", hour, minute)
                if (!times.contains(formatted)) {
                    times = (times + formatted).sorted()
                }
            },
            now.hour,
            now.minute,
            true
        ).show()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDone) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (medication == null) "Nuevo medicamento" else "Editar medicamento",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Un horario claro ayuda a sostener el hábito.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            MedsCard {
                SectionLabel(text = "Información")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { value -> if (value.length <= 60) name = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej. Metilfenidato") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = dose,
                    onValueChange = { value -> if (value.length <= 30) dose = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Dosis") },
                    placeholder = { Text("Ej. 18 mg") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
            }

            Spacer(Modifier.height(14.dp))

            MedsCard {
                SectionLabel(text = "Forma")
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    formOptions.forEach { option ->
                        FilterChip(
                            selected = form == option,
                            onClick = { form = option },
                            label = { Text(option) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            MedsCard {
                SectionLabel(text = "Horario")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Añade una o varias horas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))

                if (times.isEmpty()) {
                    Text(
                        text = "Todavía no hay horas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        times.forEach { time ->
                            AssistChip(
                                onClick = { times = times - time },
                                label = { Text(formatTimeOfDay(time)) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Quitar hora",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = openTimePicker,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Agregar hora")
                }
            }

            Spacer(Modifier.height(14.dp))

            MedsCard {
                SectionLabel(text = "Rutina")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "¿Qué sueles hacer a esa hora?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    routineOptions.forEach { option ->
                        FilterChip(
                            selected = anchor == option,
                            onClick = { anchor = option },
                            label = { Text(option) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            MedsCard {
                SectionLabel(text = "Inventario")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = stockText,
                    onValueChange = { value ->
                        stockText = value.filter(Char::isDigit).take(5)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Unidades disponibles (opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Puedes dejarlo vacío si no quieres controlar el inventario.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))

            PrimaryActionButton(
                text = if (state.saving) "Guardando..." else if (medication == null) {
                    "Guardar medicamento"
                } else {
                    "Guardar cambios"
                },
                onClick = {
                    viewModel.saveMedication(
                        MedicationDraft(
                            id = medication?.id,
                            scheduleId = schedule?.id,
                            name = name,
                            dose = dose,
                            form = form,
                            times = times,
                            stock = stockText.toIntOrNull() ?: 0,
                            anchor = anchor
                        )
                    )
                },
                enabled = name.isNotBlank() &&
                    dose.isNotBlank() &&
                    times.isNotEmpty() &&
                    !state.saving,
                icon = Icons.Outlined.Check
            )

            if (medication != null) {
                Spacer(Modifier.height(10.dp))
                TextActionButton(
                    text = "Eliminar medicamento",
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog && medication != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar este medicamento?") },
            text = {
                Text(
                    "Se quitarán sus recordatorios y las tomas pendientes de hoy. El historial de tomas se conserva."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteMedication(medication.id, schedule?.id)
                        onDone()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
