package com.dart.campushelper.api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dart.campushelper.model.SimplGrade

@Dao
interface SimplGradeDao {

    @Query("SELECT * FROM simpl_grade_table")
    fun getAll(): List<SimplGrade>

    @Insert
    fun insertAll(vararg simplGrades: SimplGrade)

    @Query("UPDATE simpl_grade_table SET is_read = :isRead WHERE gradeId = :gradeId")
    fun update(gradeId: String, isRead: Boolean)

    @Query("DELETE FROM simpl_grade_table")
    fun deleteAll()
}