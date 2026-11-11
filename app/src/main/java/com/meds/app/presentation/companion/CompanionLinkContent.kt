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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.components.InfoRow
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.MedsWordmark
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SecondaryActionButton
import com.meds.app.core.ui.theme.LocalMedsColors

@Composable
internal fun LinkPatientContent(
    name: String,
    linking: Boolean,
    modifier: Modifier = Modifier,
    onScan: () -> Unit,
    onManual: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val colors = LocalMedsColors.current
    val displayName = name.trim()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp)
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
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (displayName.isNotEmpty()) "Hola, $displayName" else "Modo acompañante",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Acompaña sin invadir. Verás el estado del día y podrás enviar un recordatorio cuando haga falta.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        MedsCard {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(colors.companionContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCode2,
                    contentDescription = null,
                    tint = colors.companion,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Vincular paciente",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Escanea el código temporal que genere la persona que acompañas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            PrimaryActionButton(
                text = if (linking) "Vinculando..." else "Escanear código",
                onClick = onScan,
                enabled = !linking,
                icon = Icons.Outlined.QrCode2
            )
            Spacer(Modifier.height(10.dp))
            SecondaryActionButton(
                text = "Ingresar código manualmente",
                onClick = onManual,
                enabled = !linking,
                icon = Icons.Outlined.VpnKey
            )
        }

        Spacer(Modifier.height(20.dp))

        MedsCard {
            InfoRow(
                icon = Icons.Outlined.QrCode2,
                text = "El código expira en 10 minutos y solo se puede usar una vez."
            )
            Spacer(Modifier.height(14.dp))
            InfoRow(
                icon = Icons.Outlined.VpnKey,
                text = "El paciente puede retirar tu acceso en cualquier momento."
            )
            Spacer(Modifier.height(14.dp))
            InfoRow(
                icon = Icons.Outlined.Verified,
                text = "Solo verás medicamentos y tomas del día. Nada más."
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
