package com.dart.campushelper.utils

import com.dart.campushelper.CampusHelperApplication
import com.dart.campushelper.R
import org.jsoup.Jsoup

// 解析HTML
fun parseHtml(html: String): String {
    val htmlDoc = Jsoup.parse(html)
    return htmlDoc.text()
}

fun String.replaceWithStars(isScreenshotMode: Boolean): String {
    return if (isScreenshotMode) {
        this.map { if (it.isDigit()) '*' else it }.joinToString("")
    } else {
        this
    }
}

/**
 * Convert the combination of grade and semester to text.
 * @param value The combination of grade and semester.
 * We use this formula to calculate the value:
 * (grade - 1) * 2 + semester
 * @return The readable text of the combination of grade and semester.
 */
fun combinationOfGradeAndSemesterToText(value: Float): String {
    return when (value) {
        1f -> CampusHelperApplication.context.getString(R.string.first_semester_of_freshman_year)
        2f -> CampusHelperApplication.context.getString(R.string.second_semester_of_freshman_year)
        3f -> CampusHelperApplication.context.getString(R.string.first_semester_of_sophomore_year)
        4f -> CampusHelperApplication.context.getString(R.string.second_semester_of_sophomore_year)
        5f -> CampusHelperApplication.context.getString(R.string.first_semester_of_junior_year)
        6f -> CampusHelperApplication.context.getString(R.string.second_semester_of_junior_year)
        7f -> CampusHelperApplication.context.getString(R.string.first_semester_of_senior_year)
        8f -> CampusHelperApplication.context.getString(R.string.second_semester_of_senior_year)
        else -> ""
    }
}

fun combinationOfGradeAndSemesterToText(grade: Int, semester: Int): String {
    return combinationOfGradeAndSemesterToText((grade - 1) * 2 + semester.toFloat())
}