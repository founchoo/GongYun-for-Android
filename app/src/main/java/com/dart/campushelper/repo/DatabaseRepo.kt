package com.dart.campushelper.repo

import androidx.annotation.WorkerThread
import com.dart.campushelper.App
import com.dart.campushelper.api.SimplifiedCourseDao
import com.dart.campushelper.model.SimplifiedCourse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRepo @Inject constructor() {

    private val simplifiedCourseDao: SimplifiedCourseDao =
        AppDatabase.getDatabase(App.context).simplifiedCourseDao()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAll(): List<SimplifiedCourse> {
        return simplifiedCourseDao.getAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAll(vararg simplifiedCourses: SimplifiedCourse) {
        simplifiedCourseDao.insertAll(*simplifiedCourses)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        simplifiedCourseDao.deleteAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun findSimplifiedCourseByBigNode(bigNode: Int): SimplifiedCourse? {
        return simplifiedCourseDao.findSimplifiedCourseByBigNode(bigNode)
    }
}