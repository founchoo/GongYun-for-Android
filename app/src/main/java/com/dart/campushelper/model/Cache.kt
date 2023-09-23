package com.dart.campushelper.model

data class Cache(
    var semesterYearAndNo: String = "",
    var studentId: String = "",
    var enterUniversityYear: String = "",
) {
    val semesterNo: String
        get(): String =
            if (semesterYearAndNo.isNotEmpty()) {
                semesterYearAndNo.last().toString()
            } else {
                ""
            }

    suspend fun reset() {
        semesterYearAndNo = ""
        studentId = ""
        enterUniversityYear = ""
    }
}