package com.meds.app.presentation.patient.companion

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meds.app.core.common.relativeTime
import com.meds.app.core.ui.components.InfoRow
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.domain.model.CompanionLink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PatientCompanionScreen(
    viewModel: PatientCompanionViewModel,
    patientName: String,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    var companionToRevoke by remember { mutableStateOf<CompanionLink?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val activeInvite = state.invite
    val remainingMillis = (activeInvite?.expiresAt ?: 0L) - now

    LaunchedEffect(activeInvite?.code) {
        if (activeInvite == null) return@LaunchedEffect
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    LaunchedEffect(state.error) {
        val message = state.error
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.infoMessage) {
        val message = state.infoMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearInfo()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                Text(
                    text = "Acompañante",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Comparte tu día con alguien de confianza.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            CompanionInviteSection(
                activeInvite = activeInvite,
                remainingMillis = remainingMillis,
                generating = state.generating,
                onGenerateInvite = { previousCode ->
                    viewModel.generateInvite(
                        patientName = patientName,
                        previousCode = previousCode
                    )
                },
                onCopyCode = { code ->
                    clipboard.setText(AnnotatedString(code))
                    scope.launch {
                        snackbarHostState.showSnackbar("Código copiado.")
                    }
                }
            )

            Spacer(Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Personas vinculadas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (state.loading) {
                    Text(
                        text = "Cargando...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(10.dp))

            if (state.companions.isEmpty()) {
                MedsCard {
                    Text(
                        text = "Aún no hay acompañantes vinculados.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Cuando alguien escanee tu código, aparecerá aquí y podrás retirar su acceso en cualquier momento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                MedsCard {
                    state.companions.forEachIndexed { index, companion ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        LocalMedsColors.current.companionContainer,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = companion.name.trim().take(1).uppercase()
                                        .ifBlank { "A" },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = LocalMedsColors.current.companion
                                )
                            }
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = companion.name.ifBlank { "Acompañante" },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Acceso activo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { companionToRevoke = companion }) {
                                Text(
                                    text = "Retirar",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            if (state.nudges.isNotEmpty()) {
                Spacer(Modifier.height(26.dp))
                Text(
                    text = "Recordatorios recibidos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(10.dp))
                MedsCard {
                    state.nudges.take(3).forEachIndexed { index, nudge ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = LocalMedsColors.current.companion,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(20.dp)
                            )
                            Spacer(Modifier.size(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nudge.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = buildString {
                                        append(nudge.fromName.ifBlank { "Tu acompañante" })
                                        if (nudge.createdAt > 0L) {
                                            append(" · ")
                                            append(relativeTime(nudge.createdAt))
                                        }
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            MedsCard {
                SectionLabel(text = "Privacidad")
                Spacer(Modifier.height(12.dp))
                InfoRow(
                    icon = Icons.Outlined.Verified,
                    text = "Tu acompañante solo verá tus medicamentos y las tomas del día. No verá tu historial completo."
                )
                Spacer(Modifier.height(12.dp))
                InfoRow(
                    icon = Icons.Outlined.QrCode2,
                    text = "Puedes retirar el acceso cuando quieras y generar un código nuevo."
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }

    val revoke = companionToRevoke
    if (revoke != null) {
        AlertDialog(
            onDismissRequest = { companionToRevoke = null },
            title = { Text("¿Retirar el acceso?") },
            text = {
                Text(
                    "${revoke.name.ifBlank { "Esta persona" }} dejará de ver tus medicamentos y tomas del día."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.revokeCompanion(revoke.uid)
                        companionToRevoke = null
                    }
                ) {
                    Text("Retirar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { companionToRevoke = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
