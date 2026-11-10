package com.meds.app.ui.screens.shared

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Lock
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
import com.meds.app.ui.components.SecondaryActionButton
import com.meds.app.ui.components.TextActionButton

private const val ALLOWED_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK
private const val RELOCK_AFTER_MS = 5 * 60 * 1000L

fun isBiometricAvailable(context: Context): Boolean =
    BiometricManager.from(context)
        .canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

fun isDeviceCredentialAvailable(context: Context): Boolean {
    val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    return keyguard?.isDeviceSecure == true
}

@Suppress("DEPRECATION")
fun createDeviceCredentialIntent(context: Context): Intent? {
    val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        ?: return null
    if (!keyguard.isDeviceSecure) return null
    return keyguard.createConfirmDeviceCredentialIntent(
        "Confirma que eres tú",
        "Usa tu PIN, patrón o contraseña para abrir MEDS"
    )
}

fun requestBiometricAuthentication(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    negativeText: String = "Cancelar",
    onSuccess: () -> Unit,
    onFailed: () -> Unit = {},
    onError: (Int, String) -> Unit
): BiometricPrompt {
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
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                onFailed()
            }
        }
    )

    val builder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            builder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            @Suppress("DEPRECATION")
            builder.setDeviceCredentialAllowed(true)
        }

        else -> {
            builder.setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            builder.setNegativeButtonText(negativeText)
        }
    }

    prompt.authenticate(builder.build())
    return prompt
}

@Composable
fun BiometricGate(
    enabled: Boolean = true,
    isAnonymous: Boolean = false,
    onSignOut: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val canUseBiometrics = remember(context) { isBiometricAvailable(context) }
    val canUsePin = remember(context) { isDeviceCredentialAvailable(context) }
    val canProtect = canUseBiometrics || canUsePin

    var unlocked by rememberSaveable {
        mutableStateOf(!enabled || !canProtect)
    }
    var prompting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var prompt by remember { mutableStateOf<BiometricPrompt?>(null) }

    val pinLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            unlocked = true
            error = null
        } else {
            error = "No se confirmó el PIN. Intenta de nuevo."
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var backgroundedAt by rememberSaveable { mutableStateOf(0L) }
    val currentUnlocked by rememberUpdatedState(unlocked)
    val currentPrompting by rememberUpdatedState(prompting)
    val currentCanProtect by rememberUpdatedState(canProtect)
    val currentEnabled by rememberUpdatedState(enabled)

    fun launchDeviceCredential() {
        val intent = createDeviceCredentialIntent(context)
        if (intent == null) {
            error = "No hay PIN, patrón o contraseña configurados en el teléfono."
        } else {
            error = null
            pinLauncher.launch(intent)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (
                        currentEnabled &&
                        currentUnlocked &&
                        currentCanProtect &&
                        !currentPrompting
                    ) {
                        backgroundedAt = SystemClock.elapsedRealtime()
                    }
                    prompt?.cancelAuthentication()
                    prompt = null
                    prompting = false
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
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            prompt?.cancelAuthentication()
            prompt = null
        }
    }

    fun requestAuthentication() {
        if (activity == null || prompting) return
        if (!canUseBiometrics) {
            error = "No hay huella o rostro registrado. Usa tu PIN del teléfono."
            return
        }
        prompting = true
        error = null

        prompt = requestBiometricAuthentication(
            activity = activity,
            title = "Desbloquea MEDs",
            subtitle = "Usa tu huella, rostro o PIN",
            negativeText = "Usar PIN",
            onSuccess = {
                prompting = false
                prompt = null
                error = null
                unlocked = true
            },
            onFailed = {
                error = "No se reconoció. Intenta de nuevo."
            },
            onError = { code, message ->
                prompting = false
                prompt = null
                when (code) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> launchDeviceCredential()
                    BiometricPrompt.ERROR_LOCKOUT ->
                        error = "Demasiados intentos. Espera 30 segundos o usa tu PIN del teléfono."
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                        error = "La huella está bloqueada. Usa tu PIN del teléfono."
                    BiometricPrompt.ERROR_USER_CANCELED ->
                        error = "Desbloqueo cancelado."
                    BiometricPrompt.ERROR_NO_BIOMETRICS ->
                        error = "No hay huella registrada. Usa tu PIN del teléfono."
                    BiometricPrompt.ERROR_HW_NOT_PRESENT,
                    BiometricPrompt.ERROR_HW_UNAVAILABLE ->
                        error = "La biometría no está disponible. Usa tu PIN del teléfono."
                    else -> error = message
                }
            }
        )
    }

    LaunchedEffect(enabled, canUseBiometrics, canProtect, unlocked) {
        if (enabled && !unlocked && canUseBiometrics) {
            requestAuthentication()
        }
    }

    if (unlocked) {
        content()
    } else {
        BiometricLockScreen(
            error = error,
            canUseBiometrics = canUseBiometrics,
            canUsePin = canUsePin,
            isAnonymous = isAnonymous,
            onUnlockBiometric = { requestAuthentication() },
            onUsePin = { launchDeviceCredential() },
            onSignOut = onSignOut
        )
    }
}

