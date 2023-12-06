package com.dart.campushelper.data

import androidx.compose.material3.SnackbarResult
import com.dart.campushelper.CampusHelperApplication.Companion.context
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
import com.dart.campushelper.ui.MainActivity
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

class Result<T>(
    val data: T?,
    val isSuccess: Boolean = data != null,
)

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

    private suspend fun <T> showSnackBarWithRetryButton(call: Call<T>): Result<T> {
        val result = MainActivity.snackBarHostState.showSnackbar(
            context.getString(R.string.network_connection_error),
            context.getString(R.string.retry),
            true
        )
        return if (result == SnackbarResult.ActionPerformed) {
            tryRequest(call)
        } else {
            return Result(null, false)
        }
    }

    /**
     * This method is only for requests which are not handling the login process
     */
    private suspend fun <T> tryRequest(call: Call<T>): Result<T> {
        // Log.d("ChaoxingRepository", "Sending request to: $reqUrl")
        try {
            val res = call.awaitResponse()
            val code = res.code()
            // Log.d("ChaoxingRepository", "Response code: $code")
            // Success to response
            if (code == 200) {
                return Result(res.body())
                // Fail to response due to invalid cookies info, need re-login
            } else if (code == 303) {
                // Success to login
                if (reLogin() == true) {
                    // Do the original request again
                    return tryRequest(call.clone())
                    // Encounter problems during login
                } else {
                    // return showSnackBarWithRetryButton(call.clone())
                    return Result(null)
                }
                // Fail to response due to other issues.
            } else {
                // return showSnackBarWithRetryButton(call.clone())
                return Result(null)
            }
        } catch (e: Exception) {
            // Log.e("ChaoxingRepository", "Error occurred: ${e.message}")
            // return showSnackBarWithRetryButton(call.clone())
            return Result(null)
        }
    }

    suspend fun getCalendar(
        yearAndSemester: String?
    ): Result<List<CalendarItem>> =
        Result(
            tryRequest(
                networkService.getCalendar(
                    yearAndSemester ?: yearAndSemesterStateFlow.value
                )
            ).data?.filter { it.weekNo?.toInt() != 0 })

    suspend fun getGrades(): Result<List<Grade>> =
        tryRequest(networkService.getGrades()).data.run { Result(this?.results) }

    suspend fun getStudentRankingInfo(yearAndSemesters: Collection<String>): Result<RankingInfo> {
        var rankingInfo: RankingInfo? = null
        tryRequest(
            networkService.getStudentRankingInfoRaw(
                enterUniversityYear = enterUniversityYearStateFlow.value,
                yearAndSemester = yearAndSemesters.joinToString(","),
            )
        ).data?.let {
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
        return Result(rankingInfo)
    }

    suspend fun getSchedule(
        yearAndSemester: String?
    ): Result<List<Course>> = tryRequest(
        networkService.getSchedule(
            yearAndSemester = yearAndSemester ?: yearAndSemesterStateFlow.value,
            studentId = usernameStateFlow.value,
            semesterNo = ((yearAndSemester ?: yearAndSemesterStateFlow.value).lastOrNull()
                ?: "").toString(),
        )
    ).data?.filter {
        (it.nodeNo!! + 1) % 2 == 0
    }?.map {
        it.copy(nodeNo = (it.nodeNo!! + 1) / 2)
    }.run {
        Result(this)
    }

    suspend fun getScheduleNotes(
        yearAndSemester: String?
    ): Result<List<ScheduleNoteItem>> {
        return tryRequest(
            networkService.getScheduleNotesRaw(yearAndSemester ?: yearAndSemesterStateFlow.value)
        ).data?.let {
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
                }.run {
                    Result(this)
                }
        } ?: Result(null)
    }

    suspend fun getGlobalSchedule(
        yearAndSemester: String,
        startWeekNo: String,
        endWeekNo: String,
        startDayOfWeek: String,
        endDayOfWeek: String,
        startNode: String,
        endNode: String,
    ): Result<List<GlobalCourse>> =
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
        ).data.run { Result(this?.results) }

    suspend fun getPlannedSchedule(): Result<List<PlannedCourse>> =
        tryRequest(networkService.getPlannedSchedule()).data.run { Result(this?.results) }

    suspend fun getEmptyClassroom(
        dayOfWeekNo: List<Int>,
        nodeNo: List<Int>,
        weekNo: List<Int>,
    ): Result<List<Classroom>> = tryRequest(
        networkService.getEmptyClassroom(
            dayOfWeekNo = dayOfWeekNo.joinToString(","),
            nodeNo = nodeNo.joinToString(","),
            weekNo = weekNo.joinToString(","),
        )
    ).data.run { Result(this?.results) }

    suspend fun getStudentInfo(): Result<StudentInfoResponse> =
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