package com.dart.campushelper.model

data class CourseType(val value: String, val label: String, var selected: Boolean) {
    companion object {

        fun mock(): CourseType {
            return CourseType(
                value = "CT001",
                label = "CT001",
                selected = true
            )
        }
    }
}