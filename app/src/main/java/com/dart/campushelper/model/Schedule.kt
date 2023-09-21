package com.dart.campushelper.model

import com.google.gson.annotations.SerializedName
import org.jsoup.Jsoup
import java.util.*

data class Course(
    // ID
    @SerializedName("id")
    val id: String?,

    // 学年学期，例如：2022-2023-2
    @SerializedName("xnxq")
    val academicYearAndTerm: String?,

    // 教师ID
    @SerializedName("tid")
    val teacherId: String?,

    // 类型
    @SerializedName("type")
    val type: Int?,

    // 学生数
    @SerializedName("xs")
    val xs: Int?,

    // 日期区间
    @SerializedName("rqxl")
    val rqxl: String?,

    // 是否完成
    @SerializedName("sfwc")
    val sfwc: Int?,

    // 教学班ID
    @SerializedName("jxbid")
    val classId: String?,

    // 教学班名称
    @SerializedName("jxbmc")
    val className: String?,

    // 课程名称（包含HTML）
    @SerializedName("kcmc")
    val courseNameHtml: String?,

    // 课程编号
    @SerializedName("kcbh")
    val courseCode: String?,

    // 周次
    @SerializedName("zc")
    val zc: String?,

    // 周次序号列表（以字符串形式显示），例如：6,7,8,9,10,11,12,13
    @SerializedName("zcstr")
    val weekNoListStr: String?,

    // 教室名称（包含HTML）
    @SerializedName("croommc")
    val classroomNameHtml: String?,

    // 教师名称（包含HTML）
    @SerializedName("tmc")
    val teacherNameHtml: String?,

    // 校区ID
    @SerializedName("xqid")
    val xqid: String?,

    // 校区名称
    @SerializedName("xqmc")
    val campusName: String?,

    // 星期几
    @SerializedName("xingqi")
    val weekDayNo: Int?,

    // 节次
    @SerializedName("djc")
    val nodeNo: Int?,

    // 标志
    @SerializedName("flag")
    val flag: Int?,

    // 来源
    @SerializedName("source")
    val source: String?,

    // PKID
    @SerializedName("pkid")
    val pkid: String?,

    // 学期
    @SerializedName("xq")
    val xq: String?,

    // 班级代码
    @SerializedName("bjdm")
    val bjdm: String?,

    // 课程性质
    @SerializedName("kcxz")
    val kcxz: String?,

    // 选定性质
    @SerializedName("xdxz")
    val xdxz: String?,

    // 考试形式
    @SerializedName("ksxs")
    val ksxs: String?,

    // 学分形式
    @SerializedName("xdfs")
    val xdfs: String?,

    // 注册类型
    @SerializedName("zctype")
    val zctype: String?
) {

    constructor() : this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    ) {
    }

    // 课程名称
    val courseName: String?
        get() = courseNameHtml?.let { parseHtml(it) }

    // 周次列表
    val weekNoList: List<Int>
        get() {
            val res = ArrayList<Int>()
            val strList = weekNoListStr?.split(",")?.toTypedArray()
            if (strList != null) {
                for (item in strList) {
                    res.add(item.toInt())
                }
            }
            return res
        }

    // 教室名称
    val classroomName: String?
        get() = classroomNameHtml?.let { "@${parseHtml(it)}" }

    // 教师名称
    val teacherName: String?
        get() = teacherNameHtml?.let { parseHtml(it) }

    // 解析HTML
    private fun parseHtml(html: String): String {
        val htmlDoc = Jsoup.parse(html)
        return htmlDoc.text()
    }
}
