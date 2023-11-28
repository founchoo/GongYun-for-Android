package com.dart.campushelper.model

import com.dart.campushelper.utils.parseHtml
import com.google.gson.annotations.SerializedName

data class GlobalCourseResponse(
    @SerializedName("msg") var msg: String? = null,
    @SerializedName("ret") var ret: Int? = null,
    @SerializedName("page") var page: Int? = null,
    @SerializedName("rows") var rows: Int? = null,
    @SerializedName("total") var total: Int? = null,
    @SerializedName("totalPages") var totalPages: Int? = null,
    @SerializedName("results") var results: ArrayList<GlobalCourse> = arrayListOf()
)

data class GlobalCourse(

    @SerializedName("currentUserId") var currentUserId: String? = null,
    @SerializedName("userRoleId") var userRoleId: String? = null,
    @SerializedName("dataAuth") var dataAuth: Boolean? = null,
    @SerializedName("dataXnxq") var dataXnxq: String? = null,
    @SerializedName("currentRoleId") var currentRoleId: String? = null,
    @SerializedName("currentJsId") var currentJsId: String? = null,
    @SerializedName("currentUserName") var currentUserName: String? = null,
    @SerializedName("currentDepartmentId") var currentDepartmentId: String? = null,
    @SerializedName("tid") var tid: String? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("xnxq") var xnxq: String? = null,
    @SerializedName("xqmc") var xqmc: String? = null,
    @SerializedName("kkyxmc") var kkyxmc: String? = null,
    @SerializedName("kcxz") var kcxz: String? = null,
    @SerializedName("kcmc") var courseNameHtml: String? = null,
    @SerializedName("skjs") var teacherNameHtml: String? = null,
    @SerializedName("jxbid") var jxbid: String? = null,
    @SerializedName("jxbmc") var jxbmc: String? = null,
    @SerializedName("jxbzc") var classNameHtml: String? = null,
    @SerializedName("bjrs") var bjrs: String? = null,
    @SerializedName("sksjdd") var sksjdd: String? = null,
    @SerializedName("skdd") var classroomRaw: String? = null,
    @SerializedName("zdskrnrs") var zdskrnrs: String? = null,
    @SerializedName("ksxs") var ksxs: String? = null,
    @SerializedName("ksfs") var ksfs: String? = null,
    @SerializedName("zongxs") var zongxs: String? = null,
    @SerializedName("llxs") var llxs: String? = null,
    @SerializedName("syxs") var syxs: String? = null,
    @SerializedName("shangjxs") var shangjxs: String? = null,
    @SerializedName("shijianxs") var shijianxs: String? = null,
    @SerializedName("kkyxAuth") var kkyxAuth: Boolean? = null,
    @SerializedName("jsyxAuth") var jsyxAuth: Boolean? = null,
    @SerializedName("xsyxAuth") var xsyxAuth: Boolean? = null,
    @SerializedName("sfhyclj") var sfhyclj: Boolean? = null,
    @SerializedName("showallbj") var showallbj: Boolean? = null
) {
    val classroom: String
        get() {
            return if (classroomRaw.isNullOrEmpty()) {
                ""
            } else {
                val html = classroomRaw!!.substring(1, classroomRaw!!.length - 2)
                parseHtml(html)
            }
        }
}