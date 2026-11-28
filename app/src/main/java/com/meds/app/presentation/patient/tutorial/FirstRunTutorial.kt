@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.meds.app.presentation.patient.tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.meds.app.R
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.TextActionButton

/**
 * Guía breve sobre cómo funciona MEDs.
 *
 * No se muestra de forma automática al arrancar: se abre únicamente después
 * de guardar el primer medicamento o bajo demanda desde Ajustes →
 * "Cómo funciona MEDs".
 */
@Composable
fun FirstRunTutorial(
    onDismiss: () -> Unit,
    isFirstMedication: Boolean = false
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
                        text = "Cómo funciona MEDs",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (isFirstMedication) {
                            "Tu primer medicamento ya está listo. Así se organiza tu espacio."
                        } else {
                            "Usa la barra inferior para moverte entre las cuatro secciones."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextActionButton(
                    text = "Cerrar",
                    onClick = onDismiss,
                    modifier = Modifier.heightIn(min = 48.dp)
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
                icon = ImageVector.vectorResource(R.drawable.ic_pill),
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
                text = if (isFirstMedication) "Ir a mis tomas" else "Entendido",
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
