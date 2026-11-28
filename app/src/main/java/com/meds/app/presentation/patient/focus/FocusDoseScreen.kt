package com.meds.app.presentation.patient.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meds.app.R
import com.meds.app.core.common.formatTime
import com.meds.app.core.common.timeUntil
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.domain.model.DoseLog
import kotlinx.coroutines.delay

/**
 * Modo foco: una sola toma, una sola acción principal.
 *
 * No es una ruta de navegación. Se renderiza como overlay en `PatientApp`
 * para cubrir ambos estilos de barra inferior sin tocar la navegación.
 */
@Composable
fun FocusDoseScreen(
    dose: DoseLog,
    onTake: () -> Unit,
    onSnooze: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showSnooze by remember { mutableStateOf(false) }
    val colors = LocalMedsColors.current

    LaunchedEffect(dose.id) {
        while (true) {
            delay(60_000L)
            now = System.currentTimeMillis()
        }
    }

    val isLate = dose.scheduledAt <= now

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cerrar"
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(colors.heroEnd),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_pill),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(20.dp))
            SectionLabel(text = "Solo esta toma")
            Spacer(Modifier.height(10.dp))
            Text(
                text = dose.medicationName.ifBlank { "Medicamento" },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildString {
                    if (dose.dose.isNotBlank()) {
                        append(dose.dose)
                        append(" · ")
                    }
                    append(formatTime(dose.scheduledAt))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = timeUntil(dose.scheduledAt, now),
                style = MaterialTheme.typography.labelMedium,
                color = if (isLate) colors.pending else MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryActionButton(
                text = "Ya la tomé",
                onClick = onTake,
                icon = Icons.Outlined.Check
            )
            Spacer(Modifier.height(6.dp))
            TextActionButton(
                text = "Posponer",
                onClick = { showSnooze = true },
                modifier = Modifier.heightIn(min = 48.dp)
            )
        }
    }

    if (showSnooze) {
        SnoozeSheet(
            dose = dose,
            onDismiss = { showSnooze = false },
            onSelect = { minutes ->
                showSnooze = false
                onSnooze(minutes)
            }
        )
    }
}
