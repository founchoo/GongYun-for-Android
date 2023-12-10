package com.dart.campushelper.model

import com.google.gson.annotations.SerializedName

data class GradeResponse(

    @SerializedName("msg") var msg: String? = null,
    @SerializedName("ret") var ret: Int? = null,
    @SerializedName("page") var page: Int? = null,
    @SerializedName("rows") var rows: Int? = null,
    @SerializedName("total") var total: Int? = null,
    @SerializedName("totalPages") var totalPages: Int? = null,
    @SerializedName("results") var results: ArrayList<Grade> = arrayListOf()

)

data class Grade(

    @SerializedName("xnxq") var yearAndSemester: String? = null,
    @SerializedName("xf") var creditRaw: String? = null,
    @SerializedName("kcmc") var courseNameRaw: String? = null,
    @SerializedName("kcxz") var courseTypeRaw: String? = null,
    @SerializedName("zhcj") var scoreRaw: String? = null,
    @SerializedName("jd") var gradePoint: Double? = null,
    @SerializedName("kcid") var courseId: String? = null,
    @SerializedName("cjfxms") var detail: String? = null,

    ) {
    val score: Int
        get() = scoreRaw.orEmpty().toIntOrNull() ?: 0

    val credit: Double
        get() = creditRaw.orEmpty().toDoubleOrNull() ?: 0.0

    val name: String
        get() = courseNameRaw?.replace("[${courseId}]", "") ?: ""
}