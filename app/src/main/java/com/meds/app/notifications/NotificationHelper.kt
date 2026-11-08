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

            manager.createNotificationChannels(listOf(reminders, nudges))
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
}
