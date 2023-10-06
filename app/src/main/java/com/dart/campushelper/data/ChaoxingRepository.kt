package com.dart.campushelper.data

import android.util.Log
import androidx.compose.material3.SnackbarResult
import com.dart.campushelper.api.ChaoxingService
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.GradeResponse
import com.dart.campushelper.model.StudentInfoResponse
import com.dart.campushelper.model.WeekInfoResponse
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.Constants
import com.dart.campushelper.utils.Constants.Companion.LOGIN_INFO_ERROR
import com.dart.campushelper.utils.Constants.Companion.NETWORK_CONNECT_ERROR
import com.dart.campushelper.utils.network.LoginCookieInvalidException
import com.dart.campushelper.utils.network.Resource
import com.dart.campushelper.utils.network.ResponseHandler
import com.dart.campushelper.utils.network.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import javax.inject.Inject

class ChaoxingRepository @Inject constructor(
    private val chaoxingService: ChaoxingService,
    private val userPreferenceRepository: UserPreferenceRepository,
) {

    val scope = CoroutineScope(Dispatchers.IO)

    private val semesterYearAndNoStateFlow =
        userPreferenceRepository.observeSemesterYearAndNo().stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking {
                userPreferenceRepository.observeSemesterYearAndNo().first()
            }
        )

    private val enterUniversityYearStateFlow =
        userPreferenceRepository.observeEnterUniversityYear().stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking {
                userPreferenceRepository.observeEnterUniversityYear().first()
            }
        )

    private val usernameStateFlow: StateFlow<String> = userPreferenceRepository.observeUsername()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking {
                userPreferenceRepository.observeUsername().first()
            }
        )

    private val passwordStateFlow: StateFlow<String> = userPreferenceRepository.observePassword()
        .stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                userPreferenceRepository.observePassword().first()
            }
        )

    private val responseHandler = ResponseHandler()

    private suspend fun autoReLogin(): Boolean {
        val loginResult = login(
            username = usernameStateFlow.value,
            password = passwordStateFlow.value,
        )
        Log.d("ChaoxingRepository", "autoReLogin: ${loginResult.status}")
        return when (loginResult.status) {
            Status.SUCCESS -> {
                true
            }
            Status.ERROR -> {
                false
            }
            else -> {
                false
            }
        }
    }

    private suspend fun <T> normalHandle(invokedFun: suspend () -> Resource<T>, response: Response<T>): Resource<T> {
        return try {
            if (response.code() == 303) {
                Log.d("ChaoxingRepository", "normalHandle: 303")
                // Need login again
                if (autoReLogin()) {
                    invokedFun()
                } else {
                    val result = MainActivity.snackBarHostState.showSnackbar("自动登录失败，请稍后重试",
                        Constants.RETRY
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        autoReLogin()
                    }
                    throw LoginCookieInvalidException()
                }
            } else if (response.code() == 200) {
                responseHandler.handleSuccess(response.body()!!)
            } else {
                throw Exception(NETWORK_CONNECT_ERROR)
            }
        } catch (e: Exception) {
            Log.d("ChaoxingRepository", "normalHandle: ${e.message}")
            responseHandler.handleException(e)
        }
    }

    private suspend fun <T> normalHandle(invokedFun: suspend (String) -> Resource<T>, para: String, response: Response<T>): Resource<T> {
        return try {
            if (response.code() == 303) {
                // Need login again
                if (autoReLogin()) {
                    invokedFun(para)
                } else {
                    val result = MainActivity.snackBarHostState.showSnackbar("自动登录失败，请稍后重试",
                        Constants.RETRY
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        autoReLogin()
                    }
                    throw LoginCookieInvalidException()
                }
            } else {
                responseHandler.handleSuccess(response.body()!!)
            }
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    suspend fun getGrades(): Resource<GradeResponse> {
        return normalHandle(::getGrades, chaoxingService.getGrades())
    }

    suspend fun getStudentRankingInfo(semester: String): Resource<String> {
        return normalHandle(::getStudentRankingInfo, semester, chaoxingService.getStudentRankingInfo(
            enterUniversityYear = runBlocking { enterUniversityYearStateFlow.value },
            semester = semester,
        ))
    }

    suspend fun getSchedule(): Resource<List<Course>> {
        return normalHandle(::getSchedule, chaoxingService.getSchedule(
            semesterYearAndNo = semesterYearAndNoStateFlow.value,
            studentId = usernameStateFlow.value,
            semesterNo = semesterYearAndNoStateFlow.value.last().toString(),
        ))
    }

    suspend fun getStudentInfo(): Resource<StudentInfoResponse> {
        return normalHandle(::getStudentInfo, chaoxingService.getStudentInfo())
    }

    suspend fun getWeekInfo(): Resource<WeekInfoResponse> {
        return normalHandle(::getWeekInfo, chaoxingService.getWeekInfo())
    }

    suspend fun login(
        username: String,
        password: String
    ): Resource<Response<Unit>> {
        return try {
            val response = chaoxingService.login(
                username = username,
                password = password
            )
            if (response.code() == 302) {
                responseHandler.handleSuccess(response)
            } else {
                Log.d("ChaoxingRepository", "login: 登录失败")
                Resource.error(LOGIN_INFO_ERROR, null)
            }
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }
}