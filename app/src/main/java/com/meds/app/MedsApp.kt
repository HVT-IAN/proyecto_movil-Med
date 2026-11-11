package com.meds.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meds.app.di.AppContainer
import com.meds.app.integration.notifications.NotificationHelper

class MedsApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            android.util.Log.d("MedsApp", "Firebase emulator: 127.0.0.1:9099 and :8080")
            FirebaseAuth.getInstance().useEmulator("127.0.0.1", 9099)
            FirebaseFirestore.getInstance().useEmulator("127.0.0.1", 8080)
        }
        NotificationHelper.createChannels(this)
        container = AppContainer(this)
        container.alarmScheduler.rescheduleAll()
    }
}
