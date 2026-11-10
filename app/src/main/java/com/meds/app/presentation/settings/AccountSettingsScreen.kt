package com.meds.app.presentation.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meds.app.BuildConfig
import com.meds.app.presentation.root.UserRole
import com.meds.app.core.ui.components.InfoRow
import com.meds.app.core.ui.components.LabelValue
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.SectionLabel
import com.meds.app.integration.biometric.createDeviceCredentialIntent
import com.meds.app.integration.biometric.findFragmentActivity
import com.meds.app.integration.biometric.requestBiometricAuthentication
import com.meds.app.core.ui.theme.LocalMedsColors

data class AccountUiState(
    val userId: String = "",
    val name: String = "",
    val role: UserRole = UserRole.PATIENT,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val biometricEnabled: Boolean = false,
    val biometricAvailable: Boolean = true,
    val deviceCredentialAvailable: Boolean = false,
    val authLoading: Boolean = false,
    val authError: String? = null,
    val authNotice: String? = null
)

@Composable
fun AccountSettingsScreen(
    account: AccountUiState,
    onBack: () -> Unit,
    onLinkEmail: (String, String) -> Unit,
    onSignOut: () -> Unit,
    onClearAuthMessages: () -> Unit,
    onBiometricChange: (Boolean) -> Unit
) {
    val colors = LocalMedsColors.current
    val roleLabel = if (account.role == UserRole.PATIENT) "Paciente" else "Acompañante"
    val avatarBackground = if (account.role == UserRole.COMPANION) {
        colors.companionContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val avatarTint = if (account.role == UserRole.COMPANION) {
        colors.companion
    } else {
        MaterialTheme.colorScheme.primary
    }
    val initial = account.name.trim().take(1).uppercase().ifBlank {
        if (account.role == UserRole.PATIENT) "P" else "A"
    }

    var showLinkDialog by rememberSaveable { mutableStateOf(false) }
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    var linkEmail by rememberSaveable { mutableStateOf("") }
    var linkPassword by rememberSaveable { mutableStateOf("") }
    var linkConfirmPassword by rememberSaveable { mutableStateOf("") }
    var linkPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    var biometricError by remember { mutableStateOf<String?>(null) }

    val pinLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            biometricError = null
            onBiometricChange(true)
        } else {
            biometricError = "No se confirmó el PIN. Intenta de nuevo."
        }
    }

    val emailValid = isValidEmail(linkEmail)
    val passwordValid = linkPassword.length >= 6
    val passwordsMatch = linkConfirmPassword == linkPassword
    val canLink = emailValid && passwordValid && passwordsMatch && !account.authLoading

    LaunchedEffect(account.isAnonymous) {
        if (!account.isAnonymous) {
            showLinkDialog = false
        }
    }

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
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(avatarBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.headlineMedium,
                        color = avatarTint
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name.trim().ifBlank { "Tu cuenta MEDs" },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = roleLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (account.isAnonymous) {
                            "Cuenta anónima"
                        } else {
                            account.email.orEmpty()
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (account.isAnonymous) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            SectionLabel(text = "Cuenta")
            Spacer(Modifier.height(10.dp))

            MedsCard {
                if (account.isAnonymous) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Estás usando una cuenta anónima",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tus datos están ligados a este dispositivo. " +
                            "Vincula un correo para no perderlos si cambias de teléfono, " +
                            "reinstalas la app o cierras sesión.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(18.dp))
                    PrimaryActionButton(
                        text = "Vincular correo electrónico",
                        onClick = {
                            onClearAuthMessages()
                            linkEmail = ""
                            linkPassword = ""
                            linkConfirmPassword = ""
                            showLinkDialog = true
                        },
                        enabled = !account.authLoading,
                        icon = Icons.Outlined.Link
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Tus medicamentos, tomas e historial se conservarán al vincular.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LabelValue(
                        label = "Correo electrónico",
                        value = account.email.orEmpty()
                    )
                    Spacer(Modifier.height(16.dp))
                    LabelValue(
                        label = "Tipo de uso",
                        value = roleLabel
                    )
                    Spacer(Modifier.height(16.dp))
                    InfoRow(
                        icon = Icons.Outlined.CheckCircle,
                        text = "Tu cuenta está vinculada. Podrás recuperar tus datos " +
                            "iniciando sesión en cualquier dispositivo."
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionLabel(text = "Seguridad")
            Spacer(Modifier.height(10.dp))

            MedsCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bloquear MEDS al abrir",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = when {
                                !account.biometricAvailable &&
                                    !account.deviceCredentialAvailable ->
                                    "Configura una huella, rostro o PIN en el teléfono."
                                account.biometricEnabled ->
                                    "Activado. Se pedirá huella, rostro o PIN al abrir."
                                else ->
                                    "Desactivado. MEDS abre sin bloqueo."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = account.biometricEnabled,
                        enabled = account.biometricAvailable ||
                            account.deviceCredentialAvailable,
                        onCheckedChange = { checked ->
                            biometricError = null
                            if (!checked) {
                                onBiometricChange(false)
                            } else {
                                val currentActivity = activity
                                if (account.biometricAvailable && currentActivity != null) {
                                    requestBiometricAuthentication(
                                        activity = currentActivity,
                                        title = "Activa el bloqueo",
                                        subtitle = "Confirma con huella, rostro o PIN",
                                        onSuccess = { onBiometricChange(true) },
                                        onFailed = {},
                                        onError = { code, message ->
                                            if (
                                                code == BiometricPrompt.ERROR_LOCKOUT ||
                                                code == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
                                            ) {
                                                val intent = createDeviceCredentialIntent(context)
                                                if (intent != null) {
                                                    pinLauncher.launch(intent)
                                                } else {
                                                    biometricError =
                                                        "No hay PIN configurado. Espera 30 segundos."
                                                }
                                            } else if (
                                                code != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                                code != BiometricPrompt.ERROR_USER_CANCELED
                                            ) {
                                                biometricError = message
                                            }
                                        }
                                    )
                                } else if (account.deviceCredentialAvailable) {
                                    val intent = createDeviceCredentialIntent(context)
                                    if (intent != null) {
                                        pinLauncher.launch(intent)
                                    } else {
                                        biometricError =
                                            "No se pudo abrir la pantalla del PIN."
                                    }
                                } else {
                                    biometricError =
                                        "Configura una huella o un PIN en el teléfono."
                                }
                            }
                        }
                    )
                }
                if (biometricError != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = biometricError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionLabel(text = "Sesión")
            Spacer(Modifier.height(10.dp))

            MedsCard {
                if (account.isAnonymous) {
                    InfoRow(
                        icon = Icons.Outlined.WarningAmber,
                        text = "Si cierras sesión ahora, perderás el acceso a esta cuenta " +
                            "anónima y a sus datos. Vincula un correo primero si quieres conservarlos."
                    )
                    Spacer(Modifier.height(18.dp))
                }
                OutlinedButton(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    enabled = !account.authLoading,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "MEDs ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
        }
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!account.authLoading) {
                    showLinkDialog = false
                    onClearAuthMessages()
                }
            },
            title = { Text("Vincular correo electrónico") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Tu cuenta anónima pasará a tener acceso con correo y contraseña. " +
                            "Conservarás todos tus datos actuales.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = linkEmail,
                        onValueChange = { linkEmail = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        isError = linkEmail.isNotBlank() && !emailValid,
                        supportingText = if (linkEmail.isNotBlank() && !emailValid) {
                            { Text("Escribe un correo válido.") }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = linkPassword,
                        onValueChange = { linkPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { linkPasswordVisible = !linkPasswordVisible }) {
                                Icon(
                                    imageVector = if (linkPasswordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = if (linkPasswordVisible) {
                                        "Ocultar contraseña"
                                    } else {
                                        "Mostrar contraseña"
                                    }
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (linkPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = linkPassword.isNotBlank() && !passwordValid,
                        supportingText = if (linkPassword.isNotBlank() && !passwordValid) {
                            { Text("Usa al menos 6 caracteres.") }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = linkConfirmPassword,
                        onValueChange = { linkConfirmPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirmar contraseña") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (linkPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = linkConfirmPassword.isNotBlank() && !passwordsMatch,
                        supportingText = if (linkConfirmPassword.isNotBlank() && !passwordsMatch) {
                            { Text("Las contraseñas no coinciden.") }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )
                    if (account.authError != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = account.authError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onLinkEmail(linkEmail.trim(), linkPassword) },
                    enabled = canLink
                ) {
                    Text(if (account.authLoading) "Vinculando..." else "Vincular")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLinkDialog = false
                        onClearAuthMessages()
                    },
                    enabled = !account.authLoading
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("¿Cerrar sesión?") },
            text = {
                Text(
                    if (account.isAnonymous) {
                        "Esta cuenta es anónima. Si cierras sesión, no podrás volver a " +
                            "acceder a tus medicamentos ni a tu historial. " +
                            "Te recomendamos vincular un correo primero."
                    } else {
                        "Podrás volver a entrar cuando quieras con tu correo y contraseña."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text(
                        text = "Cerrar sesión",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun isValidEmail(value: String): Boolean {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return false
    return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(trimmed)
}
