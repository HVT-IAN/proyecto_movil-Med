package com.meds.app.integration.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meds.app.MedsApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val container = (context.applicationContext as MedsApp).container
        container.alarmScheduler.rescheduleAll()
    }
}
