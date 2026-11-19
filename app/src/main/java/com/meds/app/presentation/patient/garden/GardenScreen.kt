package com.meds.app.presentation.patient.garden

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.components.LabelValue
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PlantArt
import com.meds.app.core.ui.theme.calma.CalmaMotion
import com.meds.app.domain.model.PlantState

/**
 * Jardín.
 *
 * Pantalla secundaria: se abre desde Hoy y vuelve con la flecha. La planta
 * se mece suavemente y el progreso se anima al entrar. No hay partículas ni
 * celebraciones que interrumpan la lectura.
 */
@Composable
fun GardenScreen(
    plant: PlantState,
    onBack: () -> Unit
) {
    val stage = plant.stage
    val isMaxLevel = plant.isMaxLevel
    val stageXp = plant.stageXp

    val sway = rememberInfiniteTransition(label = "gardenSway")
    val swayAngle by sway.animateFloat(
        initialValue = -1.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = CalmaMotion.GARDEN_SWAY_MILLIS,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gardenSwayAngle"
    )
    val progress by animateFloatAsState(
        targetValue = plant.stageProgress,
        animationSpec = tween(
            durationMillis = CalmaMotion.GARDEN_PROGRESS_MILLIS,
            easing = FastOutSlowInEasing
        ),
        label = "gardenProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.size(4.dp))
            Column {
                Text(
                    text = "Jardín",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tu constancia, hoja a hoja.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        MedsCard {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                PlantArt(
                    stage = stage,
                    modifier = Modifier
                        .size(180.dp)
                        .graphicsLayer {
                            rotationZ = swayAngle
                            transformOrigin = TransformOrigin(0.5f, 0.92f)
                        }
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Etapa $stage de ${PlantState.MAX_LEVEL}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (isMaxLevel) {
                    "Tu planta llegó a su etapa más plena."
                } else {
                    "$stageXp de ${PlantState.XP_PER_LEVEL} puntos para la siguiente etapa"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        MedsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabelValue(
                    label = "Racha",
                    value = if (plant.streak == 1) "1 día" else "${plant.streak} días"
                )
                LabelValue(
                    label = "Tomas",
                    value = plant.totalTaken.toString()
                )
                LabelValue(
                    label = "Escudos",
                    value = plant.freezeAvailable.toString()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        MedsCard {
            Text(
                text = "Tu planta crecerá cuando registres tus tomas. Si olvidas un día, se queda quieta; nunca se muere.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
