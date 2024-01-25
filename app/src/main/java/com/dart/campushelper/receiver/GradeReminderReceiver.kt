package com.dart.campushelper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dart.campushelper.App
import com.dart.campushelper.R
import com.dart.campushelper.model.SimplGrade
import com.dart.campushelper.repo.NetworkRepo
import com.dart.campushelper.repo.SimplGradeRepo
import com.dart.campushelper.utils.Notification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GradeReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var networkRepo: NetworkRepo

    @Inject
    lateinit var notification: Notification

    @Inject
    lateinit var simplGradeRepo: SimplGradeRepo

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val localGrades = simplGradeRepo.getAll()
            val remoteGrades = networkRepo.getGrades()
            val gradesToAdd = mutableListOf<SimplGrade>()
            remoteGrades?.forEach { remoteGrade ->
                if (localGrades.find { remoteGrade.courseId == it.gradeId } == null) {
                    gradesToAdd.add(SimplGrade(remoteGrade.courseId!!))
                }
            }
            if (gradesToAdd.isNotEmpty()) {
                simplGradeRepo.insertAll(*gradesToAdd.toTypedArray())
                notification.show(
                    App.context.getString(R.string.grade_reminder_title),
                    App.context.getString(R.string.grade_reminder_push_content)
                )
            }
        }
    }
}
