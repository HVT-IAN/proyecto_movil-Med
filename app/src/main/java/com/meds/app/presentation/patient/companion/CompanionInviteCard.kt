package com.meds.app.presentation.patient.companion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meds.app.core.common.QrCodeGenerator
import com.meds.app.core.common.formatCountdownMillis
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SecondaryActionButton
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.domain.model.PairingInvite

@Composable
internal fun CompanionInviteSection(
    activeInvite: PairingInvite?,
    remainingMillis: Long,
    generating: Boolean,
    onGenerateInvite: (previousCode: String?) -> Unit,
    onCopyCode: (String) -> Unit
) {
    val isExpired = activeInvite != null && remainingMillis <= 0L

    when {
        activeInvite == null -> {
            MedsCard {
                CompanionInviteHeader()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Genera un código temporal para que tu acompañante escanee el QR o lo escriba manualmente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                PrimaryActionButton(
                    text = if (generating) "Generando..." else "Generar código",
                    onClick = { onGenerateInvite(null) },
                    enabled = !generating,
                    icon = Icons.Outlined.QrCode2
                )
            }
        }

        isExpired -> {
            MedsCard {
                CompanionInviteHeader()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Este código ya venció. Genera uno nuevo para vincular a tu acompañante.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                PrimaryActionButton(
                    text = if (generating) "Generando..." else "Generar nuevo código",
                    onClick = { onGenerateInvite(null) },
                    enabled = !generating,
                    icon = Icons.Outlined.QrCode2
                )
            }
        }

        else -> {
            val qrBitmap = remember(activeInvite.code) {
                runCatching {
                    QrCodeGenerator.generate("meds://pair?code=${activeInvite.code}")
                }.getOrNull()
            }
            MedsCard {
                CompanionInviteHeader()
                Spacer(Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Código QR para vincular a un acompañante",
                            modifier = Modifier
                                .padding(12.dp)
                                .size(220.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(24.dp)
                                .size(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No se pudo generar el QR.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = activeInvite.code,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Vence en ${formatCountdownMillis(remainingMillis)}",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                TextActionButton(
                    text = "Copiar código",
                    onClick = { onCopyCode(activeInvite.code) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(6.dp))
                SecondaryActionButton(
                    text = if (generating) "Generando..." else "Generar nuevo QR",
                    onClick = { onGenerateInvite(activeInvite.code) },
                    enabled = !generating,
                    icon = Icons.Outlined.Refresh
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Al generar uno nuevo, el código actual deja de funcionar.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CompanionInviteHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    LocalMedsColors.current.companionContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.QrCode2,
                contentDescription = null,
                tint = LocalMedsColors.current.companion,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Vincular acompañante",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "El código dura 10 minutos y es de un solo uso.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
