package com.meds.app.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SignOutConfirmDialog(
    isAnonymous: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cerrar sesión?") },
        text = {
            Text(
                if (isAnonymous) {
                    "Esta cuenta es anónima. Si cierras sesión, no podrás volver a " +
                        "acceder a tus medicamentos ni a tu historial. " +
                        "Te recomendamos vincular un correo primero."
                } else {
                    "Podrás volver a entrar cuando quieras con tu correo y contraseña."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Cerrar sesión",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
