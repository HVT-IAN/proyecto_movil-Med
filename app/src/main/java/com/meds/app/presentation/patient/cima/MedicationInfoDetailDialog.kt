package com.meds.app.presentation.patient.cima

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.domain.model.MedicationInfoDetail

@Composable
internal fun MedicationInfoDetailDialog(
    loading: Boolean,
    detail: MedicationInfoDetail?,
    error: String?,
    onDismiss: () -> Unit
) {
    if (!loading && detail == null && error == null) return

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ficha del medicamento",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                when {
                    loading -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Cargando ficha...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    error != null -> {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No se pudo cargar la ficha.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            TextActionButton(
                                text = "Cerrar",
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    detail != null -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = detail.info.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (detail.info.laboratory.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = detail.info.laboratory,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            SectionLabel(text = "Principio activo")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = detail.info.activeSubstance.ifBlank { "No indicado" },
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(12.dp))
                            SectionLabel(text = "Dosis y forma")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = listOf(detail.info.dose, detail.info.form)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · ")
                                    .ifBlank { "No indicado" },
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(12.dp))
                            SectionLabel(text = "Prescripción")
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = detail.info.prescription.ifBlank { "No indicado" },
                                style = MaterialTheme.typography.bodyMedium
                            )

                            DetailList(
                                title = "Principios activos",
                                values = detail.activeIngredients
                            )
                            DetailList(
                                title = "Código ATC",
                                values = detail.atc
                            )
                            DetailList(
                                title = "Vías de administración",
                                values = detail.administrationRoutes
                            )
                            DetailList(
                                title = "Presentaciones",
                                values = detail.presentations,
                                limit = 8
                            )
                            DetailList(
                                title = "Excipientes",
                                values = detail.excipients,
                                limit = 10
                            )

                            if (detail.info.docs.isNotEmpty()) {
                                Spacer(Modifier.height(16.dp))
                                SectionLabel(text = "Documentos oficiales")
                                Spacer(Modifier.height(4.dp))
                                detail.info.docs.forEach { doc ->
                                    TextActionButton(
                                        text = doc.label,
                                        onClick = { openDocument(context, doc.url) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Información de referencia de la AEMPS. No sustituye el consejo de un profesional sanitario.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailList(
    title: String,
    values: List<String>,
    limit: Int = 12
) {
    if (values.isEmpty()) return

    Spacer(Modifier.height(12.dp))
    SectionLabel(text = title)
    Spacer(Modifier.height(4.dp))
    values.take(limit).forEach { value ->
        Text(
            text = "• $value",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    if (values.size > limit) {
        Text(
            text = "y ${values.size - limit} más...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun openDocument(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(
            context,
            "No se pudo abrir el documento.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
