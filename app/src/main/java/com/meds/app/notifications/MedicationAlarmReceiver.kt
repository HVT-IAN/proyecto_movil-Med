package com.meds.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meds.app.data.model.doseLogId
import com.meds.app.data.repository.PatientRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MedicationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID).orEmpty()
        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID).orEmpty()
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME).orEmpty()
        val dose = intent.getStringExtra(EXTRA_DOSE).orEmpty()
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        val dateKey = intent.getStringExtra(EXTRA_DATE_KEY).orEmpty()
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, System.currentTimeMillis())
        val logId = intent.getStringExtra(EXTRA_LOG_ID).orEmpty()
            .ifBlank { doseLogId(scheduleId, dateKey, time) }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = PatientRepository(context)
                val alreadyTaken = withTimeoutOrNull(4_000L) {
                    repository.isDoseTaken(logId)
                } ?: false
                if (!alreadyTaken) {
                    NotificationHelper.showMedicationReminder(
                        context = context,
                        logId = logId,
                        medicationId = medicationId,
                        scheduleId = scheduleId,
                        medicationName = medicationName.ifBlank { "tu medicamento" },
                        dose = dose,
                        time = time,
                        dateKey = dateKey,
                        scheduledAt = scheduledAt
                    )
                }
                AlarmScheduler(context).rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
