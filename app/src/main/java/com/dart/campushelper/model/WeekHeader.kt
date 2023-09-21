package com.dart.campushelper.model

import com.google.gson.annotations.SerializedName

data class WeekHeader(

    @SerializedName("ret") var ret: Int? = null,
    @SerializedName("msg") var msg: String? = null,
    @SerializedName("data") var data: ArrayList<WeekData> = arrayListOf()

)

data class WeekData(

    @SerializedName("date") var date: String? = null,
    @SerializedName("sfdt") var sfdt: Boolean? = null,
    @SerializedName("xq") var xq: String? = null

)