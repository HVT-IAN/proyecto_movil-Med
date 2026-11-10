package com.meds.app.notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.meds.app.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MedsMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            runCatching { UserRepository().saveDeviceToken(uid, token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when (data["type"]) {
            NOTIFICATION_TYPE_NUDGE -> {
                NotificationHelper.showNudge(
                    context = this,
                    nudgeId = data["nudgeId"]
                        ?: message.messageId
                        ?: System.currentTimeMillis().toString(),
                    fromName = data["fromName"].orEmpty(),
                    message = message.notification?.body ?: data["message"].orEmpty()
                )
            }

            NOTIFICATION_TYPE_COMPANION_LINKED -> {
                NotificationHelper.showCompanionLinked(
                    context = this,
                    companionUid = data["companionUid"].orEmpty(),
                    companionName = data["companionName"]
                        ?: message.notification?.title.orEmpty()
                )
            }
        }
    }
}
