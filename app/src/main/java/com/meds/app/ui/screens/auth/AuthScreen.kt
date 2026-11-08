package com.meds.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.meds.app.ui.components.MedsCard
import com.meds.app.ui.components.MedsWordmark
import com.meds.app.ui.components.PrimaryActionButton
import com.meds.app.ui.components.SecondaryActionButton

enum class AuthMode {
    LOGIN,
    REGISTER
}

@Composable
fun AuthScreen(
    mode: AuthMode,
    authLoading: Boolean,
    authError: String?,
    authNotice: String?,
    onModeChange: (AuthMode) -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onAnonymous: () -> Unit,
    onSendPasswordReset: (String) -> Unit,
    onClearAuthMessages: () -> Unit
) {
    val isRegister = mode == AuthMode.REGISTER
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val emailValid = isValidEmail(email)
    val passwordValid = if (isRegister) password.length >= 6 else password.isNotBlank()
    val passwordsMatch = !isRegister || confirmPassword == password
    val canSubmit = emailValid && passwordValid && passwordsMatch && !authLoading

    val submit: () -> Unit = {
        focusManager.clearFocus()
        if (isRegister) {
            onRegister(email.trim(), password)
        } else {
            onLogin(email.trim(), password)
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            MedsWordmark()

            Spacer(Modifier.height(26.dp))
            Text(
                text = if (isRegister) "Crea tu cuenta" else "Bienvenido de nuevo",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isRegister) {
                    "Guarda tu progreso y recupera tus medicamentos desde cualquier dispositivo."
                } else {
                    "Inicia sesión para recuperar tu jardín, tus medicamentos y tu historial."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            MedsCard {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("tu@correo.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    isError = email.isNotBlank() && !emailValid,
                    supportingText = if (email.isNotBlank() && !emailValid) {
                        { Text("Escribe un correo válido.") }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (passwordVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = isRegister && password.isNotBlank() && !passwordValid,
                    supportingText = if (isRegister && password.isNotBlank() && !passwordValid) {
                        { Text("Usa al menos 6 caracteres.") }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isRegister) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canSubmit) submit() else focusManager.clearFocus()
                        }
                    )
                )
                if (isRegister) {
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
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
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        isError = confirmPassword.isNotBlank() && !passwordsMatch,
                        supportingText = if (confirmPassword.isNotBlank() && !passwordsMatch) {
                            { Text("Las contraseñas no coinciden.") }
                        } else {
                            null
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (canSubmit) submit() else focusManager.clearFocus()
                            }
                        )
                    )
                }
            }

            if (authError != null) {
                Spacer(Modifier.height(16.dp))
                MessageCard(
                    text = authError,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    iconTint = MaterialTheme.colorScheme.error,
                    isError = true
                )
            }

            if (authNotice != null) {
                Spacer(Modifier.height(16.dp))
                MessageCard(
                    text = authNotice,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isError = false
                )
            }

            if (!isRegister) {
                TextButton(
                    onClick = {
                        onClearAuthMessages()
                        showResetDialog = true
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("¿Olvidaste tu contraseña?")
                }
            } else {
                Spacer(Modifier.height(12.dp))
            }

            PrimaryActionButton(
                text = when {
                    authLoading && isRegister -> "Creando cuenta..."
                    authLoading -> "Entrando..."
                    isRegister -> "Crear cuenta"
                    else -> "Entrar"
                },
                onClick = submit,
                enabled = canSubmit
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "o",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Spacer(Modifier.height(20.dp))

            SecondaryActionButton(
                text = if (authLoading) "Preparando tu espacio..." else "Continuar sin cuenta",
                onClick = onAnonymous,
                enabled = !authLoading,
                icon = Icons.Outlined.PersonOutline
            )

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = {
                    onModeChange(if (isRegister) AuthMode.LOGIN else AuthMode.REGISTER)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isRegister) {
                        "¿Ya tienes cuenta? Inicia sesión"
                    } else {
                        "¿No tienes cuenta? Crea una"
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Tus datos se guardan de forma segura. " +
                    "También puedes empezar sin cuenta y vincular un correo más adelante.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Recuperar contraseña") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Escribe el correo con el que te registraste y te enviaremos un enlace para crear una nueva contraseña.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onSendPasswordReset(email.trim())
                    },
                    enabled = emailValid
                ) {
                    Text("Enviar enlace")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MessageCard(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    isError: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = if (isError) {
                Icons.Outlined.ErrorOutline
            } else {
                Icons.Outlined.CheckCircle
            },
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

private fun isValidEmail(value: String): Boolean {
    val trimmed = value.trim()
    if (trimmed.isEmpty()) return false
    return Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(trimmed)
}
