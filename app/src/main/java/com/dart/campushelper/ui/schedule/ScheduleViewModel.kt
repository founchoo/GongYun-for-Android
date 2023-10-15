package com.dart.campushelper.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
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
    private suspend fun getStartLocalDate(semesterYearAndNo: String?) {
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

    fun showCourseDetailDialog() {
        _uiState.update {
            it.copy(isCourseDetailDialogOpen = true)
        }
    }

    fun hideCourseDetailDialog() {
        _uiState.update {
            it.copy(isCourseDetailDialogOpen = false)
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
        Log.d("ScheduleViewModel", "getCourses")
        val resource = chaoxingRepository.getSchedule(semesterYearAndNo)
        Log.d("ScheduleViewModel", "getCourses: resource: $resource")
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
}
