package com.dart.campushelper.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.receiver.GradeReminderReceiver
import java.util.Calendar
import javax.inject.Inject

class GradeReminderRepository @Inject constructor() : BaseAlarmImpl() {

    private val pendingIntent = PendingIntent.getBroadcast(
        context,
        GRADE_REMINDER_REQUEST_CODE_START,
        Intent(context, GradeReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val GRADE_REMINDER_REQUEST_CODE_START = 30
    }

    override fun setAlarm() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 20)
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