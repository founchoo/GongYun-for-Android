package com.dart.campushelper.model

import com.google.gson.annotations.SerializedName

data class EmptyClassroomResponse(

    @SerializedName("msg") var msg: String? = null,
    @SerializedName("ret") var ret: Int? = null,
    @SerializedName("page") var page: Int? = null,
    @SerializedName("rows") var rows: Int? = null,
    @SerializedName("total") var total: Int? = null,
    @SerializedName("totalPages") var totalPages: Int? = null,
    @SerializedName("results") var results: ArrayList<Classroom> = arrayListOf()

)

data class Classroom(

    @SerializedName("id") var id: String? = null,
    @SerializedName("jsmc") var roomName: String? = null,
    @SerializedName("jsbh") var jsbh: String? = null,
    @SerializedName("jslx") var type: String? = null,
    @SerializedName("jxldm") var jxldm: String? = null,
    @SerializedName("jxlmc") var buildingName: String? = null,
    @SerializedName("xqmc") var xqmc: String? = null,
    @SerializedName("xqdm") var xqdm: String? = null,
    @SerializedName("zdskrnrs") var zdskrnrs: Int? = null,
    @SerializedName("szlc") var floor: String? = null,
    @SerializedName("sfqy") var sfqy: String? = null,
    @SerializedName("syyx") var syyx: String? = null,
    @SerializedName("jyzt") var jyzt: String? = null,
    @SerializedName("gcqmc") var functionalAreaName: String? = null,

) {
    companion object {
        fun mock(): Classroom {
            return Classroom(
                id = "CR001",
                roomName = "Room 101",
                jsbh = "JSBH001",
                type = "Lecture",
                jxldm = "JXLD001",
                buildingName = "Building A",
                xqmc = "Main Campus",
                xqdm = "XQDM001",
                zdskrnrs = 30,
                floor = "1",
                sfqy = "1",
                syyx = "Computer Science",
                jyzt = "Available",
                functionalAreaName = "Computer Science"
            )
        }
    }
}