@Composable
private fun BiometricLockScreen(
    error: String?,
    canUseBiometrics: Boolean,
    canUsePin: Boolean,
    isAnonymous: Boolean,
    onUnlockBiometric: () -> Unit,
    onUsePin: () -> Unit,
    onSignOut: (() -> Unit)?
) {
    var showSignOutDialog by remember { mutableStateOf(false) }

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
            text = when {
                canUseBiometrics && canUsePin ->
                    "Desbloquea con tu huella, rostro o el PIN de tu teléfono."
                canUseBiometrics ->
                    "Desbloquea con tu huella o rostro para continuar."
                else ->
                    "Desbloquea con el PIN de tu teléfono para continuar."
            },
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
        if (canUseBiometrics) {
            PrimaryActionButton(
                text = "Desbloquear con huella o rostro",
                onClick = onUnlockBiometric
            )
        }
        if (canUsePin) {
            if (canUseBiometrics) {
                Spacer(Modifier.height(12.dp))
                SecondaryActionButton(
                    text = "Usar PIN del teléfono",
                    onClick = onUsePin,
                    icon = Icons.Outlined.Lock
                )
            } else {
                PrimaryActionButton(
                    text = "Usar PIN del teléfono",
                    onClick = onUsePin,
                    icon = Icons.Outlined.Lock
                )
            }
        }
        if (onSignOut != null) {
            Spacer(Modifier.height(20.dp))
            TextActionButton(
                text = "Cerrar sesión",
                onClick = { showSignOutDialog = true }
            )
        }
    }

    if (showSignOutDialog && onSignOut != null) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("¿Cerrar sesión?") },
            text = {
                Text(
                    if (isAnonymous) {
                        "Esta cuenta es anónima. Si cierras sesión, perderás el acceso " +
                            "a sus datos. Vincula un correo primero si quieres conservarlos."
                    } else {
                        "Tendrás que iniciar sesión de nuevo para volver a entrar a MEDS."
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
                    Text("Cerrar sesión")
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

@Composable
fun BiometricOptInDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    var error by remember { mutableStateOf<String?>(null) }
    var prompt by remember { mutableStateOf<BiometricPrompt?>(null) }

    val pinLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            error = null
            onEnable()
        } else {
            error = "No se confirmó el PIN. Intenta de nuevo."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            prompt?.cancelAuthentication()
            prompt = null
        }
    }

    fun activateWithPin() {
        val intent = createDeviceCredentialIntent(context)
        if (intent == null) {
            error = "No hay PIN, patrón o contraseña configurados en el teléfono."
        } else {
            error = null
            pinLauncher.launch(intent)
        }
    }

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
                    text = "¿Quieres pedir tu huella, rostro o el PIN de tu teléfono " +
                        "al abrir MEDS?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Puedes cambiarlo cuando quieras en Ajustes. Si no lo activas, " +
                        "MEDS abre directo.",
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
                    if (isBiometricAvailable(context) && activity != null) {
                        prompt = requestBiometricAuthentication(
                            activity = activity,
                            title = "Activa el bloqueo",
                            subtitle = "Confirma con tu huella, rostro o PIN",
                            onSuccess = {
                                prompt = null
                                error = null
                                onEnable()
                            },
                            onFailed = {},
                            onError = { code, message ->
                                prompt = null
                                if (
                                    code == BiometricPrompt.ERROR_LOCKOUT ||
                                    code == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
                                ) {
                                    activateWithPin()
                                } else if (
                                    code != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                    code != BiometricPrompt.ERROR_USER_CANCELED
                                ) {
                                    error = message
                                }
                            }
                        )
                    } else if (isDeviceCredentialAvailable(context)) {
                        activateWithPin()
                    } else {
                        error = "Configura una huella o un PIN en el teléfono."
                    }
                }
            ) {
                Text("Activar bloqueo")
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
