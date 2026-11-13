package com.meds.app.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.meds.app.MedsApp
import com.meds.app.presentation.companion.CompanionViewModel
import com.meds.app.presentation.patient.cima.MedicationInfoViewModel
import com.meds.app.presentation.patient.companion.PatientCompanionViewModel
import com.meds.app.presentation.patient.PatientViewModel
import com.meds.app.presentation.root.AppViewModel

/**
 * Factoría única de ViewModels.
 *
 * Obtiene el [AppContainer] desde la Application mediante `APPLICATION_KEY`,
 * que Compose y Navigation incluyen en las `CreationExtras`. Así cada
 * `viewModel()` recibe las dependencias ya construidas sin crear repositorios
 * nuevos ni duplicar el grafo de objetos.
 */
val MedsViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val container = container()
        AppViewModel(
            authRepository = container.authRepository,
            userRepository = container.userRepository,
            sessionRepository = container.sessionRepository,
            medicationCacheRepository = container.medicationCacheRepository
        )
    }
    initializer {
        val container = container()
        PatientViewModel(
            repository = container.patientRepository,
            companionRepository = container.companionRepository,
            userRepository = container.userRepository,
            authRepository = container.authRepository
        )
    }
    initializer {
        val container = container()
        PatientCompanionViewModel(
            repository = container.companionRepository,
            authRepository = container.authRepository
        )
    }
    initializer {
        val container = container()
        CompanionViewModel(
            repository = container.companionRepository,
            authRepository = container.authRepository
        )
    }
    initializer {
        val container = container()
        MedicationInfoViewModel(
            repository = container.medicationInfoRepository
        )
    }
}

private fun CreationExtras.container(): AppContainer {
    val app = this[APPLICATION_KEY] as? MedsApp
        ?: error("No se encontró MedsApp en las CreationExtras del ViewModel.")
    return app.container
}
