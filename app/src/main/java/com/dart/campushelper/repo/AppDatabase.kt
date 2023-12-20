package com.dart.campushelper.repo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dart.campushelper.api.SimplifiedCourseDao
import com.dart.campushelper.model.SimplifiedCourse

@Database(entities = [SimplifiedCourse::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun simplifiedCourseDao(): SimplifiedCourseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simplified_course_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}