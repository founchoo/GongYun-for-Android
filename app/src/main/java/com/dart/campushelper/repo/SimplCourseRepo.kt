package com.dart.campushelper.repo

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dart.campushelper.App
import com.dart.campushelper.api.SimplCourseDao
import com.dart.campushelper.model.SimplCourse
import javax.inject.Inject
import javax.inject.Singleton

@Database(entities = [SimplCourse::class], version = 1, exportSchema = false)
abstract class SimplCourseDB : RoomDatabase() {
    abstract fun simplCourseDao(): SimplCourseDao

    companion object {
        @Volatile
        private var INSTANCE: SimplCourseDB? = null

        fun getDatabase(context: Context): SimplCourseDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SimplCourseDB::class.java,
                    "simpl_course_database"
                )
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Singleton
class SimplCourseRepo @Inject constructor() {

    private val simplCourseDao: SimplCourseDao =
        SimplCourseDB.getDatabase(App.context).simplCourseDao()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAll(): List<SimplCourse> {
        return simplCourseDao.getAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAll(vararg simplCourses: SimplCourse) {
        simplCourseDao.insertAll(*simplCourses)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        simplCourseDao.deleteAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun findSimplifiedCourseByBigNode(bigNode: Int): SimplCourse? {
        return simplCourseDao.findSimplifiedCourseByBigNode(bigNode)
    }
}