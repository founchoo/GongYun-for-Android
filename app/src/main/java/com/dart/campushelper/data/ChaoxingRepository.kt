package com.dart.campushelper.data

import com.dart.campushelper.api.ChaoxingService
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.GradeResponse
import com.dart.campushelper.model.StudentInfoResponse
import com.dart.campushelper.model.WeekInfoResponse
import com.dart.campushelper.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import javax.inject.Inject

class ChaoxingRepository @Inject constructor(
    private val chaoxingService: ChaoxingService,
) {

    fun getGrades(): Flow<Response<GradeResponse>> = flow {
        emit(chaoxingService.getGrades())
    }.flowOn(Dispatchers.IO)

    fun getStudentRankingInfo(semester: String): Flow<Response<String>> = flow {
        emit(
            chaoxingService.getStudentRankingInfo(
                enterUniversityYear = MainActivity.userCache.enterUniversityYear,
                semester = semester
            )
        )
    }.flowOn(Dispatchers.IO)

    fun getSchedule(): Flow<Response<List<Course>>> = flow {
        emit(
            chaoxingService.getSchedule(
                semesterYearAndNo = MainActivity.userCache.semesterYearAndNo,
                studentId = MainActivity.userCache.studentId,
                semesterNo = MainActivity.userCache.semesterNo
            )
        )
    }.flowOn(Dispatchers.IO)

    fun getStudentInfo(): Flow<Response<StudentInfoResponse>> = flow {
        emit(chaoxingService.getStudentInfo())
    }.flowOn(Dispatchers.IO)

    fun getWeekInfo(): Flow<Response<WeekInfoResponse>> = flow {
        emit(chaoxingService.getWeekInfo())
    }.flowOn(Dispatchers.IO)

    fun login(
        username: String,
        password: String
    ): Flow<Response<Unit>> = flow {
        emit(chaoxingService.login(username, password))
    }.flowOn(Dispatchers.IO)
}