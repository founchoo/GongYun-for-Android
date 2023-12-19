package com.dart.campushelper.model

import com.google.gson.annotations.SerializedName

data class CalendarItem(
    /**
     * 学年学期，例如：2020-2021-1
     */
    @SerializedName("xnxqh") var yearAndSemester: String? = null,
    /**
     * 年份月份，例如：2023-08
     */
    @SerializedName("ny") var yearAndMonth: String? = null,
    /**
     * 周次，例如：8
     */
    @SerializedName("zc") var weekNo: String? = null,
    /**
     * 星期一对应的日子，例如：21 表示星期一那天是 21 号，剩下的星期二等等以此类推
     */
    @SerializedName("monday") var monday: String? = null,
    @SerializedName("tuesday") var tuesday: String? = null,
    @SerializedName("wednesday") var wednesday: String? = null,
    @SerializedName("thursday") var thursday: String? = null,
    @SerializedName("friday") var friday: String? = null,
    @SerializedName("saturday") var saturday: String? = null,
    @SerializedName("sunday") var sunday: String? = null,
)