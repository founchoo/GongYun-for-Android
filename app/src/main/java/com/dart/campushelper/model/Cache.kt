package com.dart.campushelper.model

import kotlinx.coroutines.flow.MutableStateFlow

data class Cache(
    var semesterYearAndNo: String = "",
    var studentId: String = "",
    var enterUniversityYear: String = "",
    var isLogin: MutableStateFlow<Boolean> = MutableStateFlow(false),
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