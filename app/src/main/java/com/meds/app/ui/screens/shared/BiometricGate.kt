package com.meds.app.ui.screens.shared

import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.meds.app.ui.components.MedsWordmark
import com.meds.app.ui.components.PrimaryActionButton

private const val ALLOWED_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK
private const val RELOCK_AFTER_MS = 5 * 60 * 1000L

fun isBiometricAvailable(context: Context): Boolean =
    BiometricManager.from(context)
        .canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

fun requestBiometricAuthentication(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    negativeText: String = "Cancelar",
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult
            ) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onError("No se reconoció la identidad. Intenta de nuevo.")
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
        .setNegativeButtonText(negativeText)
        .build()

    prompt.authenticate(promptInfo)
}

@Composable
fun BiometricGate(
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val canAuthenticate = remember(activity) {
        activity != null && isBiometricAvailable(activity)
    }
    var unlocked by rememberSaveable {
        mutableStateOf(!enabled || !canAuthenticate)
    }
    var prompting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var backgroundedAt by rememberSaveable { mutableStateOf(0L) }
    val currentUnlocked by rememberUpdatedState(unlocked)
    val currentPrompting by rememberUpdatedState(prompting)
    val currentCanAuthenticate by rememberUpdatedState(canAuthenticate)
    val currentEnabled by rememberUpdatedState(enabled)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (
                        currentEnabled &&
                        currentUnlocked &&
                        currentCanAuthenticate &&
                        !currentPrompting
                    ) {
                        backgroundedAt = SystemClock.elapsedRealtime()
                    }
                }

                Lifecycle.Event.ON_START -> {
                    if (
                        currentEnabled &&
                        currentUnlocked &&
                        backgroundedAt > 0L &&
                        SystemClock.elapsedRealtime() - backgroundedAt >= RELOCK_AFTER_MS
                    ) {
                        unlocked = false
                        error = null
                    }
                    backgroundedAt = 0L
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun requestAuthentication() {
        if (activity == null || prompting) return
        prompting = true
        error = null

        requestBiometricAuthentication(
            activity = activity,
            title = "Desbloquea MEDs",
            subtitle = "Usa tu huella o rostro para continuar",
            onSuccess = {
                prompting = false
                unlocked = true
            },
            onError = { message ->
                prompting = false
                error = message
            }
        )
    }

    LaunchedEffect(enabled, canAuthenticate, unlocked) {
        if (enabled && !unlocked && canAuthenticate) {
            requestAuthentication()
        }
    }

    if (unlocked) {
        content()
    } else {
        BiometricLockScreen(
            error = error,
            onUnlock = { requestAuthentication() }
        )
    }
}

@Composable
private fun BiometricLockScreen(
    error: String?,
    onUnlock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        MedsWordmark()
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Tu información merece privacidad.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Desbloquea con tu huella o rostro para continuar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(32.dp))
        PrimaryActionButton(
            text = "Desbloquear con biometría",
            onClick = onUnlock
        )
    }
}

@Composable
fun BiometricOptInDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Protege tu información") },
        text = {
            Column {
                Text(
                    text = "¿Quieres desbloquear MEDS con tu huella o rostro?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Puedes cambiarlo cuando quieras en Ajustes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (error != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val currentActivity = activity
                    if (currentActivity == null) {
                        error = "No se encontró el sensor de biometría."
                    } else {
                        requestBiometricAuthentication(
                            activity = currentActivity,
                            title = "Activa el desbloqueo",
                            subtitle = "Confirma tu huella o rostro para activarlo",
                            onSuccess = {
                                error = null
                                onEnable()
                            },
                            onError = { message -> error = message }
                        )
                    }
                }
            ) {
                Text("Activar huella")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ahora no")
            }
        }
    )
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current != null) {
        if (current is FragmentActivity) return current
        current = (current as? ContextWrapper)?.baseContext
    }
    return null
}
