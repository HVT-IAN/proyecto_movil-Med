package com.meds.app.integration.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meds.app.domain.model.DoseLog
import com.meds.app.data.repository.PatientRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class DoseActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val logId = intent.getStringExtra(EXTRA_LOG_ID).orEmpty()
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID).orEmpty()
        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID).orEmpty()
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME).orEmpty()
        val dose = intent.getStringExtra(EXTRA_DOSE).orEmpty()
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        val dateKey = intent.getStringExtra(EXTRA_DATE_KEY).orEmpty()
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, System.currentTimeMillis())

        AlarmScheduler(context).cancelDoseNotification(logId)

        val doseLog = DoseLog(
            id = logId,
            medicationId = medicationId,
            scheduleId = scheduleId,
            medicationName = medicationName,
            dose = dose,
            scheduledAt = scheduledAt,
            status = "PENDING",
            dateKey = dateKey
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(8_000L) {
                    val repository = PatientRepositoryImpl(context)
                    when (action) {
                        ACTION_TAKE -> repository.takeDose(doseLog, source = "NOTIFICATION")
                        ACTION_SNOOZE -> {
                            val result = repository.snoozeDose(doseLog)
                            if (result.isFailure) {
                                AlarmScheduler(context).scheduleSnooze(
                                    doseLog,
                                    System.currentTimeMillis() + 10 * 60_000L
                                )
                            }
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
