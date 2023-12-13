package com.dart.campushelper.utils

import com.dart.campushelper.App
import com.dart.campushelper.R
import com.dart.campushelper.utils.DateUtils.nodeEnds
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.floor

object DateUtils {
    val nodeEnds = listOf(
        "09:05",
        "09:55",
        "11:00",
        "11:50",
        "14:45",
        "15:35",
        "16:40",
        "17:30",
        "19:15",
        "20:05"
    )
}

fun getWeekCount(startLocalDate: LocalDate?, endLocalDate: LocalDate?): Int {
    return if (startLocalDate != null && endLocalDate != null) {
        startLocalDate.let {
            val days = endLocalDate.dayOfYear - it.dayOfYear
            floor(days / 7.0).toInt() + 1
        }
    } else {
        1
    }
}

fun getCurrentNode(): Int {
    val currentMins = LocalTime.now().hour * 60 + LocalTime.now().minute
    nodeEnds.forEachIndexed { i, node ->
        val nodeEndMins = node.split(":")[0].toInt() * 60 + node.split(":")[1].toInt()
        if (currentMins <= nodeEndMins) {
            return i + 1
        }
    }
    return 0
}

class DayOfWeek {
    private val map = mapOf(
        1 to App.instance.getString(R.string.monday),
        2 to App.instance.getString(R.string.tuesday),
        3 to App.instance.getString(R.string.wednesday),
        4 to App.instance.getString(R.string.thursday),
        5 to App.instance.getString(R.string.friday),
        6 to App.instance.getString(R.string.saturday),
        7 to App.instance.getString(R.string.sunday),
    )

    fun convertDayOfWeekToText(dayOfWeek: Int): String = map[dayOfWeek] ?: ""

    fun convertChineseToDayOfWeek(chinese: String): Int {
        map.forEach { (key, value) ->
            if (value == chinese) {
                return key
            }
        }
        return 0
    }

    companion object {
        val instance = DayOfWeek()
    }
}

