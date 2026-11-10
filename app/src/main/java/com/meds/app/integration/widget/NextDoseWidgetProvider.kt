package com.meds.app.integration.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.meds.app.MainActivity
import com.meds.app.R
import com.meds.app.domain.model.DoseLog
import com.meds.app.domain.model.DoseStatus
import com.meds.app.core.common.formatTime
import com.meds.app.core.common.relativeTime
import java.time.LocalDate

class NextDoseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, buildRemoteViews(context))
        }
    }

    companion object {
        private const val PREFS = "meds_widget"
        private const val KEY_TITLE = "title"
        private const val KEY_DOSE = "dose"
        private const val KEY_SCHEDULED_AT = "scheduledAt"
        private const val KEY_STATUS = "status"
        private const val KEY_DATE_KEY = "dateKey"

        fun update(context: Context, doses: List<DoseLog>) {
            val pending = doses
                .filter { it.status == DoseStatus.PENDING || it.status == DoseStatus.SNOOZED }
                .minByOrNull { it.scheduledAt }
            val today = LocalDate.now().toString()

            val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            if (pending == null) {
                editor.putString(KEY_TITLE, "Todo al día")
                editor.putString(KEY_DOSE, "")
                editor.putLong(KEY_SCHEDULED_AT, 0L)
                editor.putString(KEY_STATUS, "NONE")
                editor.putString(KEY_DATE_KEY, today)
            } else {
                editor.putString(KEY_TITLE, pending.medicationName.ifBlank { "Medicamento" })
                editor.putString(KEY_DOSE, pending.dose)
                editor.putLong(KEY_SCHEDULED_AT, pending.scheduledAt)
                editor.putString(KEY_STATUS, pending.status)
                editor.putString(KEY_DATE_KEY, pending.dateKey.ifBlank { today })
            }
            editor.apply()

            refresh(context)
        }

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, NextDoseWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { widgetId ->
                manager.updateAppWidget(widgetId, buildRemoteViews(context))
            }
        }

        private fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_next_dose)
            val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val status = preferences.getString(KEY_STATUS, null)
            val dateKey = preferences.getString(KEY_DATE_KEY, null)
            val scheduledAt = preferences.getLong(KEY_SCHEDULED_AT, 0L)
            val isToday = dateKey == LocalDate.now().toString()

            val title: String
            val detail: String
            val label: String

            when {
                status == null -> {
                    title = "MEDs"
                    detail = "Abre la app para ver tu próxima toma."
                    label = "Próxima toma"
                }

                status == "NONE" || !isToday -> {
                    title = "Todo al día"
                    detail = if (isToday) {
                        "No tienes tomas pendientes por ahora."
                    } else {
                        "Abre MEDs para actualizar tus tomas de hoy."
                    }
                    label = "MEDs"
                }

                else -> {
                    title = preferences.getString(KEY_TITLE, null)
                        ?.ifBlank { "Medicamento" }
                        ?: "Medicamento"
                    val dose = preferences.getString(KEY_DOSE, "").orEmpty()
                    detail = buildString {
                        if (dose.isNotBlank()) {
                            append(dose)
                            append(" · ")
                        }
                        append(formatTime(scheduledAt))
                        val relative = relativeTime(scheduledAt)
                        if (relative.isNotBlank()) {
                            append(" · ")
                            append(relative)
                        }
                    }
                    label = if (status == DoseStatus.SNOOZED) "Pospuesta" else "Próxima toma"
                }
            }

            views.setTextViewText(R.id.widget_status, label.uppercase())
            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_detail, detail)

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            return views
        }
    }
}
