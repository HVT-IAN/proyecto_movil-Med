package com.meds.app.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meds.app.data.local.OfflineMedicationStore
import com.meds.app.data.local.PreferencesStore
import com.meds.app.data.repository.AuthRepositoryImpl
import com.meds.app.data.repository.CompanionRepositoryImpl
import com.meds.app.data.repository.MedicationInfoRepositoryImpl
import com.meds.app.data.repository.PatientRepositoryImpl
import com.meds.app.data.repository.UserRepositoryImpl
import com.meds.app.domain.repository.AuthRepository
import com.meds.app.domain.repository.CompanionRepository
import com.meds.app.domain.repository.MedicationInfoRepository
import com.meds.app.domain.repository.PatientRepository
import com.meds.app.domain.repository.UserRepository
import com.meds.app.integration.notifications.AlarmScheduler

/**
 * Contenedor manual de dependencias de la aplicación.
 *
 * Se crea una sola vez en [com.meds.app.MedsApp] y expone los repositorios ya
 * construidos, de modo que los ViewModels no instancien implementaciones ni
 * conozcan Firebase, Retrofit o Android. Al vivir en la Application, las
 * dependencias sobreviven a los cambios de Activity y de configuración.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val preferencesStore: PreferencesStore = PreferencesStore(appContext)
    val offlineMedicationStore: OfflineMedicationStore =
        OfflineMedicationStore(appContext)
    val alarmScheduler: AlarmScheduler = AlarmScheduler(appContext)

    val authRepository: AuthRepository = AuthRepositoryImpl(auth)
    val userRepository: UserRepository = UserRepositoryImpl(firestore)
    val patientRepository: PatientRepository =
        PatientRepositoryImpl(
            appContext,
            firestore,
            auth,
            alarmScheduler,
            offlineMedicationStore
        )
    val companionRepository: CompanionRepository =
        CompanionRepositoryImpl(firestore, auth)
    val medicationInfoRepository: MedicationInfoRepository =
        MedicationInfoRepositoryImpl()
}
