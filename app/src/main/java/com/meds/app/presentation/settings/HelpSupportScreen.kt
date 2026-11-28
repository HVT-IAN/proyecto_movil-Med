package com.meds.app.presentation.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.ReadableContent
import com.meds.app.core.ui.components.SectionLabel

private const val SUPPORT_EMAIL = "soporte@meds.app"

private data class FaqItem(
    val question: String,
    val answer: String
)

private val faqs = listOf(
    FaqItem(
        question = "¿Cómo agrego un medicamento?",
        answer = "Desde Hoy, toca “Agregar rápido” o “Agregar medicamento”. " +
            "Escribe el nombre, elige la hora y guarda."
    ),
    FaqItem(
        question = "¿Qué hago si se me pasa una toma?",
        answer = "Hoy la mostrará como pendiente. Puedes tomarla o posponerla unos minutos."
    ),
    FaqItem(
        question = "¿Cómo funcionan los recordatorios?",
        answer = "Se activan cuando das permiso. En Ajustes → Recordatorios puedes apagarlos, " +
            "elegir horas de silencio y probar el aviso."
    ),
    FaqItem(
        question = "¿Cómo invito a un acompañante?",
        answer = "En Hoy o en Ajustes → Acompañante. Comparte el código de invitación " +
            "y la otra persona lo introduce en su app."
    ),
    FaqItem(
        question = "¿Cómo cambio el tema o el contraste?",
        answer = "En Ajustes → Apariencia elige Sistema, Claro u Oscuro. " +
            "En Accesibilidad puedes activar alto contraste y reducir movimiento."
    ),
    FaqItem(
        question = "¿Dónde están mis datos?",
        answer = "Se guardan en tu cuenta de MEDS y en una copia local del teléfono."
    )
)

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var expandedIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    val sendSupport: () -> Unit = {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$SUPPORT_EMAIL")
            putExtra(Intent.EXTRA_SUBJECT, "Ayuda con MEDs")
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(
                    context,
                    "No encontramos una app de correo instalada.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    ReadableContent {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = "Ayuda y soporte",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Respuestas rápidas y un canal directo para cuando algo no cuadra.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            SectionLabel(text = "Preguntas frecuentes")
            Spacer(Modifier.height(10.dp))

            faqs.forEachIndexed { index, faq ->
                val expanded = expandedIndex == index
                MedsCard(
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClick = {
                            expandedIndex = if (expanded) null else index
                        }
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = faq.question,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = if (expanded) {
                                "Ocultar respuesta"
                            } else {
                                "Mostrar respuesta"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = if (expanded) 180f else 0f
                            }
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = faq.answer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel(text = "Contacto")
            Spacer(Modifier.height(10.dp))

            MedsCard {
                Text(
                    text = "¿No encuentras la respuesta?",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Escríbenos y te respondemos lo antes posible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                PrimaryActionButton(
                    text = "Escribir a soporte",
                    onClick = sendSupport,
                    icon = Icons.Outlined.Email
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = SUPPORT_EMAIL,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
