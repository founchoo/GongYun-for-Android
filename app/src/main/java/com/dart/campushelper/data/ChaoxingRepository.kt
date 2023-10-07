package com.dart.campushelper.data

import android.util.Log
import androidx.compose.material3.SnackbarResult
import com.dart.campushelper.api.ChaoxingService
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.GradeResponse
import com.dart.campushelper.model.LoginResponse
import com.dart.campushelper.model.StudentInfoResponse
import com.dart.campushelper.model.WeekInfoResponse
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.Constants.Companion.LOGIN_INFO_ERROR
import com.dart.campushelper.utils.Constants.Companion.NETWORK_CONNECT_ERROR
import com.dart.campushelper.utils.Constants.Companion.RETRY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.awaitResponse
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

    private suspend fun reLogin(): Boolean = login(
        username = usernameStateFlow.value,
        password = passwordStateFlow.value,
    ).isSuccess

    private suspend fun <T> retry(call: Call<T>): T? {
        val reqUrl = call.request().url.toString()
        Log.d("ChaoxingRepository", "Sending request to: $reqUrl")
        try {
            val res = call.awaitResponse()
            val code = res.code()
            Log.d("ChaoxingRepository", "Response code: $code")
            if (code == 200) {
                return res.body()
            } else if (code == 303) {
                if (reLogin()) {
                    return retry(call.clone())
                } else {
                    return null
                }
            } else {
                throw Exception(NETWORK_CONNECT_ERROR)
            }
        } catch (e: Exception) {
            Log.e("ChaoxingRepository", "Error occurred: ${e.message}")
            val result = MainActivity.snackBarHostState.showSnackbar(
                NETWORK_CONNECT_ERROR,
                RETRY
            )
            if (result == SnackbarResult.ActionPerformed) {
                return retry(call.clone())
            } else {
                return null
            }
        }
    }

    suspend fun getGrades(): GradeResponse? = retry(chaoxingService.getGrades())

    suspend fun getStudentRankingInfo(semester: String): String? = retry(
        chaoxingService.getStudentRankingInfo(
            enterUniversityYear = enterUniversityYearStateFlow.value,
            semester = semester,
        )
    )

    suspend fun getSchedule(
        semesterYearAndNo: String = semesterYearAndNoStateFlow.value
    ): List<Course>? = retry(
        chaoxingService.getSchedule(
            semesterYearAndNo = semesterYearAndNo,
            studentId = usernameStateFlow.value,
            semesterNo = semesterYearAndNo.last().toString(),
        )
    )

    suspend fun getStudentInfo(): StudentInfoResponse? = retry(chaoxingService.getStudentInfo())

    suspend fun getWeekInfo(): WeekInfoResponse? = retry(chaoxingService.getWeekInfo())

    suspend fun login(
        username: String,
        password: String,
        showErrorToast: Boolean = false,
    ): LoginResponse {
        val call = chaoxingService.login(
            username = username,
            password = password,
        )
        val reqUrl = call.request().url.toString()
        Log.d("ChaoxingRepository", "Sending request to: $reqUrl")
        try {
            val res = call.awaitResponse()
            val code = res.code()
            Log.d("ChaoxingRepository", "Response code: $code")
            if (code == 200) {
                return LoginResponse(false, LOGIN_INFO_ERROR)
            } else if (code == 302) {
                return LoginResponse(true, null)
            } else {
                throw Exception(NETWORK_CONNECT_ERROR)
            }
        } catch (e: Exception) {
            Log.e("ChaoxingRepository", "Error occurred: ${e.message}")
            if (showErrorToast) {
                val result = MainActivity.snackBarHostState.showSnackbar(
                    NETWORK_CONNECT_ERROR,
                    RETRY
                )
                if (result == SnackbarResult.ActionPerformed) {
                    return login(username, password)
                } else {
                    return LoginResponse.error()
                }
            } else {
                return LoginResponse.error()
            }
        }
    }
}