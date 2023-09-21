package com.dart.campushelper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        /*scheduleViewModel.dayOfWeek.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().dayOfWeek.value
        } else {
            1
        }*/
    }
}