package com.dart.campushelper.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.receiver.SyncScheduleReceiver
import java.util.Calendar
import javax.inject.Inject

class SyncScheduleRepository @Inject constructor() : BaseAlarmImpl() {

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        SYNC_SCHEDULE_REQUEST_CODE,
        Intent(context, SyncScheduleReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val SYNC_SCHEDULE_REQUEST_CODE = 20
    }

    override fun setAlarm() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    override fun cancelAlarm() {
        alarmManager.cancel(pendingIntent)
    }
}