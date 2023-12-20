package com.dart.campushelper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SyncScheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        AppWidgetReceiver.updateBroadcast(context)
    }
}
