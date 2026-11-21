package com.meds.app.presentation.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.meds.app.core.ui.components.InfoRow
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PrimaryActionButton
import com.meds.app.core.ui.components.TextActionButton
import com.meds.app.core.ui.theme.LemonMilk
import com.meds.app.core.ui.theme.LocalMedsColors
import com.meds.app.domain.model.UserRole
import com.meds.app.integration.notifications.AlarmScheduler

private const val DEFAULT_ONBOARDING_NAME = "Hola"

@Composable
fun OnboardingScreen(
    role: UserRole,
    onFinish: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    var step by rememberSaveable { mutableIntStateOf(1) }
    var name by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val completeWithResolvedName: () -> Unit = {
        focusManager.clearFocus()
        onFinish(name.trim().ifBlank { DEFAULT_ONBOARDING_NAME })
    }

    BackHandler(enabled = step == 2) {
        step = 1
    }

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        completeWithResolvedName()
    }

    val finishAfterPermissions: () -> Unit = {
        val intent = AlarmScheduler.exactAlarmSettingsIntent(context)
        if (intent != null) {
            val launched = runCatching { exactAlarmLauncher.launch(intent) }.isSuccess
            if (!launched) {
                completeWithResolvedName()
            }
        } else {
            completeWithResolvedName()
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            finishAfterPermissions()
        } else {
            completeWithResolvedName()
        }
    }

    val requestNotifications: () -> Unit = {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            finishAfterPermissions()
        }
    }

    val colors = LocalMedsColors.current

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
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        if (step == 2) {
                            step = 1
                        } else {
                            onBack()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = if (step == 2) "Volver" else "Volver a elegir rol",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "MEDs",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = LemonMilk,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(Modifier.weight(1f))
                TextActionButton(
                    text = "Saltar",
                    onClick = {
                        focusManager.clearFocus()
                        if (step == 1) {
                            step = 2
                        } else {
                            completeWithResolvedName()
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepBar(active = step >= 1, wide = step == 1)
                Spacer(Modifier.width(8.dp))
                StepBar(active = step >= 2, wide = step == 2)
            }

            Spacer(Modifier.height(36.dp))

            when {
                step == 1 -> {
                    Text(
                        text = "¿Cómo te llamas?",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (role == UserRole.PATIENT) {
                            "Así te saludaremos en tu espacio. Si prefieres omitirlo, usaremos «Hola»."
                        } else {
                            "Así te verá la persona que acompañas. Puedes omitirlo si prefieres."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(32.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { value ->
                            if (value.length <= 40) name = value
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tu nombre (opcional)") },
                        placeholder = { Text(DEFAULT_ONBOARDING_NAME) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                step = 2
                            }
                        )
                    )
                    Spacer(Modifier.height(28.dp))
                    PrimaryActionButton(
                        text = "Continuar",
                        onClick = {
                            focusManager.clearFocus()
                            step = 2
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    TextActionButton(
                        text = "Omitir nombre",
                        onClick = {
                            focusManager.clearFocus()
                            name = ""
                            step = 2
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    )
                }

                role == UserRole.PATIENT -> {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(colors.heroEnd, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(22.dp))
                    Text(
                        text = "Un aviso a tiempo",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "MEDs puede recordarte cada toma. Sin ruido y sin presión.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    MedsCard {
                        InfoRow(
                            icon = Icons.Outlined.Schedule,
                            text = "Tú eliges la hora de cada medicamento."
                        )
                        Spacer(Modifier.height(14.dp))
                        InfoRow(
                            icon = Icons.Outlined.CheckCircle,
                            text = "Confirmas la toma con un solo toque."
                        )
                        Spacer(Modifier.height(14.dp))
                        InfoRow(
                            icon = Icons.Outlined.Verified,
                            text = "Puedes activar o cambiar los avisos más adelante en Ajustes."
                        )
                    }
                    Spacer(Modifier.height(28.dp))
                    PrimaryActionButton(
                        text = "Activar recordatorios",
                        onClick = requestNotifications
                    )
                    Spacer(Modifier.height(8.dp))
                    TextActionButton(
                        text = "Ahora no",
                        onClick = completeWithResolvedName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
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
                    Spacer(Modifier.height(22.dp))
                    Text(
                        text = "Vincular a tu paciente",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Escanea el código que genere la persona que acompañas. Podrás ver sus tomas del día y enviarle un recordatorio.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    MedsCard {
                        InfoRow(
                            icon = Icons.Outlined.QrCode2,
                            text = "El código es temporal y de un solo uso."
                        )
                        Spacer(Modifier.height(14.dp))
                        InfoRow(
                            icon = Icons.Outlined.CheckCircle,
                            text = "Solo verás lo necesario: medicamentos y tomas del día."
                        )
                        Spacer(Modifier.height(14.dp))
                        InfoRow(
                            icon = Icons.Outlined.Verified,
                            text = "La persona que acompañas puede retirar el acceso cuando quiera."
                        )
                    }
                    Spacer(Modifier.height(28.dp))
                    PrimaryActionButton(
                        text = "Entrar al modo acompañante",
                        onClick = completeWithResolvedName
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "También podrás ingresar un código manualmente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepBar(active: Boolean, wide: Boolean) {
    Box(
        modifier = Modifier
            .height(4.dp)
            .width(if (wide) 32.dp else 12.dp)
            .background(
                color = if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
    )
}

