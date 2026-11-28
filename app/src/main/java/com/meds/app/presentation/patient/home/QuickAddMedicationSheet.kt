package com.meds.app.presentation.patient.home

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.meds.app.core.common.formatTimeOfDay
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton
import java.time.LocalTime
import java.util.Locale

enum class QuickTimeOption(val label: String) {
    NOW("Ahora"),
    MORNING("Mañana"),
    NOON("Mediodía"),
    NIGHT("Noche"),
    OTHER("Otra")
}

/**
 * Resuelve la hora en formato `HH:mm` para el chip seleccionado en el alta rápida.
 */
fun resolveQuickTime(
    option: QuickTimeOption,
    now: LocalTime = LocalTime.now(),
    customTime: String? = null
): String? = when (option) {
    QuickTimeOption.NOW -> String.format(Locale.US, "%02d:%02d", now.hour, now.minute)
    QuickTimeOption.MORNING -> "08:00"
    QuickTimeOption.NOON -> "14:00"
    QuickTimeOption.NIGHT -> "21:00"
    QuickTimeOption.OTHER -> customTime?.takeIf { it.isNotBlank() }?.let(::formatTimeOfDay)
}

/**
 * Hoja de alta rápida de medicamento desde Hoy.
 *
 * Permite registrar un medicamento en 3–4 toques más el nombre, o saltar
 * al editor completo mediante "Más opciones".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddMedicationSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, dose: String, time: String) -> Unit,
    onMoreOptions: () -> Unit,
    visible: Boolean = true,
    saving: Boolean = false,
    error: String? = null
) {
    if (!visible) return

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by rememberSaveable { mutableStateOf("") }
    var dose by rememberSaveable { mutableStateOf("") }
    var selectedOption by rememberSaveable { mutableStateOf(QuickTimeOption.NOW) }
    var customTime by rememberSaveable { mutableStateOf<String?>(null) }
    var nowSnapshot by remember { mutableStateOf(LocalTime.now()) }

    val resolvedTime = resolveQuickTime(
        option = selectedOption,
        now = nowSnapshot,
        customTime = customTime
    )
    val canSave = name.isNotBlank() && resolvedTime != null

    val openCustomTimePicker: () -> Unit = {
        val current = LocalTime.now()
        val initialParts = customTime?.split(":")
        val initialHour = initialParts?.getOrNull(0)?.toIntOrNull() ?: current.hour
        val initialMinute = initialParts?.getOrNull(1)?.toIntOrNull() ?: current.minute
        TimePickerDialog(
            context,
            { _, hour, minute ->
                customTime = String.format(Locale.US, "%02d:%02d", hour, minute)
                selectedOption = QuickTimeOption.OTHER
            },
            initialHour,
            initialMinute,
            true
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            SectionLabel(text = "Alta rápida")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Agregar medicamento",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))

            MedsCard {
                SectionLabel(text = "Medicamento")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { value -> if (value.length <= 60) name = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej. Paracetamol") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(Modifier.height(16.dp))
                SectionLabel(text = "Hora")
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTimeOption.entries.forEach { option ->
                        val isSelected = selectedOption == option
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (option == QuickTimeOption.OTHER) {
                                    openCustomTimePicker()
                                } else {
                                    if (option == QuickTimeOption.NOW) {
                                        nowSnapshot = LocalTime.now()
                                    }
                                    selectedOption = option
                                }
                            },
                            label = { Text(option.label) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                if (resolvedTime != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Hora programada: $resolvedTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = dose,
                    onValueChange = { value -> if (value.length <= 30) dose = value },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Dosis (opcional)") },
                    placeholder = { Text("Ej. 500 mg") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }

            Spacer(Modifier.height(18.dp))
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(12.dp))
            }
            PrimaryActionButton(
                text = if (saving) "Guardando..." else "Agregar",
                onClick = {
                    val timeToSave = resolveQuickTime(
                        option = selectedOption,
                        now = LocalTime.now(),
                        customTime = customTime
                    ) ?: return@PrimaryActionButton
                    onSave(name.trim(), dose.trim(), timeToSave)
                },
                enabled = canSave && !saving,
                icon = Icons.Outlined.Check
            )
            Spacer(Modifier.height(6.dp))
            TextActionButton(
                text = "Más opciones",
                onClick = {
                    onDismiss()
                    onMoreOptions()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(min = 48.dp)
            )
        }
    }
}
