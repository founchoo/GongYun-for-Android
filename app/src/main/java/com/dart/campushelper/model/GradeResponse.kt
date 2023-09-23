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

    @SerializedName("id") var id: String? = null,
    @SerializedName("xhid") var xhid: String? = null,
    @SerializedName("xnxq") var xnxq: String? = null,
    @SerializedName("xf") var xf: String? = null,
    @SerializedName("kcmc") var kcmc: String? = null,
    @SerializedName("kcxz") var kcxz: String? = null,
    @SerializedName("zhcj") var zhcj: String? = null,
    @SerializedName("hdxf") var hdxf: Double? = null,
    @SerializedName("xfjd") var xfjd: String? = null,
    @SerializedName("jd") var gp: Double? = null,
    @SerializedName("xdxz") var xdxz: String? = null,
    @SerializedName("ksxs") var ksxs: String? = null,
    @SerializedName("kcid") var kcid: String? = null,

    ) {
    val score: Int
        get() = zhcj.orEmpty().toIntOrNull() ?: 0

    val credit: Double
        get() = xf.orEmpty().toDoubleOrNull() ?: 0.0

    val name: String
        get() = kcmc?.replace("[${kcid}]", "") ?: ""

    val courseSort: String?
        get() = when (kcxz) {
            "50" -> "基础实践"
            "51" -> "专业实践"
            "99" -> "公共选修课"
            "32" -> "工程基础课"
            "98" -> "重修课"
            "45" -> "专业选修课"
            "71" -> "辅修双学位实践"
            "70" -> "辅修双学位理论"
            "53" -> "其他实践"
            "52" -> "综合实践"
            "44" -> "专业必修课"
            "43" -> "专业基础课"
            "42" -> "专业任选课"
            "41" -> "专业方向组选课"
            "40" -> "专业核心课"
            "31" -> "学科基础课"
            "16" -> "限定性选修课"
            "11" -> "通识教育必修课"
            "12" -> "通识教育选修课"
            else -> "未归类的课程"
        }
}