package com.dart.campushelper.ui.schedule

import android.util.Log
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_IS_DATE_DISPLAY
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_IS_TIME_DISPLAY
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_IS_YEAR_DISPLAY
import com.dart.campushelper.model.Course
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.DateUtils
import com.dart.campushelper.utils.getCurrentNode
import com.dart.campushelper.utils.getWeekCount
import com.dart.campushelper.utils.network.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val courses: Map<Pair<Int, Int>, Course> = emptyMap(),
    val isTimetableLoading: Boolean = false,
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
    val dateHeaders: List<String>? = null,
    val nodeStartHeaders: List<String> = DateUtils.nodeEnds.map {
        LocalTime.of(
            it.split(":")[0].toInt(),
            it.split(":")[1].toInt()
        ).minusMinutes(45).format(DateTimeFormatter.ofPattern("HH:mm"))
    },
    val nodeEndHeaders: List<String> = DateUtils.nodeEnds,
    val browsedYear: String = "",
    val isOtherCourseDisplay: Boolean = false,
    val isYearDisplay: Boolean = false,
    val isDateDisplay: Boolean = false,
    val isTimeDisplay: Boolean = false,
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

    private val isLoginStateFlow: StateFlow<Boolean> =
        userPreferenceRepository.observeIsLogin().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeIsLogin().first()
            }
        )

    private val isOtherCourseDisplayStateFlow = userPreferenceRepository.observeIsOtherCourseDisplay().stateIn(
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

    private val startLocalDateStateFlow = userPreferenceRepository.observeStartLocalDate().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            userPreferenceRepository.observeStartLocalDate().first()
        }
    )

    init {
        viewModelScope.launch {
            isLoginStateFlow.collect {
                Log.d("ScheduleViewModel", "isLogin: $it")
                if (it) {
                    getCourses()
                }
            }
        }
        viewModelScope.launch {
            startLocalDateStateFlow.collect { startLocalDate ->
                val currentWeek = getWeekCount(startLocalDate, LocalDate.now())
                _uiState.update { uiState ->
                    uiState.copy(currentWeek = currentWeek, browsedWeek = currentWeek)
                }
                updateDateHeaders()
                updateBrowsedYear()
            }
        }
        viewModelScope.launch {
            isOtherCourseDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isOtherCourseDisplay = value ?: DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY)
                }
            }
        }
        viewModelScope.launch {
            isYearDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isYearDisplay = value ?: DEFAULT_VALUE_IS_YEAR_DISPLAY)
                }
            }
        }
        viewModelScope.launch {
            isDateDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isDateDisplay = value ?: DEFAULT_VALUE_IS_DATE_DISPLAY)
                }
            }
        }
        viewModelScope.launch {
            isTimeDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isTimeDisplay = value ?: DEFAULT_VALUE_IS_TIME_DISPLAY)
                }
            }
        }
    }

    private fun updateBrowsedYear() {
        _uiState.update {
            it.copy(browsedYear = startLocalDateStateFlow.value?.plusDays(_uiState.value.browsedWeek * 7L)?.format(
                DateTimeFormatter.ofPattern("yyyy")
            )?.takeLast(2) ?: "")
        }
    }

    fun setBrowsedWeek(value: Int) {
        _uiState.update {
            it.copy(
                browsedWeek = value
            )
        }
        updateDateHeaders()
        updateBrowsedYear()
    }

    fun resetBrowsedWeek() {
        _uiState.update {
            it.copy(
                browsedWeek = _uiState.value.currentWeek
            )
        }
        updateDateHeaders()
        updateBrowsedYear()
    }

    private fun updateDateHeaders() {
        _uiState.update {
            it.copy(dateHeaders = (0..6).map { day ->
                startLocalDateStateFlow.value?.plusDays(_uiState.value.browsedWeek * 7 + day.toLong())?.format(
                    DateTimeFormatter.ofPattern("M-d")
                ) ?: ""
            })
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

    suspend fun getCourses() {
        Log.d("ScheduleViewModel", "getCourses")
        _uiState.update { uiState ->
            uiState.copy(isTimetableLoading = true)
        }
        Log.d("ScheduleViewModel", "getCourses: ${_uiState.value.isTimetableLoading}")
        val resource = chaoxingRepository.getSchedule()
        Log.d("ScheduleViewModel", "getCourses: ${resource.status}")
        when (resource.status) {
            Status.SUCCESS -> {
                val map = resource.data!!.associateBy { course ->
                    Pair(course.weekDayNo!!, (course.nodeNo!! + 1) / 2)
                }
                Log.d("ScheduleViewModel", "getCourses: ${map.size}")
                _uiState.update { uiState ->
                    uiState.copy(courses = map, isTimetableLoading = false)
                }
            }

            Status.ERROR -> {
                val result = MainActivity.snackBarHostState.showSnackbar(
                    "加载课表失败，请稍后重试",
                    "重试"
                )
                if (result == SnackbarResult.ActionPerformed) {
                    getCourses()
                }
            }

            Status.INVALID -> {

            }
        }
    }
}
