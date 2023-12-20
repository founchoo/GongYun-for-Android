package com.dart.campushelper.alarm

import android.app.AlarmManager
import android.content.Context
import com.dart.campushelper.App

open class BaseAlarmImpl : BaseAlarm {
    val alarmManager = App.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun setAlarm() {
        TODO("Not yet implemented")
    }

    override fun cancelAlarm() {
        TODO("Not yet implemented")
    }
}