package com.dart.campushelper.ui.schedule

import androidx.compose.material3.TooltipState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.model.Classroom
import com.dart.campushelper.model.Course
import com.dart.campushelper.utils.DateUtils
import com.dart.campushelper.utils.getCurrentNode
import com.dart.campushelper.utils.getWeekCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ScheduleUiState(
    val courses: List<Course> = emptyList(),
    val isTimetableLoading: Boolean = true,
    val currentWeek: Int,
    val browsedWeek: Int,
    // Indicate the day of week, 1 for Monday, 7 for Sunday
    val dayOfWeek: Int,
    // Indicate the node of the current day, 1 for 8:20 - 9:05, 2 for 9:10 - 9:55, etc.
    val currentNode: Int = 1,
    val isCourseDetailDialogOpen: Boolean = false,
    val isNonEmptyClrLoading: Boolean = true,
    val isEmptyClrLoading: Boolean = true,
    val isShowWeekSliderDialog: Boolean = false,
    val nodeHeaders: IntRange = (1..10),
    val weekHeaders: List<String> = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日"),
    val contentInCourseDetailDialog: List<Course> = emptyList(),
    val nodeStartHeaders: List<String> = DateUtils.nodeEnds.map {
        LocalTime.of(
            it.split(":")[0].toInt(),
            it.split(":")[1].toInt()
        ).minusMinutes(45).format(DateTimeFormatter.ofPattern("HH:mm"))
    },
    val nodeEndHeaders: List<String> = DateUtils.nodeEnds,
    val isOtherCourseDisplay: Boolean = false,
    val isYearDisplay: Boolean = false,
    val isDateDisplay: Boolean = false,
    val isTimeDisplay: Boolean = false,
    val semesters: List<String> = emptyList(),
    val browsedSemester: String = "",
    val startLocalDate: LocalDate? = null,
    val nonEmptyClassrooms: List<Course> = emptyList(),
    val buildingNames: List<String> = emptyList(),
    val emptyClassrooms: List<Classroom> = emptyList(),
    val holdingCourseTooltipState: TooltipState = TooltipState(isPersistent = true),
    val holdingSemesterTooltipState: TooltipState = TooltipState(isPersistent = true),
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        ScheduleUiState(
            currentWeek = 1,
            browsedWeek = 1,
            dayOfWeek = LocalDate.now().dayOfWeek.value,
            currentNode = getCurrentNode(),
        )
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    /**
     * 开学日期，如：2021-09-06
     */
    private var startLocalDate: LocalDate? = null

    private val semesterYearAndNoStateFlow =
        userPreferenceRepository.observeSemesterYearAndNo().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeSemesterYearAndNo().first()
            }
        )

    private val enterUniversityYearStateFlow =
        userPreferenceRepository.observeEnterUniversityYear().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeEnterUniversityYear().first()
            }
        )

    private val isOtherCourseDisplayStateFlow =
        userPreferenceRepository.observeIsOtherCourseDisplay().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                userPreferenceRepository.observeIsOtherCourseDisplay().first()
            }
        )

    private val isYearDisplayStateFlow = userPreferenceRepository.observeIsYearDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeIsYearDisplay().first()
        }
    )

    private val isDateDisplayStateFlow = userPreferenceRepository.observeIsDateDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeIsDateDisplay().first()
        }
    )

    private val isTimeDisplayStateFlow = userPreferenceRepository.observeIsTimeDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeIsTimeDisplay().first()
        }
    )

    init {
        viewModelScope.launch {
            isOtherCourseDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isOtherCourseDisplay = value)
                }
            }
        }
        viewModelScope.launch {
            isYearDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isYearDisplay = value)
                }
            }
        }
        viewModelScope.launch {
            isDateDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isDateDisplay = value)
                }
            }
        }
        viewModelScope.launch {
            isTimeDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isTimeDisplay = value)
                }
            }
        }
        viewModelScope.launch {
            combine(
                semesterYearAndNoStateFlow,
                enterUniversityYearStateFlow
            ) { semesterYearAndNo, enterUniversityYear ->
                listOf(semesterYearAndNo, enterUniversityYear)
            }.collect {
                if (it[0].isNotEmpty() && it[1].isNotEmpty()) {
                    val semesterYearStart = it[1].toInt()
                    val semesterYearEnd = it[0].take(4).toInt()
                    val semesterNoEnd = it[0].last().toString().toInt()
                    var semesters = mutableListOf<String>()
                    (semesterYearStart..semesterYearEnd).forEach { year ->
                        (1..2).forEach { no ->
                            if (year == semesterYearEnd && no > semesterNoEnd) {
                                return@forEach
                            }
                            semesters.add("$year-${year + 1}-$no")
                        }
                    }
                    _uiState.update { uiState ->
                        uiState.copy(browsedSemester = it[0], semesters = semesters)
                    }
                    getCourses()
                }
            }
        }
    }

    /**
     * 当切换学年学期时，此方法应该被调用
     */
    private suspend fun getStartLocalDate(semesterYearAndNo: String? = null) {
        val list = chaoxingRepository.getCalendar(semesterYearAndNo)
        if (list != null) {
            val first = list[0]
            val day = first.monday ?: (first.tuesday ?: (first.wednesday
                ?: (first.thursday ?: (first.friday ?: (first.saturday ?: first.sunday ?: "")))))
            val date =
                first.yearAndMonth + "-" + (if (day.toInt() < 10) "0$day" else day)
            startLocalDate =
                LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            _uiState.update {
                it.copy(
                    startLocalDate = startLocalDate
                )
            }
            if (semesterYearAndNo == null) {
                val currentWeek = getWeekCount(startLocalDate, LocalDate.now())
                _uiState.update { uiState ->
                    uiState.copy(currentWeek = currentWeek, browsedWeek = currentWeek)
                }
            }
        }
    }

    fun setBrowsedSemester(value: String) {
        _uiState.update {
            it.copy(
                browsedSemester = value
            )
        }
    }

    fun setBrowsedWeek(value: Int) {
        _uiState.update {
            it.copy(
                browsedWeek = value
            )
        }
    }

    fun setIsCourseDetailDialogOpen(value: Boolean) {
        _uiState.update {
            it.copy(isCourseDetailDialogOpen = value)
        }
    }

    fun setIsShowWeekSliderDialog(value: Boolean) {
        _uiState.update {
            it.copy(isShowWeekSliderDialog = value)
        }
    }

    fun setContentInCourseDetailDialog(value: List<Course>) {
        _uiState.update {
            it.copy(contentInCourseDetailDialog = value)
        }
    }

    suspend fun getCourses(semesterYearAndNo: String? = null) {
        _uiState.update { uiState ->
            uiState.copy(isTimetableLoading = true)
        }
        getStartLocalDate(semesterYearAndNo)
        // Log.d("ScheduleViewModel", "getCourses")
        val resource = chaoxingRepository.getSchedule(semesterYearAndNo)
        // Log.d("ScheduleViewModel", "getCourses: resource: $resource")
        if (resource != null) {
            val courses = resource.filter {
                (it.nodeNo!! + 1) % 2 == 0
            }.map {
                it.copy(nodeNo = (it.nodeNo!! + 1) / 2)
            }
            _uiState.update { uiState ->
                uiState.copy(isTimetableLoading = false, courses = courses)
            }
        }
    }

    suspend fun getNonEmptyClassrooms(dayOfWeek: Int, node: Int) {
        _uiState.update {
            it.copy(isNonEmptyClrLoading = true)
        }
        val startNode = node * 2 - 1
        val response = chaoxingRepository.getGlobalSchedule(
            semesterYearAndNo = _uiState.value.browsedSemester,
            startWeekNo = _uiState.value.browsedWeek.toString(),
            endWeekNo = _uiState.value.browsedWeek.toString(),
            startDayOfWeek = dayOfWeek.toString(),
            endDayOfWeek = dayOfWeek.toString(),
            startNode = startNode.toString(),
            endNode = (node * 2).toString(),
        )
        if (response != null) {
            val nonEmptyClassrooms = mutableListOf<Course>()
            response.results.forEach { course ->
                val classroomList = course.classroom.split(", ")
                if (classroomList.size > 1) {
                    course.sksjdd!!.split("\n").forEach { info ->
                        val items = info.split(" ")
                        if (toMachineReadableWeekNoList(items[0]).contains(_uiState.value.browsedWeek)) {
                            if (when (items[1]) {
                                    "周一" -> 1
                                    "周二" -> 2
                                    "周三" -> 3
                                    "周四" -> 4
                                    "周五" -> 5
                                    "周六" -> 6
                                    "周日" -> 7
                                    else -> 0
                                } == dayOfWeek
                            ) {
                                if (items[2].replace("小节", "")
                                        .split("-")[0].toInt() == startNode
                                ) {
                                    nonEmptyClassrooms.add(
                                        Course().copy(
                                            courseNameHtml = course.courseNameHtml,
                                            classroomNameHtml = items[3],
                                            teacherNameHtml = course.teacherNameHtml,
                                            classNameHtml = course.classNameHtml,
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            _uiState.update {
                it.copy(
                    buildingNames = nonEmptyClassrooms.map { course ->
                        course.classroomName?.split("-")?.get(0) ?: ""
                    }.distinct().sorted(),
                    nonEmptyClassrooms = nonEmptyClassrooms.distinct()
                        .sortedBy { item -> item.classroomName },
                    isNonEmptyClrLoading = false,
                )
            }
        }
    }

    suspend fun getEmptyClassroom(dayOfWeek: Int, node: Int) {
        _uiState.update {
            it.copy(isEmptyClrLoading = true)
        }
        val response = chaoxingRepository.getEmptyClassroom(
            weekNo = listOf(_uiState.value.browsedWeek),
            dayOfWeekNo = listOf(dayOfWeek),
            nodeNo = listOf(node),
        )
        if (response != null) {
            _uiState.update {
                it.copy(
                    buildingNames = response.results.map { course ->
                        course.buildingName ?: "" }.distinct().sorted(),
                    emptyClassrooms = response.results,
                    isEmptyClrLoading = false,
                )
            }
        }
    }

    private fun toMachineReadableWeekNoList(humanReadableWeekNoList: String): List<Int> {
        val list = mutableListOf<Int>()
        humanReadableWeekNoList.replace("周", "").split(",").forEach { item ->
            if (item.contains("-")) {
                val start = item.split("-")[0].toInt()
                val end = item.split("-")[1].toInt()
                (start..end).forEach { list.add(it) }
            } else {
                list.add(item.toInt())
            }
        }
        return list
    }

    fun clearNonEmptyClassrooms() {
        _uiState.update {
            it.copy(
                nonEmptyClassrooms = emptyList()
            )
        }
    }

    fun clearEmptyClassrooms() {
        _uiState.update {
            it.copy(
                emptyClassrooms = emptyList()
            )
        }
    }
}
