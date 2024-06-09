package com.dart.campushelper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dart.campushelper.App
import com.dart.campushelper.R
import com.dart.campushelper.model.SimplCourse
import com.dart.campushelper.repo.NetworkRepo
import com.dart.campushelper.repo.SimplCourseRepo
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

    companion object {
        private const val AHEAD_MINUTES = 30L
    }

    @Inject
    lateinit var networkRepo: NetworkRepo

    @Inject
    lateinit var notification: Notification

    @Inject
    lateinit var simplCourseRepo: SimplCourseRepo

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val needUpdateDatabase =
                simplCourseRepo.getAll().isEmpty() || (LocalTime.now().hour <= 8 && LocalTime.now().minute <= 20)
            if (needUpdateDatabase) {
                val currentWeek = getWeekCount(
                    networkRepo.getSemesterStartDate(
                        null
                    ), LocalDate.now()
                )
                simplCourseRepo.deleteAll()
                networkRepo.getSchedule(null)?.filter {
                    it.weekDayNo == LocalDate.now().dayOfWeek.value &&
                            it.weekNoList.contains(currentWeek)
                }?.map {
                    SimplCourse(
                        it.bigNodeNo!!,
                        it.courseName,
                        it.classroomName,
                    )
                }?.toTypedArray()?.let {
                    if (it.isEmpty()) {
                        simplCourseRepo.insertAll(SimplCourse.placeholder())
                    } else {
                        simplCourseRepo.insertAll(*it)
                    }
                }
            }
            val node = getCurrentBigNode(LocalTime.now().plusMinutes(AHEAD_MINUTES))
            simplCourseRepo.findSimplifiedCourseByBigNode(node)?.let {
                notification.show(
                    App.context.getString(R.string.lesson_reminder_title),
                    App.context.getString(
                        R.string.lesson_reminder_push_content,
                        it.courseName,
                        DateUtils.bigNodeStarts[node - 1].format(
                            DateTimeFormatter.ofPattern(
                                "HH:mm"
                            )
                        ),
                        it.coursePlace
                    )
                )
            }
        }
    }
}
