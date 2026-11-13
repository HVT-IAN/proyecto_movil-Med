package com.meds.app.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.meds.app.core.common.isValidEmail

@Composable
fun LinkEmailDialog(
    authLoading: Boolean,
    authError: String?,
    onLinkEmail: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var linkEmail by rememberSaveable { mutableStateOf("") }
    var linkPassword by rememberSaveable { mutableStateOf("") }
    var linkConfirmPassword by rememberSaveable { mutableStateOf("") }
    var linkPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val emailValid = isValidEmail(linkEmail)
    val passwordValid = linkPassword.length >= 6
    val passwordsMatch = linkConfirmPassword == linkPassword
    val canLink = emailValid && passwordValid && passwordsMatch && !authLoading

    AlertDialog(
        onDismissRequest = {
            if (!authLoading) {
                onDismiss()
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
                if (authError != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = authError,
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
                Text(if (authLoading) "Vinculando..." else "Vincular")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !authLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}
