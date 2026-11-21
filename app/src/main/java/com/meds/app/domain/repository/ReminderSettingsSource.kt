package com.meds.app.domain.repository

import com.meds.app.domain.model.ReminderSettings

/** Lectura síncrona para el scheduler, que corre fuera de una corrutina. */
fun interface ReminderSettingsSource {
    fun current(): ReminderSettings
}
