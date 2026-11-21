package com.meds.app.presentation.patient.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SecondaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.domain.model.DoseLog

/**
 * Hoja para posponer una toma con duración ajustable.
 *
 * Se usa tanto en Hoy como en el modo foco. "Otro" se resuelve dentro de la
 * misma hoja para no apilar un diálogo encima de un ModalBottomSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeSheet(
    dose: DoseLog?,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    if (dose == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customVisible by remember(dose.id) { mutableStateOf(false) }
    var customText by remember(dose.id) { mutableStateOf("") }
    val customMinutes = customText.toLongOrNull()
    val customValid = customMinutes != null && customMinutes in 1L..120L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            SectionLabel(text = "Posponer toma")
            Spacer(Modifier.height(8.dp))
            Text(
                text = dose.medicationName.ifBlank { "Medicamento" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "¿Cuánto tiempo?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))

            if (!customVisible) {
                listOf(5L, 10L, 15L, 30L, 60L).forEach { minutes ->
                    SecondaryActionButton(
                        text = "$minutes minutos",
                        onClick = { onSelect(minutes) },
                        fillWidth = true
                    )
                    Spacer(Modifier.height(8.dp))
                }
                SecondaryActionButton(
                    text = "Otro…",
                    onClick = { customVisible = true },
                    fillWidth = true
                )
            } else {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { value ->
                        customText = value.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("Minutos (1–120)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = customText.isNotEmpty() && !customValid,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                PrimaryActionButton(
                    text = "Posponer",
                    onClick = { customMinutes?.let(onSelect) },
                    enabled = customValid
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            customVisible = false
                            customText = ""
                        }
                    ) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}
