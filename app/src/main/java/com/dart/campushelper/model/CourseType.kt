package com.dart.campushelper.model

import com.dart.campushelper.App
import com.dart.campushelper.R

data class CourseType(val value: String, val label: String, val selected: Boolean) {
    companion object {

        const val FALLBACK_VALUE = "-1"
        val FALLBACK_LABEL = App.context.getString(R.string.course_type_fallback_label)

        fun mock(): CourseType {
            return CourseType(
                value = "CT001",
                label = "CT001",
                selected = true
            )
        }
    }
}