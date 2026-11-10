package com.meds.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.meds.app.MainActivity
import com.meds.app.R

object NotificationHelper {
    const val CHANNEL_REMINDERS = "meds_reminders"
    const val CHANNEL_NUDGES = "meds_nudges"
    const val CHANNEL_NUDGES_V2 = "meds_nudges_v2"
    const val CHANNEL_COMPANIONS = "meds_companions"

    private const val NOTIFICATION_ID_NUDGE = 2001
    private const val NOTIFICATION_ID_COMPANION = 2002

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val reminders = NotificationChannel(
                CHANNEL_REMINDERS,
                "Recordatorios de medicación",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos para tomar tus medicamentos a tiempo"
                enableVibration(true)
            }

            val nudges = NotificationChannel(
                CHANNEL_NUDGES,
                "Recordatorios de acompañantes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos que envían las personas que te acompañan"
            }

            val nudgesV2 = NotificationChannel(
                CHANNEL_NUDGES_V2,
                "Recordatorios de acompañantes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios con sonido y vibración de tus acompañantes"
                enableVibration(true)
                enableLights(true)
            }

            val companions = NotificationChannel(
                CHANNEL_COMPANIONS,
                "Vinculación de acompañantes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos cuando una persona se vincula como acompañante"
                enableVibration(true)
                enableLights(true)
            }

            manager.createNotificationChannels(listOf(reminders, nudges, nudgesV2, companions))
        }
    }

    fun showMedicationReminder(
        context: Context,
        logId: String,
        medicationId: String,
        scheduleId: String,
        medicationName: String,
        dose: String,
        time: String,
        dateKey: String,
        scheduledAt: Long
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_LOG_ID, logId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            logId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val baseIntent = Intent(context, DoseActionReceiver::class.java).apply {
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
            putExtra(EXTRA_DOSE, dose)
            putExtra(EXTRA_TIME, time)
            putExtra(EXTRA_DATE_KEY, dateKey)
            putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
        }

        val takeIntent = Intent(baseIntent).setAction(ACTION_TAKE)
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            (logId.hashCode() + 1) and Int.MAX_VALUE,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(baseIntent).setAction(ACTION_SNOOZE)
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (logId.hashCode() + 2) and Int.MAX_VALUE,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Toma de $medicationName")
            .setContentText("$dose · $time")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Es hora de tu dosis de $dose de $medicationName. Puedes confirmarla o posponerla unos minutos.")
            )
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
            .addAction(0, "Ya la tomé", takePendingIntent)
            .addAction(0, "Posponer 10 min", snoozePendingIntent)
            .build()

        manager.notify(logId.hashCode(), notification)
    }

    fun showNudge(
        context: Context,
        nudgeId: String,
        fromName: String,
        message: String
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val safeName = fromName.trim().ifBlank { "Tu acompañante" }
        val safeMessage = message.trim().ifBlank {
            "Tu acompañante te envió un recordatorio."
        }
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_NUDGE)
            putExtra(EXTRA_NUDGE_ID, nudgeId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            nudgeId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NUDGES_V2)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Recordatorio de $safeName")
            .setContentText(safeMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(safeMessage))
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
            .build()

        manager.notify(NOTIFICATION_ID_NUDGE, notification)
    }

    fun showCompanionLinked(
        context: Context,
        companionUid: String,
        companionName: String
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val safeName = companionName.trim().ifBlank { "Tu acompañante" }
        val body = "$safeName ahora puede ver tus tomas del día."
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_COMPANION_LINKED)
            putExtra(EXTRA_COMPANION_UID, companionUid)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            companionUid.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_COMPANIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Nuevo acompañante")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
            .build()

        manager.notify(NOTIFICATION_ID_COMPANION, notification)
    }
}
