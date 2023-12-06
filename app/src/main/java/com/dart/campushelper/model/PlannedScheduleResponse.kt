package com.dart.campushelper.model

import com.google.gson.annotations.SerializedName


data class PlannedScheduleResponse(
    @SerializedName("msg") var msg: String? = null,
    @SerializedName("ret") var ret: Int? = null,
    @SerializedName("page") var page: Int? = null,
    @SerializedName("rows") var rows: Int? = null,
    @SerializedName("total") var total: Int? = null,
    @SerializedName("totalPages") var totalPages: Int? = null,
    @SerializedName("results") var results: ArrayList<PlannedCourse> = arrayListOf()
)

data class PlannedCourse(
    @SerializedName("currentUserId") var currentUserId: String? = null,
    @SerializedName("userRoleId") var userRoleId: String? = null,
    @SerializedName("dataAuth") var dataAuth: Boolean? = null,
    @SerializedName("dataXnxq") var currentYearAndSemester: String? = null,
    @SerializedName("currentRoleId") var currentRoleId: String? = null,
    @SerializedName("currentJsId") var currentJsId: String? = null,
    @SerializedName("currentUserName") var currentUserName: String? = null,
    @SerializedName("currentDepartmentId") var currentDepartmentId: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("pyfaid") var pyfaid: String? = null,
    @SerializedName("grade") var grade: String? = null,
    @SerializedName("gradename") var gradename: String? = null,
    @SerializedName("kkxq") var semester: String? = null,
    @SerializedName("kcid") var kcid: String? = null,
    @SerializedName("kcbh") var kcbh: String? = null,
    @SerializedName("ksxs") var ksxs: String? = null,
    @SerializedName("llxs") var llxs: String? = null,
    @SerializedName("syxs") var syxs: String? = null,
    @SerializedName("shijianxs") var shijianxs: String? = null,
    @SerializedName("shangjxs") var shangjxs: String? = null,
    @SerializedName("qtxs") var qtxs: String? = null,
    @SerializedName("zhouxs") var zhouxs: String? = null,
    @SerializedName("sjzs") var sjzs: String? = null,
    @SerializedName("kkyx") var kkyx: String? = null,
    @SerializedName("kkyxmc") var hostInstituteName: String? = null,
    @SerializedName("kcxz") var kcxz: String? = null,
    @SerializedName("jxzs") var jxzs: String? = null,
    @SerializedName("kclb") var kclb: String? = null,
    @SerializedName("kcmc") var courseName: String? = null,
    @SerializedName("zongxs") var zongxs: String? = null,
    @SerializedName("xf") var xf: String? = null,
    @SerializedName("sfsjhj") var sfsjhj: String? = null,
    @SerializedName("sfsjhjmc") var sfsjhjmc: String? = null,
    @SerializedName("sfbx") var sfbx: String? = null,
    @SerializedName("jd") var jd: Int? = null,
    @SerializedName("jhcounts") var jhcounts: Int? = null,
    @SerializedName("sumflag") var sumflag: Int? = null,
    @SerializedName("kczxgzt") var kczxgzt: String? = null,
    @SerializedName("ywmc") var ywmc: String? = null,
    @SerializedName("delFlag") var delFlag: String? = null,
    @SerializedName("createDate") var createDate: String? = null,
    @SerializedName("new") var new: Boolean? = null
)