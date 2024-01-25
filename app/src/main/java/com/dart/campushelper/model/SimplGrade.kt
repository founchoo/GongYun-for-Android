package com.dart.campushelper.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simpl_grade_table")
data class SimplGrade(
    @PrimaryKey val gradeId: String,
    @ColumnInfo(name = "is_read") var isRead: Boolean = false,
)