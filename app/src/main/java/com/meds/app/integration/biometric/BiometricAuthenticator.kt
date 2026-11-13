package com.meds.app.integration.biometric

import android.app.KeyguardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val ALLOWED_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_WEAK

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

/**
 * Solicita confirmación de seguridad para activar el bloqueo biométrico o por PIN,
 * gestionando automáticamente el respaldo con PIN cuando hay bloqueo por intentos.
 */
fun requestSecurityConfirmation(
    context: Context,
    activity: FragmentActivity?,
    biometricAvailable: Boolean = isBiometricAvailable(context),
    deviceCredentialAvailable: Boolean = isDeviceCredentialAvailable(context),
    onLaunchPinIntent: (Intent) -> Unit,
    onConfirmed: () -> Unit,
    onErrorMessage: (String) -> Unit
): BiometricPrompt? {
    if (biometricAvailable && activity != null) {
        return requestBiometricAuthentication(
            activity = activity,
            title = "Activa el bloqueo",
            subtitle = "Confirma con tu huella, rostro o PIN",
            onSuccess = onConfirmed,
            onFailed = {},
            onError = { code, message ->
                if (
                    code == BiometricPrompt.ERROR_LOCKOUT ||
                    code == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
                ) {
                    val intent = createDeviceCredentialIntent(context)
                    if (intent != null) {
                        onLaunchPinIntent(intent)
                    } else {
                        onErrorMessage("No hay PIN configurado. Espera 30 segundos.")
                    }
                } else if (
                    code != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                    code != BiometricPrompt.ERROR_USER_CANCELED
                ) {
                    onErrorMessage(message)
                }
            }
        )
    }

    if (deviceCredentialAvailable) {
        val intent = createDeviceCredentialIntent(context)
        if (intent != null) {
            onLaunchPinIntent(intent)
        } else {
            onErrorMessage("No se pudo abrir la pantalla del PIN.")
        }
        return null
    }

    onErrorMessage("Configura una huella o un PIN en el teléfono.")
    return null
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current != null) {
        if (current is FragmentActivity) return current
        current = (current as? ContextWrapper)?.baseContext
    }
    return null
}
