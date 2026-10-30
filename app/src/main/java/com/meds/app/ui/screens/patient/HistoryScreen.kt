package com.meds.app.ui.screens.patient

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meds.app.ui.components.EmptyState
import com.meds.app.ui.components.LabelValue
import com.meds.app.ui.components.MedsCard
import com.meds.app.ui.components.SectionLabel
import com.meds.app.ui.screens.shared.BackHeader

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        BackHeader(title = "Historial", onBack = onBack)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Últimos 7 días",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Una mirada tranquila a tu constancia.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        MedsCard {
            SectionLabel(text = "Resumen")
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                LabelValue(
                    label = "Adherencia",
                    value = "Sin datos",
                    modifier = Modifier.weight(1f)
                )
                LabelValue(
                    label = "Tomas",
                    value = "0",
                    modifier = Modifier.weight(1f)
                )
                LabelValue(
                    label = "Racha",
                    value = "0 días",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        EmptyState(
            icon = Icons.Outlined.History,
            title = "Todavía no hay historial",
            message = "Cuando confirmes tus primeras tomas, aparecerán aquí."
        )
    }
}
