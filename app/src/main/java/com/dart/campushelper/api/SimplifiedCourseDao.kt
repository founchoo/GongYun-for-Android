package com.dart.campushelper.api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dart.campushelper.model.SimplifiedCourse

@Dao
interface SimplifiedCourseDao {

    @Query("SELECT * FROM simplified_course_table")
    fun getAll(): List<SimplifiedCourse>

    @Query("SELECT * FROM simplified_course_table WHERE bigNode = :bigNode")
    fun findSimplifiedCourseByBigNode(bigNode: Int): SimplifiedCourse?

    @Insert
    fun insertAll(vararg simplifiedCourses: SimplifiedCourse)

    @Query("DELETE FROM simplified_course_table")
    fun deleteAll()
}