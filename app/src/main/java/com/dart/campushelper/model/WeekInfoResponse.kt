package com.dart.campushelper.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class WeekInfoResponse(

    @SerializedName("ret") var ret: Int? = null,
    @SerializedName("msg") var msg: String? = null,
    @SerializedName("data") var data: ArrayList<WeekData> = arrayListOf()

)

data class WeekData(

    @SerializedName("date") var date: String? = null,
    @SerializedName("sfdt") var sfdt: Boolean? = null,
    @SerializedName("xq") var xq: String? = null

)

fun WeekInfoResponse.getSemesterStartLocalDate(): LocalDate? {
    val startWeekMonthAndDay = this.data.first().date!!
    var month = startWeekMonthAndDay.split("-")[0]
    if (month.length == 1) {
        month = "0$month"
    }
    var day = startWeekMonthAndDay.split("-")[1]
    if (day.length == 1) {
        day = "0$day"
    }
    return LocalDate.parse("${LocalDate.now().year}-$month-$day")
}