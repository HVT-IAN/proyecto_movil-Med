package com.meds.app.integration.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.meds.app.MedsApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MedsMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val container get() = (applicationContext as MedsApp).container

    override fun onNewToken(token: String) {
        val uid = container.authRepository.currentUserId ?: return
        scope.launch {
            runCatching { container.userRepository.saveDeviceToken(uid, token) }
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
