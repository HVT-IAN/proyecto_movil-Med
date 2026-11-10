package com.meds.app.integration.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.doseLogId
import com.meds.app.core.common.formatTime
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)
    private val preferences = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun scheduleMedication(
        medicationId: String,
        medicationName: String,
        dose: String,
        scheduleId: String,
        times: List<String>
    ) {
        cancelForSchedule(scheduleId)
        val alarms = loadAlarms().toMutableList()
        val now = System.currentTimeMillis()

        times.distinct().forEach { time ->
            val localTime = runCatching { LocalTime.parse(time) }.getOrNull() ?: return@forEach
            val triggerAt = nextOccurrence(localTime, now)
            val dateKey = Instant.ofEpochMilli(triggerAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString()
            val alarm = ScheduledAlarm(
                requestCode = requestCode(scheduleId, time),
                medicationId = medicationId,
                scheduleId = scheduleId,
                medicationName = medicationName,
                dose = dose,
                time = time,
                triggerAt = triggerAt,
                dateKey = dateKey
            )
            alarms.add(alarm)
            schedule(alarm)
        }

        saveAlarms(alarms)
    }

    fun cancelForSchedule(scheduleId: String?) {
        if (scheduleId.isNullOrBlank()) return
        val alarms = loadAlarms()
        alarms.filter { it.scheduleId == scheduleId }.forEach { cancel(it) }
        saveAlarms(alarms.filterNot { it.scheduleId == scheduleId })
    }

    fun cancelDoseNotification(logId: String) {
        if (logId.isBlank()) return
        NotificationManagerCompat.from(appContext).cancel(logId.hashCode())
    }

    fun scheduleSnooze(dose: DoseLog, triggerAtMillis: Long) {
        val alarm = ScheduledAlarm(
            requestCode = ("snooze|${dose.id}").hashCode() and Int.MAX_VALUE,
            medicationId = dose.medicationId,
            scheduleId = dose.scheduleId,
            medicationName = dose.medicationName,
            dose = dose.dose,
            time = formatTime(triggerAtMillis),
            triggerAt = triggerAtMillis,
            dateKey = dose.dateKey
        )
        schedule(alarm)
    }

    fun rescheduleAll() {
        val now = System.currentTimeMillis()
        val alarms = loadAlarms()
        val updated = alarms.map { alarm ->
            val localTime = runCatching { LocalTime.parse(alarm.time) }.getOrNull()
            val triggerAt = if (localTime != null) {
                nextOccurrence(localTime, now, bufferMillis = 0L)
            } else {
                var candidate = alarm.triggerAt
                while (candidate <= now) {
                    candidate += DAY_MILLIS
                }
                candidate
            }
            val dateKey = Instant.ofEpochMilli(triggerAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString()
            val copy = alarm.copy(triggerAt = triggerAt, dateKey = dateKey)
            schedule(copy)
            copy
        }
        saveAlarms(updated)
    }

    private fun schedule(alarm: ScheduledAlarm) {
        val intent = Intent(appContext, MedicationAlarmReceiver::class.java).apply {
            putExtra(EXTRA_LOG_ID, doseLogId(alarm.scheduleId, alarm.dateKey, alarm.time))
            putExtra(EXTRA_REQUEST_CODE, alarm.requestCode)
            putExtra(EXTRA_MEDICATION_ID, alarm.medicationId)
            putExtra(EXTRA_SCHEDULE_ID, alarm.scheduleId)
            putExtra(EXTRA_MEDICATION_NAME, alarm.medicationName)
            putExtra(EXTRA_DOSE, alarm.dose)
            putExtra(EXTRA_TIME, alarm.time)
            putExtra(EXTRA_DATE_KEY, alarm.dateKey)
            putExtra(EXTRA_SCHEDULED_AT, alarm.triggerAt)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            if (exactAllowed) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarm.triggerAt,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarm.triggerAt,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarm.triggerAt,
                pendingIntent
            )
        }
    }

    private fun cancel(alarm: ScheduledAlarm) {
        val intent = Intent(appContext, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            alarm.requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun nextOccurrence(
        time: LocalTime,
        now: Long,
        bufferMillis: Long = 5_000L
    ): Long {
        val zone = ZoneId.systemDefault()
        var date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        var candidate = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
        if (candidate <= now + bufferMillis) {
            date = date.plusDays(1)
            candidate = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
        }
        return candidate
    }

    private fun requestCode(scheduleId: String, time: String): Int {
        return ("$scheduleId|$time").hashCode() and Int.MAX_VALUE
    }

    private fun loadAlarms(): List<ScheduledAlarm> {
        val raw = preferences.getString(KEY_ALARMS, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.toScheduledAlarm()
            }
        }.getOrDefault(emptyList())
    }

    private fun saveAlarms(alarms: List<ScheduledAlarm>) {
        val array = JSONArray()
        alarms.forEach { array.put(it.toJson()) }
        preferences.edit().putString(KEY_ALARMS, array.toString()).apply()
    }

    private data class ScheduledAlarm(
        val requestCode: Int,
        val medicationId: String,
        val scheduleId: String,
        val medicationName: String,
        val dose: String,
        val time: String,
        val triggerAt: Long,
        val dateKey: String
    ) {
        fun toJson(): JSONObject = JSONObject().apply {
            put("requestCode", requestCode)
            put("medicationId", medicationId)
            put("scheduleId", scheduleId)
            put("medicationName", medicationName)
            put("dose", dose)
            put("time", time)
            put("triggerAt", triggerAt)
            put("dateKey", dateKey)
        }
    }

    private fun JSONObject.toScheduledAlarm(): ScheduledAlarm? {
        return runCatching {
            ScheduledAlarm(
                requestCode = getInt("requestCode"),
                medicationId = getString("medicationId"),
                scheduleId = getString("scheduleId"),
                medicationName = getString("medicationName"),
                dose = getString("dose"),
                time = getString("time"),
                triggerAt = getLong("triggerAt"),
                dateKey = getString("dateKey")
            )
        }.getOrNull()
    }

    private companion object {
        const val PREFERENCES = "meds_alarms"
        const val KEY_ALARMS = "alarms"
        const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
