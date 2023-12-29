package com.dart.campushelper.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.App
import com.dart.campushelper.R

class AcademicYearAndSemesterComposable {
    companion object {
        @Composable
        fun getReadableString(value: Float): String {
            val grade = ((value.toInt() - 1) / 2) + 1
            val semester = (value.toInt() - 1) % 2 + 1
            return getReadableString(grade, semester)
        }

        @Composable
        fun getReadableString(grade: Int, semester: Int): String {
            return "${
                when (grade) {
                    1 -> stringResource(R.string.freshman_year)
                    2 -> stringResource(R.string.sophomore_year)
                    3 -> stringResource(R.string.junior_year)
                    4 -> stringResource(R.string.senior_year)
                    else -> ""
                }
            } ${
                when (semester) {
                    1 -> stringResource(R.string.spring_semester)
                    2 -> stringResource(R.string.fall_semester)
                    else -> ""
                }
            }"
        }

        @Composable
        fun getReadableString(start: String, now: String): String {
            val grade = now.substring(0, 4).toInt() - start.substring(0, 4).toInt() + 1
            val semester = now.last().toString().toInt()
            return getReadableString(grade, semester)
        }
    }
}

class AcademicYearAndSemester {
    companion object {
        fun getReadableString(value: Float): String {
            val grade = ((value.toInt() - 1) / 2) + 1
            val semester = (value.toInt() - 1) % 2 + 1
            return getReadableString(grade, semester)
        }

        fun getReadableString(grade: Int, semester: Int): String {
            return "${
                when (grade) {
                    1 -> App.context.getString(R.string.freshman_year)
                    2 -> App.context.getString(R.string.sophomore_year)
                    3 -> App.context.getString(R.string.junior_year)
                    4 -> App.context.getString(R.string.senior_year)
                    else -> ""
                }
            } ${
                when (semester) {
                    1 -> App.context.getString(R.string.spring_semester)
                    2 -> App.context.getString(R.string.fall_semester)
                    else -> ""
                }
            }"
        }

        fun getReadableString(start: String, now: String): String {
            val grade = now.substring(0, 4).toInt() - start.substring(0, 4).toInt() + 1
            val semester = now.last().toString().toInt()
            return getReadableString(grade, semester)
        }
    }
}