package com.dart.campushelper.model

import com.dart.campushelper.repo.DataStoreRepo.Companion.MOCK_VALUE_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.repo.DataStoreRepo.Companion.MOCK_VALUE_YEAR_AND_SEMESTER
import com.google.gson.annotations.SerializedName


data class StudentInfoResponse(

    @SerializedName("ret") var ret: Int? = null,
    @SerializedName("msg") var msg: String? = null,
    @SerializedName("data") var data: StudentInfoData? = StudentInfoData()

)

data class StudentInfoData(

    @SerializedName("offset") var offset: Int? = null,
    @SerializedName("limit") var limit: Int? = null,
    @SerializedName("total") var total: Int? = null,
    @SerializedName("size") var size: Int? = null,
    @SerializedName("pages") var pages: Int? = null,
    @SerializedName("current") var current: Int? = null,
    @SerializedName("searchCount") var searchCount: Boolean? = null,
    @SerializedName("openSort") var openSort: Boolean? = null,
    @SerializedName("records") var records: ArrayList<Records> = arrayListOf(),
    @SerializedName("asc") var asc: Boolean? = null,
    @SerializedName("offsetCurrent") var offsetCurrent: Int? = null

)

data class Records(

    @SerializedName("currentUserId") var currentUserId: String? = null,
    @SerializedName("userRoleId") var userRoleId: String? = null,
    @SerializedName("dataAuth") var dataAuth: Boolean? = null,
    @SerializedName("dataXnxq") var dataXnxq: String? = null,
    @SerializedName("currentRoleId") var currentRoleId: String? = null,
    @SerializedName("currentJsId") var currentJsId: String? = null,
    @SerializedName("currentUserName") var currentUserName: String? = null,
    @SerializedName("currentDepartmentId") var currentDepartmentId: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("xh") var xh: String? = null,
    @SerializedName("xm") var xm: String? = null,
    @SerializedName("xmpy") var xmpy: String? = null,
    @SerializedName("xbdm") var xbdm: String? = null,
    @SerializedName("zzmmdm") var zzmmdm: String? = null,
    @SerializedName("zjlxdm") var zjlxdm: String? = null,
    @SerializedName("sfzjh") var sfzjh: String? = null,
    @SerializedName("csrq") var csrq: String? = null,
    @SerializedName("mzdm") var mzdm: String? = null,
    @SerializedName("jtzz") var jtzz: String? = null,
    @SerializedName("ksh") var ksh: String? = null,
    @SerializedName("createBy") var createBy: CreateBy? = CreateBy(),
    @SerializedName("createDate") var createDate: String? = null,
    @SerializedName("updateBy") var updateBy: UpdateBy? = UpdateBy(),
    @SerializedName("updateDate") var updateDate: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("gkzp") var gkzp: String? = null,
    @SerializedName("yxdm") var yxdm: String? = null,
    @SerializedName("zydm") var zydm: String? = null,
    @SerializedName("sznj") var sznj: String? = null,
    @SerializedName("rxnj") var rxnj: String? = null,
    @SerializedName("bjdm") var bjdm: String? = null,
    @SerializedName("xslbdm") var xslbdm: String? = null,
    @SerializedName("pyccdm") var pyccdm: String? = null,
    @SerializedName("xjzt") var xjzt: String? = null,
    @SerializedName("xsdqztdm") var xsdqztdm: String? = null,
    @SerializedName("rxnf") var rxnf: String? = null,
    @SerializedName("xz") var xz: String? = null,
    @SerializedName("sfzx") var sfzx: String? = null,
    @SerializedName("zymc") var zymc: String? = null,
    @SerializedName("bjmc") var bjmc: String? = null,
    @SerializedName("yxmc") var yxmc: String? = null,
    @SerializedName("xxzydm") var xxzydm: String? = null,
    @SerializedName("wfwtbzt") var wfwtbzt: String? = null,
    @SerializedName("new") var new: Boolean? = null

) {
    companion object {
        fun mock(): Records {
            return Records(
                dataXnxq = MOCK_VALUE_YEAR_AND_SEMESTER,
                rxnj = MOCK_VALUE_ENTER_UNIVERSITY_YEAR,
            )
        }
    }
}

data class UpdateBy(

    @SerializedName("currentUserId") var currentUserId: String? = null,
    @SerializedName("userRoleId") var userRoleId: String? = null,
    @SerializedName("dataAuth") var dataAuth: Boolean? = null,
    @SerializedName("dataXnxq") var dataXnxq: String? = null,
    @SerializedName("currentRoleId") var currentRoleId: String? = null,
    @SerializedName("currentJsId") var currentJsId: String? = null,
    @SerializedName("currentUserName") var currentUserName: String? = null,
    @SerializedName("currentDepartmentId") var currentDepartmentId: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("credentialsSalt") var credentialsSalt: String? = null,
    @SerializedName("new") var new: Boolean? = null

)

data class CreateBy(

    @SerializedName("currentUserId") var currentUserId: String? = null,
    @SerializedName("userRoleId") var userRoleId: String? = null,
    @SerializedName("dataAuth") var dataAuth: Boolean? = null,
    @SerializedName("dataXnxq") var dataXnxq: String? = null,
    @SerializedName("currentRoleId") var currentRoleId: String? = null,
    @SerializedName("currentJsId") var currentJsId: String? = null,
    @SerializedName("currentUserName") var currentUserName: String? = null,
    @SerializedName("currentDepartmentId") var currentDepartmentId: String? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("credentialsSalt") var credentialsSalt: String? = null,
    @SerializedName("new") var new: Boolean? = null

)