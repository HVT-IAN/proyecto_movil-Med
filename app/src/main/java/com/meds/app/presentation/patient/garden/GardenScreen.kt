package com.meds.app.presentation.patient.garden

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.meds.app.core.ui.components.HeroCard
import com.meds.app.core.ui.components.LabelValue
import com.meds.app.core.ui.components.MedsCard
import com.meds.app.core.ui.components.PlantArt
import com.meds.app.domain.model.PlantState

@Composable
fun GardenScreen(plant: PlantState) {
    val stage = plant.stage
    val isMaxLevel = plant.isMaxLevel
    val stageXp = plant.stageXp
    val progress = plant.stageProgress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Jardín",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tu constancia, hoja a hoja.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        HeroCard {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                PlantArt(
                    stage = stage,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Etapa $stage de ${PlantState.MAX_LEVEL}",
                style = MaterialTheme.typography.headlineMedium,
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
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))

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

        Spacer(Modifier.height(20.dp))

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
