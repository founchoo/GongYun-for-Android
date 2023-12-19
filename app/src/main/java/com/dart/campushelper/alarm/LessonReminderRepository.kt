package com.dart.campushelper.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.receiver.LessonReminderReceiver
import com.dart.campushelper.utils.DateUtils
import java.util.Calendar
import javax.inject.Inject

class LessonReminderRepository @Inject constructor() : BaseAlarmImpl() {

    // private val workManager = WorkManager.getInstance(App.instance)

    private val bigNodeStarts =
        DateUtils.smallNodeStarts.filterIndexed { index, _ -> index % 2 == 0 }
            // Push notification 30 minutes before the lesson starts
            .map { it.minusMinutes(30) }

    private val pendingIntents = List<PendingIntent>(bigNodeStarts.size) {
        PendingIntent.getBroadcast(
            context,
            LESSON_REMINDER_REQUEST_CODE_START + it,
            Intent(context, LessonReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        // const val LESSON_REMINDER_TAG = "WorkRepository"
        const val LESSON_REMINDER_REQUEST_CODE_START = 10
    }

    override fun setAlarm() {
        /*cancelLessonReminderWork()
        val lessonReminderBuilder =
            PeriodicWorkRequestBuilder<LessonReminderWorker>(15, TimeUnit.MINUTES)
        lessonReminderBuilder.addTag(LESSON_REMINDER_TAG)
        workManager.enqueue(lessonReminderBuilder.build())*/
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        bigNodeStarts.forEachIndexed { index, node ->
            calendar.set(Calendar.HOUR_OF_DAY, node.hour)
            calendar.set(Calendar.MINUTE, node.minute)
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntents[index]
            )
        }
    }

    override fun cancelAlarm() {
        /*workManager.cancelAllWorkByTag(LESSON_REMINDER_TAG)*/
        pendingIntents.forEach { alarmManager.cancel(it) }
    }
}