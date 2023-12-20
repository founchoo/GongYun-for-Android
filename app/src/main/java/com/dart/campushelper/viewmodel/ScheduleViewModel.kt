package com.dart.campushelper.viewmodel

import androidx.compose.material3.TooltipState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.model.Classroom
import com.dart.campushelper.model.Course
import com.dart.campushelper.model.GlobalCourse
import com.dart.campushelper.model.PlannedCourse
import com.dart.campushelper.model.ScheduleNoteItem
import com.dart.campushelper.repo.DataStoreRepo
import com.dart.campushelper.repo.NetworkRepo
import com.dart.campushelper.repo.Result
import com.dart.campushelper.utils.DateUtils
import com.dart.campushelper.utils.getCurrentSmallNode
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
import javax.inject.Inject

data class ScheduleUiState constructor(
    val courses: Result<List<Course>> = Result(),
    val currentWeek: Int? = null,
    val browsedWeek: Int? = null,
    // Indicate the day of week, 1 for Monday, 7 for Sunday
    val dayOfWeek: Int,
    // Indicate the node of the current day, 1 for 8:20 - 9:05, 2 for 9:10 - 9:55, etc.
    val currentNode: Int = 1,
    val isCourseDetailDialogOpen: Boolean = false,
    val isShowWeekSliderDialog: Boolean = false,
    val nodeHeaders: IntRange = (1..5),
    val contentInCourseDetailDialog: List<Course> = emptyList(),
    val nodeStartHeaders: List<LocalTime> = DateUtils.smallNodeStarts,
    val nodeEndHeaders: List<LocalTime> = DateUtils.smallNodeEnds,
    val isOtherCourseDisplay: Boolean = false,
    val isYearDisplay: Boolean = false,
    val isDateDisplay: Boolean = false,
    val isTimeDisplay: Boolean = false,
    val semesters: List<String> = emptyList(),
    val browsedSemester: String? = null,
    val currentSemester: String? = null,
    val startLocalDate: LocalDate? = null,
    val teachingClassrooms: Result<List<Course>> = Result(),
    val buildingNames: List<String>? = null,
    val emptyClassrooms: Result<List<Classroom>> = Result(),
    val holdingCourseTooltipState: TooltipState = TooltipState(isPersistent = true),
    val scheduleNotes: Result<List<ScheduleNoteItem>> = Result(),
    val isShowScheduleNotesSheet: Boolean = false,
    val plannedSchedule: Result<List<PlannedCourse>> = Result(),
    val isShowPlannedScheduleSheet: Boolean = false,
    val isShowTeachingClassroomSheet: Boolean = false,
    val isShowEmptyClassroomSheet: Boolean = false,
    val dayOfWeekOnHoldingCourse: Int = 0,
    val nodeNoOnHoldingCourse: Int = 0,
    val isShowTeacherScheduleSheet: Boolean = false,
    val teacherSchedule: Result<List<GlobalCourse>> = Result(),
    val searchTeacherName: String = "",
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val networkRepo: NetworkRepo,
    private val dataStoreRepo: DataStoreRepo
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        ScheduleUiState(
            dayOfWeek = LocalDate.now().dayOfWeek.value,
            currentNode = getCurrentSmallNode(),
        )
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val yearAndSemesterStateFlow =
        dataStoreRepo.observeYearAndSemester().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepo.observeYearAndSemester().first()
            }
        )

    private val enterUniversityYearStateFlow =
        dataStoreRepo.observeEnterUniversityYear().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepo.observeEnterUniversityYear().first()
            }
        )

    private val isOtherCourseDisplayStateFlow =
        dataStoreRepo.observeIsOtherCourseDisplay().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                dataStoreRepo.observeIsOtherCourseDisplay().first()
            }
        )

    private val isYearDisplayStateFlow = dataStoreRepo.observeIsYearDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeIsYearDisplay().first()
        }
    )

    private val isDateDisplayStateFlow = dataStoreRepo.observeIsDateDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeIsDateDisplay().first()
        }
    )

    private val isTimeDisplayStateFlow = dataStoreRepo.observeIsTimeDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeIsTimeDisplay().first()
        }
    )

    private val isLoginStateFlow = dataStoreRepo.observeIsLogin().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeIsLogin().first()
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
            }.collect { data ->
                if (data[0].isNotEmpty() && data[1].isNotEmpty()) {
                    _uiState.update { it.copy(currentSemester = data[0]) }
                }
            }
        }
        viewModelScope.launch {
            isLoginStateFlow.collect {
                if (it) {
                    loadSchedule()
                }
            }
        }
    }

    fun setBrowsedSemester(value: String?) {
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

    suspend fun loadSchedule() {
        val semesterYearStart = enterUniversityYearStateFlow.value.toInt()
        val semesterYearEnd = yearAndSemesterStateFlow.value.take(4).toInt()
        val semesterNoEnd = yearAndSemesterStateFlow.value.last().toString().toInt()
        val semesters = mutableListOf<String>()
        (semesterYearStart..semesterYearEnd).forEach label@{ year ->
            (1..2).forEach { no ->
                if (year == semesterYearEnd && no > semesterNoEnd) {
                    return@label
                }
                semesters.add("$year-${year + 1}-$no")
            }
        }
        _uiState.update {
            it.copy(
                browsedSemester = it.browsedSemester ?: yearAndSemesterStateFlow.value,
                semesters = semesters,
                startLocalDate = networkRepo.getSemesterStartDate(
                    it.browsedSemester ?: yearAndSemesterStateFlow.value
                ),
            )
        }
        if (_uiState.value.currentWeek == null) {
            val currentWeek = getWeekCount(_uiState.value.startLocalDate, LocalDate.now())
            _uiState.update {
                it.copy(currentWeek = currentWeek, browsedWeek = currentWeek)
            }
        }
        _uiState.update { it.copy(courses = Result(networkRepo.getSchedule(_uiState.value.browsedSemester))) }
    }

    suspend fun loadTeachingClassrooms() {
        _uiState.update { it.copy(buildingNames = null) }
        val node = _uiState.value.nodeNoOnHoldingCourse
        val dayOfWeek = _uiState.value.dayOfWeekOnHoldingCourse
        val startNode = node * 2 - 1
        val result = _uiState.value.browsedSemester?.let {
            networkRepo.getGlobalSchedule(
                yearAndSemester = it,
                startWeekNo = _uiState.value.browsedWeek.toString(),
                endWeekNo = _uiState.value.browsedWeek.toString(),
                startDayOfWeek = dayOfWeek.toString(),
                endDayOfWeek = dayOfWeek.toString(),
                startNode = startNode.toString(),
                endNode = (node * 2).toString(),
            )
        }
        var teachingClassrooms: List<Course>? = null
        result?.forEach { course ->
            if (course.classroom.split(", ").size > 1) {
                course.sksjdd!!.split("\n").forEach {
                    val items = it.split(" ")
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
                            if (items[2].replace("小节", "").split("-")[0].toInt() == startNode) {
                                val tmp = Course().copy(
                                    courseNameHtml = course.courseNameHtml,
                                    classroomNameHtml = items[3],
                                    teacherNameHtml = course.teacherNameHtml,
                                    classNameHtml = course.classNameHtml,
                                )
                                teachingClassrooms = if (teachingClassrooms == null) {
                                    listOf(tmp)
                                } else {
                                    teachingClassrooms!!.plus(tmp)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (result != null && teachingClassrooms == null) {
            teachingClassrooms = emptyList()
        }
        _uiState.update {
            it.copy(
                buildingNames = teachingClassrooms?.map { course ->
                    course.classroomName?.split("-")?.get(0) ?: ""
                }?.distinct()?.sorted(),
                teachingClassrooms = Result(teachingClassrooms?.distinct()
                    ?.sortedBy { item -> item.classroomName }),
            )
        }
    }

    suspend fun loadEmptyClassroom() {
        _uiState.update { it.copy(buildingNames = null) }
        val result = _uiState.value.browsedWeek?.let {
            networkRepo.getEmptyClassroom(
                weekNo = listOf(it),
                dayOfWeekNo = listOf(_uiState.value.dayOfWeekOnHoldingCourse),
                nodeNo = listOf(_uiState.value.nodeNoOnHoldingCourse),
            )
        }
        _uiState.update {
            it.copy(
                buildingNames = result?.map { course ->
                    course.buildingName ?: ""
                }?.distinct()?.sorted(),
                emptyClassrooms = Result(result),
            )
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

    suspend fun loadScheduleNotes() {
        _uiState.update {
            it.copy(
                scheduleNotes = Result(
                    networkRepo.getScheduleNotes(
                        _uiState.value.browsedSemester
                    )
                ),
            )
        }
    }

    fun setIsShowScheduleNotesSheet(value: Boolean) {
        _uiState.update {
            it.copy(isShowScheduleNotesSheet = value)
        }
    }

    suspend fun loadPlannedSchedule() {
        _uiState.update {
            it.copy(
                isShowPlannedScheduleSheet = true,
            )
        }
        _uiState.update {
            it.copy(
                plannedSchedule = Result(networkRepo.getPlannedSchedule()),
            )
        }
    }

    fun setIsShowPlannedScheduleSheet(value: Boolean) {
        _uiState.update {
            it.copy(isShowPlannedScheduleSheet = value)
        }
    }

    fun setIsShowNonEmptyClassroomSheet(value: Boolean) {
        _uiState.update {
            it.copy(isShowTeachingClassroomSheet = value)
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

    fun setIsShowTeacherScheduleSheet(value: Boolean) {
        _uiState.update {
            it.copy(searchTeacherName = "")
        }
        loadTeacherSchedule()
        _uiState.update {
            it.copy(
                isShowTeacherScheduleSheet = value
            )
        }
    }

    fun loadTeacherSchedule() {
        _uiState.value.browsedSemester?.let { browsedSemester ->
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        teacherSchedule = if (_uiState.value.searchTeacherName.isEmpty()) Result(
                            emptyList()
                        ) else Result(
                            networkRepo.getGlobalSchedule(
                                yearAndSemester = browsedSemester,
                                teacherName = _uiState.value.searchTeacherName,
                            )
                        ),
                    )
                }
            }
        }
    }

    fun setSearchTeacherName(value: String) {
        _uiState.update {
            it.copy(searchTeacherName = value)
        }
    }
}
