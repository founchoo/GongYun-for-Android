package com.dart.campushelper.data

import androidx.compose.material3.SnackbarResult
import com.dart.campushelper.api.NetworkService
import com.dart.campushelper.model.CalendarItem
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.EmptyClassroomResponse
import com.dart.campushelper.model.GlobalCourseResponse
import com.dart.campushelper.model.GradeResponse
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.LoginResponse
import com.dart.campushelper.model.Ranking
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.model.ScheduleNoteItem
import com.dart.campushelper.model.StudentInfoResponse
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.Constants.Companion.LOGIN_INFO_ERROR
import com.dart.campushelper.utils.Constants.Companion.NETWORK_CONNECT_ERROR
import com.dart.campushelper.utils.Constants.Companion.RETRY
import com.example.example.PlannedScheduleResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.awaitResponse
import javax.inject.Inject

class NetworkRepository @Inject constructor(
    private val networkService: NetworkService,
    private val dataStoreRepository: DataStoreRepository,
) {

    val scope = CoroutineScope(Dispatchers.IO)

    private val yearAndSemesterStateFlow =
        dataStoreRepository.observeYearAndSemester().stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepository.observeYearAndSemester().first()
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
        yearAndSemester: String?
    ): List<CalendarItem>? = retry(
        networkService.getCalendar(yearAndSemester ?: yearAndSemesterStateFlow.value)
    )?.filter { it.weekNo?.toInt() != 0 }

    suspend fun getGrades(): GradeResponse? = retry(networkService.getGrades())

    private suspend fun getStudentRankingInfoRaw(yearAndSemester: String): String? = retry(
        networkService.getStudentRankingInfoRaw(
            enterUniversityYear = enterUniversityYearStateFlow.value,
            yearAndSemester = yearAndSemester,
        )
    )

    suspend fun getStudentRankingInfo(yearAndSemesters: Collection<String>): RankingInfo? {
        val stuRankInfoResult = getStudentRankingInfoRaw(
            yearAndSemesters.joinToString(","),
        )
        if (stuRankInfoResult != null) {
            val rankingInfo = RankingInfo()
            Jsoup.parse(stuRankInfoResult).run {
                select("table")[1].select("tr").forEachIndexed { hostRankingType, hostElement ->
                    hostElement.select("td")
                        .forEachIndexed { subRankingType, subElement ->
                            if (subRankingType > 0) {
                                rankingInfo.setRanking(
                                    HostRankingType.values()[hostRankingType - 1],
                                    SubRankingType.values()[subRankingType - 1],
                                    subElement.text().split("/").let {
                                        if (it[0] == "" || it[1] == "") {
                                            Ranking()
                                        } else {
                                            Ranking(it[0].toInt(), it[1].toInt())
                                        }
                                    }
                                )
                            }
                        }
                }
            }
            return rankingInfo
        }
        return null
    }

    suspend fun getSchedule(
        yearAndSemester: String?
    ): List<Course>? = retry(
        networkService.getSchedule(
            yearAndSemester = yearAndSemester ?: yearAndSemesterStateFlow.value,
            studentId = usernameStateFlow.value,
            semesterNo = ((yearAndSemester ?: yearAndSemesterStateFlow.value).lastOrNull()
                ?: "").toString(),
        )
    )

    private suspend fun getScheduleNotesRaw(
        yearAndSemester: String?
    ): String? = retry(
        networkService.getScheduleNotesRaw(
            yearAndSemester = yearAndSemester ?: yearAndSemesterStateFlow.value,
        )
    )

    suspend fun getScheduleNotes(
        yearAndSemester: String?
    ): List<ScheduleNoteItem>? = getScheduleNotesRaw(yearAndSemester)?.let { html ->
        Jsoup.parse(html).select("td[colspan=8]").first()?.html()?.trim()?.split("<br>")?.dropLast(1)?.map {
            val openBracketIndex = it.indexOf("【")
            val closeBracketIndex = it.indexOf("】")
            if (openBracketIndex == -1 || closeBracketIndex == -1) {
                ScheduleNoteItem(it.trim(), "")
            } else {
                ScheduleNoteItem(
                    it.substring(0, openBracketIndex).trim(),
                    it.substring(openBracketIndex + 1, closeBracketIndex).trim()
                )
            }
        }
    }

    suspend fun getGlobalSchedule(
        yearAndSemester: String,
        startWeekNo: String,
        endWeekNo: String,
        startDayOfWeek: String,
        endDayOfWeek: String,
        startNode: String,
        endNode: String,
    ): GlobalCourseResponse? =
        retry(
            networkService.getGlobalSchedule(
                yearAndSemester = yearAndSemester,
                startWeekNo = startWeekNo,
                endWeekNo = endWeekNo,
                startDayOfWeek = startDayOfWeek,
                endDayOfWeek = endDayOfWeek,
                startNode = startNode,
                endNode = endNode,
            )
        )

    suspend fun getPlannedSchedule(): PlannedScheduleResponse? = retry(networkService.getPlannedSchedule())

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