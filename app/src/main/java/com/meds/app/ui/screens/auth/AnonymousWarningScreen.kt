package com.meds.app.ui.screens.auth

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meds.app.ui.components.InfoRow
import com.meds.app.ui.components.MedsCard
import com.meds.app.ui.components.MedsWordmark
import com.meds.app.ui.components.PrimaryActionButton
import com.meds.app.ui.components.SecondaryActionButton

@Composable
fun AnonymousWarningScreen(
    authLoading: Boolean,
    authError: String?,
    onContinueAnonymously: () -> Unit,
    onCreateAccount: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, enabled = !authLoading) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(4.dp))
                MedsWordmark()
            }

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "Antes de continuar sin cuenta",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Puedes usar MEDs ahora mismo. Solo ten en cuenta estas limitaciones:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            MedsCard {
                InfoRow(
                    icon = Icons.Outlined.PhoneAndroid,
                    text = "Tus datos quedan ligados a este dispositivo y a esta sesión."
                )
                Spacer(Modifier.height(16.dp))
                InfoRow(
                    icon = Icons.Outlined.CloudOff,
                    text = "Si desinstalas la app, borras sus datos o cierras sesión, no podrás recuperar tu información."
                )
                Spacer(Modifier.height(16.dp))
                InfoRow(
                    icon = Icons.Outlined.Devices,
                    text = "No podrás acceder a tus medicamentos desde otro dispositivo."
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Puedes vincularlo después",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Empieza sin cuenta y vincula un correo desde Ajustes > Cuenta. " +
                            "Así conservas todo lo que ya hayas registrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (authError != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = authError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(28.dp))

            PrimaryActionButton(
                text = "Crear cuenta con correo",
                onClick = onCreateAccount,
                enabled = !authLoading
            )
            Spacer(Modifier.height(10.dp))
            SecondaryActionButton(
                text = if (authLoading) {
                    "Preparando tu espacio..."
                } else {
                    "Entendido, continuar sin cuenta"
                },
                onClick = onContinueAnonymously,
                enabled = !authLoading
            )

            Spacer(Modifier.height(18.dp))
            Text(
                text = "Una cuenta anónima no se puede recuperar si cierras sesión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}
