package com.dart.campushelper.repo

import com.dart.campushelper.api.NetworkService
import com.dart.campushelper.model.Classroom
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.CourseType
import com.dart.campushelper.model.GlobalCourse
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.PlannedCourse
import com.dart.campushelper.model.Ranking
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.model.Records
import com.dart.campushelper.model.ScheduleNoteItem
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.repo.DataStoreRepo.Companion.MOCK_VALUE_PASSWORD
import com.dart.campushelper.repo.DataStoreRepo.Companion.MOCK_VALUE_USERNAME
import com.dart.campushelper.repo.DataStoreRepo.Companion.MOCK_VALUE_YEAR_AND_SEMESTER
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

data class Result<T>(val data: T?, val status: Status) {
    constructor() : this(null, Status.LOADING)

    constructor(data: T?) : this(data, if (data == null) Status.ERROR else Status.SUCCESS)
}

class NetworkRepo @Inject constructor(
    private val networkService: NetworkService,
    private val dataStoreRepo: DataStoreRepo,
) {

    val scope = CoroutineScope(Dispatchers.IO)

    private val yearAndSemesterStateFlow =
        dataStoreRepo.observeYearAndSemester().stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepo.observeYearAndSemester().first()
            }
        )

    private val enterUniversityYearStateFlow =
        dataStoreRepo.observeEnterUniversityYear().stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepo.observeEnterUniversityYear().first()
            }
        )

    private val usernameStateFlow: StateFlow<String> = dataStoreRepo.observeUsername()
        .stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepo.observeUsername().first()
            }
        )

    private val passwordStateFlow: StateFlow<String> = dataStoreRepo.observePassword()
        .stateIn(
            scope = scope,
            started = Eagerly,
            initialValue = runBlocking {
                dataStoreRepo.observePassword().first()
            }
        )

    private fun isMockMode() = usernameStateFlow.value == MOCK_VALUE_USERNAME

    private suspend fun reLogin(): Boolean? = login(
        username = usernameStateFlow.value,
        password = passwordStateFlow.value,
    )

    /**
     * This method is only for requests which are not handling the login process
     */
    private suspend fun <T> tryRequest(call: Call<T>): T? {
        try {
            val res = call.awaitResponse()
            // Success to response
            when (res.code()) {
                200 -> {
                    return res.body()
                    // Fail to response due to invalid cookies info, need re-login
                }

                303 -> {
                    // Success to login
                    return if (reLogin() == true) {
                        // Do the original request again
                        tryRequest(call.clone())
                        // Encounter problems during login
                    } else {
                        // return showSnackBarWithRetryButton(call.clone())
                        null
                    }
                    // Fail to response due to other issues.
                }

                else -> {
                    return null
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun getSemesterStartDate(
        yearAndSemester: String?
    ): LocalDate? {
        return if (isMockMode()) {
            LocalDate.now().minusMonths(2)
        } else {
            tryRequest(
                networkService.getCalendar(
                    yearAndSemester ?: yearAndSemesterStateFlow.value
                )
            )?.firstOrNull { it.weekNo?.toInt() != 0 }
                ?.let { found ->
                    (found.monday ?: (found.tuesday ?: (found.wednesday
                        ?: (found.thursday ?: (found.friday ?: (found.saturday
                            ?: found.sunday))))))?.let { day ->
                        LocalDate.parse(
                            found.yearAndMonth + "-" + (if (day.toInt() < 10) "0$day" else day),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        )
                    }
                }
        }
    }

    suspend fun getGrades(): List<Grade>? {
        return if (isMockMode()) {
            return List(20) {
                Grade(
                    yearAndSemester = MOCK_VALUE_YEAR_AND_SEMESTER,
                    creditRaw = "${1.0 + it * 0.1}",
                    courseNameRaw = "[${it}]课程名称${it}",
                    courseTypeRaw = "${it / 2}",
                    scoreRaw = "${80 + it}",
                    gradePoint = 3.0 + it * 0.1,
                    courseId = "$it",
                    detail = "详细信息"
                )
            }
        } else {
            tryRequest(networkService.getGrades())?.results
        }
    }


    suspend fun getCourseTypeList(): List<CourseType>? {
        return if (isMockMode()) {
            List(10) {
                CourseType(
                    value = "$it",
                    label = "类型${it}",
                    selected = true,
                )
            }
        } else {
            tryRequest(networkService.getCourseTypeList())?.let {
                Jsoup.parse(it).select("select[id=kcxz]").firstOrNull()?.children()?.drop(1)?.map {
                    CourseType(
                        value = it.attr("value"),
                        label = it.text(),
                        selected = true,
                    )
                }
            }
        }
    }

    suspend fun getStudentRankingInfo(yearAndSemesters: Collection<String>): RankingInfo? {
        if (isMockMode()) {
            return RankingInfo.mock()
        } else {
            var rankingInfo: RankingInfo? = null
            tryRequest(
                networkService.getStudentRankingInfoRaw(
                    enterUniversityYear = enterUniversityYearStateFlow.value,
                    yearAndSemester = yearAndSemesters.joinToString(","),
                )
            )?.let {
                rankingInfo = RankingInfo()
                Jsoup.parse(it).run {
                    select("table")[1].select("tr").forEachIndexed { hostRankingTypeIndex, hostElement ->
                        hostElement.select("td")
                            .forEachIndexed { subRankingTypeIndex, subElement ->
                                val hostRankingType = HostRankingType.values()[hostRankingTypeIndex - 1]
                                rankingInfo!!.setRanking(hostRankingType, SubRankingType.REF, Ranking(1, 100000))
                                if (subRankingTypeIndex > 0) {
                                    rankingInfo!!.setRanking(
                                        hostRankingType,
                                        SubRankingType.values()[subRankingTypeIndex],
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
    }

    suspend fun getSchedule(
        yearAndSemester: String?
    ): List<Course>? {
        return (if (isMockMode()) {
            var list = emptyList<Course>()
            (1..7).forEach { dayOfWeek ->
                list = list.plus(
                    (1..10).map { node ->
                        Course.mock().copy(
                            bigNodeNo = node,
                            weekDayNo = dayOfWeek,
                        )
                    })
            }
            list
        } else {
            tryRequest(
                networkService.getSchedule(
                    yearAndSemester = yearAndSemester ?: yearAndSemesterStateFlow.value,
                    studentId = usernameStateFlow.value,
                    semesterNo = ((yearAndSemester ?: yearAndSemesterStateFlow.value).lastOrNull()
                        ?: "").toString(),
                )
            )
            // At this moment, bigNodeNo actually is small node no,
            // so we need to convert it to big node and re-assign to itself
        })?.filter {
            (it.bigNodeNo!! + 1) % 2 == 0
        }?.map {
            it.copy(bigNodeNo = (it.bigNodeNo!! + 1) / 2)
        }
    }

    suspend fun getScheduleNotes(
        yearAndSemester: String?
    ): List<ScheduleNoteItem>? {
        return if (isMockMode()) {
            List(2) {
                ScheduleNoteItem.mock()
            }
        } else {
            tryRequest(
                networkService.getScheduleNotesRaw(
                    yearAndSemester ?: yearAndSemesterStateFlow.value
                )
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
    }

    suspend fun getGlobalSchedule(
        yearAndSemester: String,
        startWeekNo: String = "",
        endWeekNo: String = "",
        startDayOfWeek: String = "",
        endDayOfWeek: String = "",
        startNode: String = "",
        endNode: String = "",
        teacherName: String = "",
    ): List<GlobalCourse>? {
        return if (isMockMode()) {
            List(20) {
                GlobalCourse.mock()
            }
        } else {
            tryRequest(
                networkService.getGlobalSchedule(
                    yearAndSemester = yearAndSemester,
                    startWeekNo = startWeekNo,
                    endWeekNo = endWeekNo,
                    startDayOfWeek = startDayOfWeek,
                    endDayOfWeek = endDayOfWeek,
                    startNode = startNode,
                    endNode = endNode,
                    teacherName = teacherName,
                )
            )?.results
        }
    }

    suspend fun getPlannedSchedule(): List<PlannedCourse>? {
        return if (isMockMode()) {
            List(20) {
                PlannedCourse.mock()
            }
        } else {
            tryRequest(networkService.getPlannedSchedule())?.results
        }
    }


    suspend fun getEmptyClassroom(
        dayOfWeekNo: List<Int>,
        nodeNo: List<Int>,
        weekNo: List<Int>,
    ): List<Classroom>? {
        return if (isMockMode()) {
            List(20) {
                Classroom.mock()
            }
        } else {
            tryRequest(
                networkService.getEmptyClassroom(
                    dayOfWeekNo = dayOfWeekNo.joinToString(","),
                    nodeNo = nodeNo.joinToString(","),
                    weekNo = weekNo.joinToString(","),
                )
            )?.results
        }
    }

    suspend fun getStudentInfo(): Records? {
        return if (isMockMode()) {
            Records.mock()
        } else {
            tryRequest(networkService.getStudentInfo())?.data?.records?.get(0)
        }
    }

    suspend fun login(
        username: String,
        password: String,
    ): Boolean? {
        if (username == MOCK_VALUE_USERNAME && password == MOCK_VALUE_PASSWORD) {
            return true
        } else {
            val call = networkService.login(
                username = username,
                password = password,
            )
            return try {
                val res = call.awaitResponse()
                when (res.code()) {
                    200 -> false
                    302 -> true
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}