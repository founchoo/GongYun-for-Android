package com.dart.campushelper.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simplified_course_table")
data class SimplifiedCourse(
    @PrimaryKey val bigNode: Int,
    @ColumnInfo(name = "course_name") val courseName: String?,
    @ColumnInfo(name = "course_place") val coursePlace: String?,
) {
    companion object {
        fun placeholder(): SimplifiedCourse {
            return SimplifiedCourse(
                -1,
                null,
                null,
            )
        }
    }
}