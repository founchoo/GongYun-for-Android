package com.dart.campushelper.api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dart.campushelper.model.SimplCourse

@Dao
interface SimplCourseDao {

    @Query("SELECT * FROM simpl_course_table")
    fun getAll(): List<SimplCourse>

    @Query("SELECT * FROM simpl_course_table WHERE bigNode = :bigNode")
    fun findSimplifiedCourseByBigNode(bigNode: Int): SimplCourse?

    @Insert
    fun insertAll(vararg simpliCours: SimplCourse)

    @Query("DELETE FROM simpl_course_table")
    fun deleteAll()
}