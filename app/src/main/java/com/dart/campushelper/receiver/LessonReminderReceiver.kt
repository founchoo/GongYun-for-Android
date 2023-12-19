package com.dart.campushelper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dart.campushelper.App
import com.dart.campushelper.R
import com.dart.campushelper.data.NetworkRepository
import com.dart.campushelper.utils.DateUtils
import com.dart.campushelper.utils.Notification
import com.dart.campushelper.utils.getCurrentBigNode
import com.dart.campushelper.utils.getWeekCount
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class LessonReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var networkRepository: NetworkRepository

    @Inject
    lateinit var notification: Notification

    override fun onReceive(context: Context, intent: Intent) {
        val scope = CoroutineScope(Dispatchers.IO)
        val ahead = 30
        val node = getCurrentBigNode(LocalTime.now().plusMinutes(ahead.toLong()))
        val dayOfWeek = LocalDate.now().dayOfWeek
        scope.launch {
            val currentWeek = getWeekCount(
                networkRepository.getSemesterStartDate(
                    null
                ), LocalDate.now()
            )
            networkRepository.getSchedule(null)?.firstOrNull {
                it.weekDayNo == dayOfWeek.value &&
                        it.bigNodeNo == node &&
                        it.weekNoList.contains(currentWeek)
            }?.let {
                notification.show(
                    App.context.getString(R.string.lesson_reminder_title),
                    App.context.getString(
                        R.string.lesson_reminder_push_content,
                        it.courseName,
                        DateUtils.bigNodeStarts[it.bigNodeNo!! - 1].format(
                            DateTimeFormatter.ofPattern(
                                "HH:mm"
                            )
                        ),
                        it.classroomName
                    )
                )
            }
        }
    }
}
