package com.dart.campushelper.data

import androidx.compose.material3.SnackbarResult
import com.dart.campushelper.api.NetworkService
import com.dart.campushelper.model.CalendarItem
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.EmptyClassroomResponse
import com.dart.campushelper.model.GlobalCourseResponse
import com.dart.campushelper.model.GradeResponse
import com.dart.campushelper.model.LoginResponse
import com.dart.campushelper.model.StudentInfoResponse
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.Constants.Companion.LOGIN_INFO_ERROR
import com.dart.campushelper.utils.Constants.Companion.NETWORK_CONNECT_ERROR
import com.dart.campushelper.utils.Constants.Companion.RETRY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.awaitResponse
import javax.inject.Inject

class NetworkRepository @Inject constructor(
    private val networkService: NetworkService,
    private val dataStoreRepository: DataStoreRepository,
) {

    val scope = CoroutineScope(Dispatchers.IO)

    private val semesterYearAndNoStateFlow =
        dataStoreRepository.observeSemesterYearAndNo().stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepository.observeSemesterYearAndNo().first()
            }
        )

    private val enterUniversityYearStateFlow =
        dataStoreRepository.observeEnterUniversityYear().stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepository.observeEnterUniversityYear().first()
            }
        )

    private val usernameStateFlow: StateFlow<String> = dataStoreRepository.observeUsername()
        .stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepository.observeUsername().first()
            }
        )

    private val passwordStateFlow: StateFlow<String> = dataStoreRepository.observePassword()
        .stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepository.observePassword().first()
            }
        )

    private suspend fun reLogin(): Boolean = login(
        username = usernameStateFlow.value,
        password = passwordStateFlow.value,
    ).isSuccess

    private suspend fun <T> retry(call: Call<T>): T? {
        val reqUrl = call.request().url.toString()
        // Log.d("ChaoxingRepository", "Sending request to: $reqUrl")
        try {
            val res = call.awaitResponse()
            val code = res.code()
            // Log.d("ChaoxingRepository", "Response code: $code")
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
            // Log.e("ChaoxingRepository", "Error occurred: ${e.message}")
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

    suspend fun getCalendar(
        semesterYearAndNo: String?
    ): List<CalendarItem>? = retry(
        networkService.getCalendar(semesterYearAndNo ?: semesterYearAndNoStateFlow.value)
    )?.filter { it.weekNo?.toInt() != 0 }

    suspend fun getGrades(): GradeResponse? = retry(networkService.getGrades())

    suspend fun getStudentRankingInfo(semester: String): String? = retry(
        networkService.getStudentRankingInfo(
            enterUniversityYear = enterUniversityYearStateFlow.value,
            semester = semester,
        )
    )

    suspend fun getSchedule(
        semesterYearAndNo: String?
    ): List<Course>? = retry(
        networkService.getSchedule(
            semesterYearAndNo = semesterYearAndNo ?: semesterYearAndNoStateFlow.value,
            studentId = usernameStateFlow.value,
            semesterNo = ((semesterYearAndNo ?: semesterYearAndNoStateFlow.value).lastOrNull()
                ?: "").toString(),
        )
    )

    suspend fun getGlobalSchedule(
        semesterYearAndNo: String,
        startWeekNo: String,
        endWeekNo: String,
        startDayOfWeek: String,
        endDayOfWeek: String,
        startNode: String,
        endNode: String,
    ): GlobalCourseResponse? =
        retry(
            networkService.getGlobalSchedule(
                semesterYearAndNo = semesterYearAndNo,
                startWeekNo = startWeekNo,
                endWeekNo = endWeekNo,
                startDayOfWeek = startDayOfWeek,
                endDayOfWeek = endDayOfWeek,
                startNode = startNode,
                endNode = endNode,
            )
        )

    suspend fun getEmptyClassroom(
        dayOfWeekNo: List<Int>,
        nodeNo: List<Int>,
        weekNo: List<Int>,
    ): EmptyClassroomResponse? = retry(
        networkService.getEmptyClassroom(
            dayOfWeekNo = dayOfWeekNo.joinToString(","),
            nodeNo = nodeNo.joinToString(","),
            weekNo = weekNo.joinToString(","),
        )
    )

    suspend fun getStudentInfo(): StudentInfoResponse? = retry(networkService.getStudentInfo())

    suspend fun login(
        username: String,
        password: String,
        showErrorToast: Boolean = false,
    ): LoginResponse {
        val call = networkService.login(
            username = username,
            password = password,
        )
        val reqUrl = call.request().url.toString()
        // Log.d("ChaoxingRepository", "Sending request to: $reqUrl")
        try {
            val res = call.awaitResponse()
            val code = res.code()
            // Log.d("ChaoxingRepository", "Response code: $code")
            if (code == 200) {
                return LoginResponse(false, LOGIN_INFO_ERROR)
            } else if (code == 302) {
                return LoginResponse(true, null)
            } else {
                throw Exception(NETWORK_CONNECT_ERROR)
            }
        } catch (e: Exception) {
            // Log.e("ChaoxingRepository", "Error occurred: ${e.message}")
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