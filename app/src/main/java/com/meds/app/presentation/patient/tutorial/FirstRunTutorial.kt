@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.meds.app.presentation.patient.tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.TextActionButton

/**
 * Guía breve de primera vez.
 *
 * Una sola hoja, sin carrusel. Explica los cuatro destinos fijos y dónde vive
 * el historial. Se puede cerrar deslizando, con "Saltar" o con
 * "Empezar"; en Ajustes hay un acceso para volver a verla.
 */
@Composable
fun FirstRunTutorial(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Así funciona MEDs",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Cuatro lugares y todo lo demás a la vista.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextActionButton(
                    text = "Saltar",
                    onClick = onDismiss
                )
            }

            Spacer(Modifier.height(24.dp))

            TutorialRow(
                icon = Icons.Outlined.Home,
                title = "Hoy",
                message = "Tu próxima toma y un solo botón para confirmarla."
            )
            Spacer(Modifier.height(18.dp))
            TutorialRow(
                icon = Icons.Outlined.Medication,
                title = "Medicamentos",
                message = "Dosis, horarios y edición de tu tratamiento."
            )
            Spacer(Modifier.height(18.dp))
            TutorialRow(
                icon = Icons.Outlined.Eco,
                title = "Jardín",
                message = "Tu constancia, hoja a hoja."
            )
            Spacer(Modifier.height(18.dp))
            TutorialRow(
                icon = Icons.Outlined.Settings,
                title = "Ajustes",
                message = "Cuenta, seguridad y acompañante."
            )

            Spacer(Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "El historial está dentro de Hoy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            PrimaryActionButton(
                text = "Empezar",
                onClick = onDismiss
            )
        }
    }
}

@Composable
private fun TutorialRow(
    icon: ImageVector,
    title: String,
    message: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
