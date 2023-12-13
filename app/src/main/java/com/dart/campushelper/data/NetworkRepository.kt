package com.dart.campushelper.data

import androidx.compose.material3.SnackbarResult
import com.dart.campushelper.App.Companion.instance
import com.dart.campushelper.R
import com.dart.campushelper.api.NetworkService
import com.dart.campushelper.model.CalendarItem
import com.dart.campushelper.model.Classroom
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.GlobalCourse
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.PlannedCourse
import com.dart.campushelper.model.Ranking
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.model.ScheduleNoteItem
import com.dart.campushelper.model.StudentInfoResponse
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.ui.main.MainActivity
import com.dart.campushelper.viewmodel.CourseType
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

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

data class Result<T>(val data: T?, val status: Status) {
    val isDataEmpty = data is List<*> && (data as List<*>).isEmpty()

    constructor() : this(null, Status.LOADING)

    constructor(data: T?) : this(data, if (data == null) Status.ERROR else Status.SUCCESS)
}

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

    private suspend fun reLogin(): Boolean? = login(
        username = usernameStateFlow.value,
        password = passwordStateFlow.value,
    )

    private suspend fun <T> showSnackBarWithRetryButton(call: Call<T>): T? {
        val result = MainActivity.snackBarHostState.showSnackbar(
            instance.getString(R.string.network_connection_error),
            instance.getString(R.string.retry),
            true
        )
        return if (result == SnackbarResult.ActionPerformed) {
            tryRequest(call)
        } else {
            return null
        }
    }

    /**
     * This method is only for requests which are not handling the login process
     */
    private suspend fun <T> tryRequest(call: Call<T>): T? {
        // Log.d("ChaoxingRepository", "Sending request to: $reqUrl")
        try {
            val res = call.awaitResponse()
            val code = res.code()
            // Log.d("ChaoxingRepository", "Response code: $code")
            // Success to response
            if (code == 200) {
                return res.body()
                // Fail to response due to invalid cookies info, need re-login
            } else if (code == 303) {
                // Success to login
                if (reLogin() == true) {
                    // Do the original request again
                    return tryRequest(call.clone())
                    // Encounter problems during login
                } else {
                    // return showSnackBarWithRetryButton(call.clone())
                    return null
                }
                // Fail to response due to other issues.
            } else {
                // return showSnackBarWithRetryButton(call.clone())
                return null
            }
        } catch (e: Exception) {
            // Log.e("ChaoxingRepository", "Error occurred: ${e.message}")
            // return showSnackBarWithRetryButton(call.clone())
            return null
        }
    }

    suspend fun getCalendar(
        yearAndSemester: String?
    ): List<CalendarItem>? =
        tryRequest(
            networkService.getCalendar(
                yearAndSemester ?: yearAndSemesterStateFlow.value
            )
        )?.filter { it.weekNo?.toInt() != 0 }

    suspend fun getGrades(): List<Grade>? =
        tryRequest(networkService.getGrades())?.results

    suspend fun getCourseTypeList(): List<CourseType>? =
        tryRequest(networkService.getCourseTypeList())?.let {
            Jsoup.parse(it).select("select[id=kcxz]").firstOrNull()?.children()?.drop(1)?.map {
                CourseType(
                    value = it.attr("value"),
                    label = it.text(),
                    selected = true,
                )
            }
        }

    suspend fun getStudentRankingInfo(yearAndSemesters: Collection<String>): RankingInfo? {
        var rankingInfo: RankingInfo? = null
        tryRequest(
            networkService.getStudentRankingInfoRaw(
                enterUniversityYear = enterUniversityYearStateFlow.value,
                yearAndSemester = yearAndSemesters.joinToString(","),
            )
        )?.let {
            rankingInfo = RankingInfo()
            Jsoup.parse(it).run {
                select("table")[1].select("tr").forEachIndexed { hostRankingType, hostElement ->
                    hostElement.select("td")
                        .forEachIndexed { subRankingType, subElement ->
                            if (subRankingType > 0) {
                                rankingInfo!!.setRanking(
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
        }
        return rankingInfo
    }

    suspend fun getSchedule(
        yearAndSemester: String?
    ): List<Course>? = tryRequest(
        networkService.getSchedule(
            yearAndSemester = yearAndSemester ?: yearAndSemesterStateFlow.value,
            studentId = usernameStateFlow.value,
            semesterNo = ((yearAndSemester ?: yearAndSemesterStateFlow.value).lastOrNull()
                ?: "").toString(),
        )
    )?.filter {
        (it.nodeNo!! + 1) % 2 == 0
    }?.map {
        it.copy(nodeNo = (it.nodeNo!! + 1) / 2)
    }

    suspend fun getScheduleNotes(
        yearAndSemester: String?
    ): List<ScheduleNoteItem>? {
        return tryRequest(
            networkService.getScheduleNotesRaw(yearAndSemester ?: yearAndSemesterStateFlow.value)
        )?.let {
            Jsoup.parse(it).select("td[colspan=8]").first()?.html()?.trim()?.split("<br>")
                ?.dropLast(1)?.map {
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
    }

    suspend fun getGlobalSchedule(
        yearAndSemester: String,
        startWeekNo: String,
        endWeekNo: String,
        startDayOfWeek: String,
        endDayOfWeek: String,
        startNode: String,
        endNode: String,
    ): List<GlobalCourse>? =
        tryRequest(
            networkService.getGlobalSchedule(
                yearAndSemester = yearAndSemester,
                startWeekNo = startWeekNo,
                endWeekNo = endWeekNo,
                startDayOfWeek = startDayOfWeek,
                endDayOfWeek = endDayOfWeek,
                startNode = startNode,
                endNode = endNode,
            )
        )?.results

    suspend fun getPlannedSchedule(): List<PlannedCourse>? =
        tryRequest(networkService.getPlannedSchedule())?.results

    suspend fun getEmptyClassroom(
        dayOfWeekNo: List<Int>,
        nodeNo: List<Int>,
        weekNo: List<Int>,
    ): List<Classroom>? = tryRequest(
        networkService.getEmptyClassroom(
            dayOfWeekNo = dayOfWeekNo.joinToString(","),
            nodeNo = nodeNo.joinToString(","),
            weekNo = weekNo.joinToString(","),
        )
    )?.results

    suspend fun getStudentInfo(): StudentInfoResponse? =
        tryRequest(networkService.getStudentInfo())

    suspend fun login(
        username: String,
        password: String,
    ): Boolean? {
        val call = networkService.login(
            username = username,
            password = password,
        )
        val reqUrl = call.request().url.toString()
        // Log.d("ChaoxingRepository", "Sending request to: $reqUrl")
        try {
            val res = call.awaitResponse()
            // Log.d("ChaoxingRepository", "Response code: $code")
            return when (res.code()) {
                200 -> false
                302 -> true
                else -> null
            }
        } catch (e: Exception) {
            // Log.e("ChaoxingRepository", "Error occurred: ${e.message}")
            return null
        }
    }
}