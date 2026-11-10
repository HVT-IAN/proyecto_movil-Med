package com.meds.app.presentation.companion

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.meds.app.domain.model.DoseStatus
import com.meds.app.core.ui.components.DoseLogRow
import com.meds.app.core.ui.components.HeroCard
import com.meds.app.core.ui.components.InfoRow
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.MedsWordmark
import com.meds.app.core.ui.components.PlantArt
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SecondaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.presentation.settings.AccountSettingsScreen
import com.meds.app.presentation.settings.AccountUiState
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.core.common.parsePairingCode
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    val viewModel: CompanionViewModel = viewModel(key = "companion_${account.userId}")
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

@Composable
private fun LinkPatientContent(
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

@Composable
private fun CompanionPanelContent(
    name: String,
    state: CompanionUiState,
    modifier: Modifier = Modifier,
    onNudge: () -> Unit,
    onUnlink: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val patient = state.selectedPatient
    val day = state.day
    val locale = Locale.forLanguageTag("es")
    val dateText = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale))
        .replaceFirstChar { character ->
            if (character.isLowerCase()) character.titlecase(locale) else character.toString()
        }
    val colors = LocalMedsColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp)
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

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Acompañas a",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = patient?.name?.ifBlank { day?.patientName.orEmpty() }
                        ?.ifBlank { "tu paciente" }
                        ?: "tu paciente",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(colors.companionContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (patient?.name ?: day?.patientName ?: "A")
                        .trim()
                        .take(1)
                        .uppercase()
                        .ifBlank { "A" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.companion
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        when {
            day == null && state.loading -> {
                MedsCard {
                    Text(
                        text = "Cargando el día...",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            day == null -> {
                MedsCard {
                    Text(
                        text = "No pudimos acceder a los datos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Es posible que la persona haya retirado tu acceso o que la conexión no esté disponible.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryActionButton(
                        text = "Desvincular",
                        onClick = onUnlink
                    )
                }
            }

            else -> {
                val taken = day.doses.count { it.status == DoseStatus.TAKEN }
                val total = day.doses.size
                val progress = if (total > 0) taken.toFloat() / total.toFloat() else 0f
                val allDone = total > 0 && taken == total

                HeroCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SectionLabel(text = "Hoy")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$taken de $total tomas",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = when {
                                    total == 0 -> "Todavía no hay tomas registradas."
                                    allDone -> "Todo al día. No hace falta insistir."
                                    else -> "Puedes enviar un recordatorio si hace falta."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        PlantArt(
                            stage = day.plant.level,
                            modifier = Modifier.size(84.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tomas de hoy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$taken de $total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))

                if (day.doses.isEmpty()) {
                    MedsCard {
                        Text(
                            text = "Aún no hay tomas para hoy.",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Cuando el paciente registre sus medicamentos, aparecerán aquí en tiempo real.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    MedsCard {
                        day.doses.forEachIndexed { index, dose ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 1.dp
                                )
                            }
                            DoseLogRow(log = dose)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                MedsCard {
                    SectionLabel(text = "Apoyo")
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Enviar un recordatorio",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Un mensaje breve y amable puede ayudar a retomar el ritmo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryActionButton(
                        text = "Enviar recordatorio",
                        onClick = onNudge,
                        enabled = !state.nudgeSending,
                        icon = Icons.Outlined.Notifications
                    )
                }

                Spacer(Modifier.height(12.dp))

                TextActionButton(
                    text = "Desvincular a ${patient?.name?.ifBlank { "este paciente" } ?: "este paciente"}",
                    onClick = onUnlink,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                MedsCard {
                    InfoRow(
                        icon = Icons.Outlined.CheckCircle,
                        text = "Los cambios del paciente se actualizan en tiempo real mientras la app está abierta."
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Sesión de ${name.trim().ifBlank { "acompañante" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
