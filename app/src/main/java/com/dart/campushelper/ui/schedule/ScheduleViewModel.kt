package com.dart.campushelper.ui.schedule

import androidx.compose.material3.TooltipState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.DataStoreRepository
import com.dart.campushelper.data.NetworkRepository
import com.dart.campushelper.model.Classroom
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.ScheduleNoteItem
import com.dart.campushelper.utils.DateUtils
import com.dart.campushelper.utils.getCurrentNode
import com.dart.campushelper.utils.getWeekCount
import com.example.example.PlannedCourse
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

data class ScheduleUiState constructor(
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
    val nonEmptyClassrooms: List<Course>? = null,
    val buildingNames: List<String> = emptyList(),
    val emptyClassrooms: List<Classroom>? = null,
    val holdingCourseTooltipState: TooltipState = TooltipState(isPersistent = true),
    val holdingSemesterTooltipState: TooltipState = TooltipState(isPersistent = true),
    val scheduleNotes: List<ScheduleNoteItem>? = null,
    val isShowScheduleNotesSheet: Boolean = false,
    val isScheduleNotesLoading: Boolean = true,
    val plannedSchedule: List<PlannedCourse>? = null,
    val isShowPlannedScheduleSheet: Boolean = false,
    val isPlannedScheduleLoading: Boolean = true,
    val isShowNonEmptyClassroomSheet: Boolean = false,
    val isShowEmptyClassroomSheet: Boolean = false,
    val dayOfWeekOnHoldingCourse: Int = 0,
    val nodeNoOnHoldingCourse: Int = 0,
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val dataStoreRepository: DataStoreRepository
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

    private val yearAndSemesterStateFlow =
        dataStoreRepository.observeYearAndSemester().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepository.observeYearAndSemester().first()
            }
        )

    private val enterUniversityYearStateFlow =
        dataStoreRepository.observeEnterUniversityYear().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepository.observeEnterUniversityYear().first()
            }
        )

    private val isOtherCourseDisplayStateFlow =
        dataStoreRepository.observeIsOtherCourseDisplay().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                dataStoreRepository.observeIsOtherCourseDisplay().first()
            }
        )

    private val isYearDisplayStateFlow = dataStoreRepository.observeIsYearDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepository.observeIsYearDisplay().first()
        }
    )

    private val isDateDisplayStateFlow = dataStoreRepository.observeIsDateDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepository.observeIsDateDisplay().first()
        }
    )

    private val isTimeDisplayStateFlow = dataStoreRepository.observeIsTimeDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepository.observeIsTimeDisplay().first()
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
                yearAndSemesterStateFlow,
                enterUniversityYearStateFlow
            ) { yearAndSemester, enterUniversityYear ->
                listOf(yearAndSemester, enterUniversityYear)
            }.collect {
                if (it[0].isNotEmpty() && it[1].isNotEmpty()) {
                    val semesterYearStart = it[1].toInt()
                    val semesterYearEnd = it[0].take(4).toInt()
                    val semesterNoEnd = it[0].last().toString().toInt()
                    val semesters = mutableListOf<String>()
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
    private suspend fun getStartLocalDate(yearAndSemester: String? = null) {
        val list = networkRepository.getCalendar(yearAndSemester)
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
            if (yearAndSemester == null) {
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

    suspend fun getCourses(yearAndSemester: String? = null) {
        _uiState.update { uiState ->
            uiState.copy(isTimetableLoading = true)
        }
        getStartLocalDate(yearAndSemester)
        // Log.d("ScheduleViewModel", "getCourses")
        val resource = networkRepository.getSchedule(yearAndSemester)
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
        val response = networkRepository.getGlobalSchedule(
            yearAndSemester = _uiState.value.browsedSemester,
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

    suspend fun loadEmptyClassroom(dayOfWeek: Int, node: Int) {
        _uiState.update {
            it.copy(isEmptyClrLoading = true)
        }
        val response = networkRepository.getEmptyClassroom(
            weekNo = listOf(_uiState.value.browsedWeek),
            dayOfWeekNo = listOf(dayOfWeek),
            nodeNo = listOf(node),
        )
        if (response != null) {
            _uiState.update {
                it.copy(
                    buildingNames = response.results.map { course ->
                        course.buildingName ?: ""
                    }.distinct().sorted(),
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

    fun loadScheduleNotes() {
        _uiState.update {
            it.copy(
                isScheduleNotesLoading = true,
            )
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    scheduleNotes = networkRepository.getScheduleNotes(_uiState.value.browsedSemester),
                    isScheduleNotesLoading = false
                )
            }
        }
    }

    fun setIsShowScheduleNotesSheet(value: Boolean) {
        _uiState.update {
            it.copy(isShowScheduleNotesSheet = value)
        }
    }

    fun loadPlannedSchedule() {
        _uiState.update {
            it.copy(
                isPlannedScheduleLoading = true,
                isShowPlannedScheduleSheet = true,
            )
        }
        viewModelScope.launch {
            val list = networkRepository.getPlannedSchedule()?.results
            _uiState.update {
                it.copy(
                    plannedSchedule = list,
                    isPlannedScheduleLoading = false
                )
            }
        }
    }

    fun setIsShowPlannedScheduleSheet(value: Boolean) {
        _uiState.update {
            it.copy(isShowPlannedScheduleSheet = value)
        }
    }

    fun setIsShowNonEmptyClassroomSheet(value: Boolean) {
        _uiState.update {
            it.copy(isShowNonEmptyClassroomSheet = value)
        }
    }

    fun setIsShowEmptyClassroomSheet(value: Boolean) {
        _uiState.update {
            it.copy(isShowEmptyClassroomSheet = value)
        }
    }

    fun setDayOfWeekOnHoldingCourse(value: Int) {
        _uiState.update {
            it.copy(dayOfWeekOnHoldingCourse = value)
        }
    }

    fun setNodeNoOnHoldingCourse(value: Int) {
        _uiState.update {
            it.copy(nodeNoOnHoldingCourse = value)
        }
    }
}
