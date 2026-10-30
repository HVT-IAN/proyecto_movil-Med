package com.meds.app.ui.screens.patient

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.meds.app.ui.screens.shared.PlaceholderScreen

@Composable
fun PatientCompanionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlaceholderScreen(
        title = "Acompañantes",
        description = "Invita a una persona de confianza con un código temporal.",
        icon = Icons.Outlined.QrCode2,
        onBack = onBack,
        modifier = modifier
    )
}
