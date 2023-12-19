package com.dart.campushelper.utils

import com.dart.campushelper.App
import com.dart.campushelper.R
import com.dart.campushelper.utils.DateUtils.smallNodeEnds
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.floor

object DateUtils {
    val smallNodeEnds = listOf(
        // "09:05",
        LocalTime.of(9, 5),
        // "09:55",
        LocalTime.of(9, 55),
        // "11:00",
        LocalTime.of(11, 0),
        // "11:50",
        LocalTime.of(11, 50),
        // "14:45",
        LocalTime.of(14, 45),
        // "15:35",
        LocalTime.of(15, 35),
        // "16:40",
        LocalTime.of(16, 40),
        // "17:30",
        LocalTime.of(17, 30),
        // "19:15",
        LocalTime.of(19, 15),
        // "20:05"
        LocalTime.of(20, 5),
    )
    val smallNodeStarts = smallNodeEnds.map { it.minusMinutes(45) }
    val bigNodeStarts = smallNodeStarts.filterIndexed { index, _ -> index % 2 == 0 }
    val bigNodeEnds = smallNodeEnds.filterIndexed { index, _ -> index % 2 == 1 }
}

fun getWeekCount(startLocalDate: LocalDate?, endLocalDate: LocalDate?): Int? {
    return if (startLocalDate != null && endLocalDate != null) {
        startLocalDate.let {
            val days = endLocalDate.dayOfYear - it.dayOfYear
            floor(days / 7.0).toInt() + 1
        }
    } else {
        null
    }
}

/**
 * @return 0 if not in any small node
 */
fun getCurrentSmallNode(mockCurrentTime: LocalTime? = null): Int {
    val currentMins = (mockCurrentTime ?: LocalTime.now()).hour * 60 + LocalTime.now().minute
    smallNodeEnds.forEachIndexed { i, node ->
        if (currentMins <= node.hour * 60 + node.minute) {
            return i + 1
        }
    }
    return 0
}

fun getCurrentBigNode(mockCurrentTime: LocalTime? = null): Int {
    return (1 + getCurrentSmallNode(mockCurrentTime)) / 2
}

class DayOfWeek {
    private val map = mapOf(
        1 to App.context.getString(R.string.monday),
        2 to App.context.getString(R.string.tuesday),
        3 to App.context.getString(R.string.wednesday),
        4 to App.context.getString(R.string.thursday),
        5 to App.context.getString(R.string.friday),
        6 to App.context.getString(R.string.saturday),
        7 to App.context.getString(R.string.sunday),
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

