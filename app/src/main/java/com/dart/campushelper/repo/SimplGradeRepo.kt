package com.dart.campushelper.repo

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dart.campushelper.App
import com.dart.campushelper.api.SimplGradeDao
import com.dart.campushelper.model.SimplGrade
import javax.inject.Inject
import javax.inject.Singleton

@Database(entities = [SimplGrade::class], version = 1, exportSchema = false)
abstract class SimplGradeDB : RoomDatabase() {
    abstract fun simplGradeDao(): SimplGradeDao

    companion object {
        @Volatile
        private var INSTANCE: SimplGradeDB? = null

        fun getDatabase(context: Context): SimplGradeDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SimplGradeDB::class.java,
                    "simpl_grade_database"
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
class SimplGradeRepo @Inject constructor() {

    private val simplGradeDao: SimplGradeDao =
        SimplGradeDB.getDatabase(App.context).simplGradeDao()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAll(): List<SimplGrade> {
        return simplGradeDao.getAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAll(vararg simplGrades: SimplGrade) {
        simplGradeDao.insertAll(*simplGrades)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(gradeId: String, isRead: Boolean) {
        simplGradeDao.update(gradeId, isRead)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        simplGradeDao.deleteAll()
    }
}