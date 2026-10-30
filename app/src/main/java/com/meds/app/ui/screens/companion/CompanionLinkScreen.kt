package com.meds.app.ui.screens.companion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.ui.components.InfoRow
import com.meds.app.ui.components.MedsCard
import com.meds.app.ui.components.MedsWordmark
import com.meds.app.ui.components.PrimaryActionButton
import com.meds.app.ui.components.SectionLabel

@Composable
fun CompanionLinkScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        MedsWordmark()

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Acompaña con calma.",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Vincula el día de tu persona de confianza y acompáñala sin invadir su espacio.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        MedsCard {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCode2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            SectionLabel(text = "Vinculación")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Escanea el código o usa el código manual",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            InfoRow(
                icon = Icons.Outlined.QrCode2,
                text = "El código QR se habilitará con la cámara en develop."
            )
            Spacer(Modifier.height(10.dp))
            InfoRow(
                icon = Icons.Outlined.VpnKey,
                text = "El código manual tiene 8 caracteres y dura 10 minutos."
            )
        }

        Spacer(Modifier.height(20.dp))

        PrimaryActionButton(
            text = "Ver panel de ejemplo",
            onClick = onContinue
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "La vinculación real, la lectura del QR y el panel en tiempo real se implementarán en develop.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
