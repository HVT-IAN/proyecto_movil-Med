package com.meds.app.presentation.companion

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.meds.app.core.common.parsePairingCode
import com.meds.app.di.MedsViewModelFactory
import com.meds.app.presentation.settings.AccountSettingsScreen
import com.meds.app.presentation.settings.AccountUiState
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Contenedor del módulo acompañante.
 *
 * Coordina el ViewModel, los avisos, los diálogos y el cambio a Ajustes. La
 * interfaz de vinculación y el panel están separados en
 * `CompanionLinkContent.kt` y `CompanionPanelContent.kt`.
 */
@Composable
fun CompanionApp(
    account: AccountUiState,
    onLinkEmail: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onClearAuthMessages: () -> Unit,
    onBiometricChange: (Boolean) -> Unit
) {
    val name = account.name
    var showSettings by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = showSettings) { showSettings = false }

    if (showSettings) {
        AccountSettingsScreen(
            account = account,
            onBack = { showSettings = false },
            onLinkEmail = onLinkEmail,
            onSignOut = onSignOut,
            onClearAuthMessages = onClearAuthMessages,
            onBiometricChange = onBiometricChange
        )
        return
    }

    val viewModel: CompanionViewModel =
        viewModel(key = "companion_${account.userId}", factory = MedsViewModelFactory)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showManualDialog by remember { mutableStateOf(false) }
    var manualCode by remember { mutableStateOf("") }
    var showNudgeDialog by remember { mutableStateOf(false) }
    var nudgeText by remember { mutableStateOf("¿Ya tomaste tu medicamento?") }
    var showUnlinkDialog by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val raw = result.contents
        if (raw.isNullOrBlank()) return@rememberLauncherForActivityResult

        val code = parsePairingCode(raw)
        if (code != null) {
            viewModel.linkWithCode(code, name)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("El código escaneado no es válido.")
            }
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
        if (state.linkedPatients.isEmpty()) {
            LinkPatientContent(
                name = name,
                linking = state.linking,
                modifier = Modifier.padding(padding),
                onScan = {
                    scanLauncher.launch(
                        ScanOptions()
                            .setDesiredBarcodeFormats("QR_CODE")
                            .setPrompt("Escanea el código de MEDs")
                            .setBeepEnabled(false)
                            .setOrientationLocked(true)
                    )
                },
                onManual = { showManualDialog = true },
                onOpenSettings = { showSettings = true }
            )
        } else {
            CompanionPanelContent(
                name = name,
                state = state,
                modifier = Modifier.padding(padding),
                onNudge = { showNudgeDialog = true },
                onUnlink = { showUnlinkDialog = true },
                onOpenSettings = { showSettings = true }
            )
        }
    }

    if (showManualDialog) {
        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = { Text("Código de vinculación") },
            text = {
                Column {
                    Text(
                        text = "Escribe el código de 8 caracteres que aparece junto al QR del paciente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = manualCode,
                        onValueChange = { value ->
                            manualCode = value
                                .uppercase(Locale.US)
                                .filter(Char::isLetterOrDigit)
                                .take(8)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Código") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 4.sp
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val code = manualCode
                        showManualDialog = false
                        manualCode = ""
                        viewModel.linkWithCode(code, name)
                    },
                    enabled = manualCode.length == 8 && !state.linking
                ) {
                    Text("Vincular")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val patient = state.selectedPatient
    if (showNudgeDialog && patient != null) {
        AlertDialog(
            onDismissRequest = { showNudgeDialog = false },
            title = { Text("Enviar recordatorio") },
            text = {
                Column {
                    Text(
                        text = "Un mensaje breve y amable puede ayudar sin generar presión.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = nudgeText,
                        onValueChange = { value ->
                            if (value.length <= 160) nudgeText = value
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Mensaje") },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendNudge(patient.uid, name, nudgeText)
                        nudgeText = "¿Ya tomaste tu medicamento?"
                        showNudgeDialog = false
                    },
                    enabled = nudgeText.isNotBlank() && !state.nudgeSending
                ) {
                    Text(if (state.nudgeSending) "Enviando..." else "Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNudgeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showUnlinkDialog && patient != null) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = { Text("¿Desvincular a ${patient.name.ifBlank { "este paciente" }}?") },
            text = {
                Text(
                    "Dejarás de ver sus tomas del día. Puedes volver a vincularte con un código nuevo."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnlinkDialog = false
                        viewModel.unlinkPatient(patient.uid)
                    }
                ) {
                    Text("Desvincular", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
