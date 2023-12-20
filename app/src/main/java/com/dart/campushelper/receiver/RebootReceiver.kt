package com.dart.campushelper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dart.campushelper.alarm.SyncScheduleRepository
import javax.inject.Inject

class RebootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var syncScheduleRepository: SyncScheduleRepository

    override fun onReceive(context: Context, intent: Intent) {
        // When device rebooted, set alarm again
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            syncScheduleRepository.setAlarm()
        }
    }
}