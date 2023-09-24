package com.dart.campushelper.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_START_LOCALDATE
import com.dart.campushelper.model.Course
import com.dart.campushelper.utils.getCurrentNode
import com.dart.campushelper.utils.getWeekCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject

data class ScheduleUiState(
    val courses: Map<Pair<Int, Int>, Course> = emptyMap(),
    val isTimetableLoading: Boolean = true,
    val currentWeek: Int,
    // Indicate the day of week, 1 for Monday, 7 for Sunday
    val dayOfWeek: Int,
    // Indicate the node of the current day, 1 for 8:20 - 9:05, 2 for 9:10 - 9:55, etc.
    val currentNode: Int = 1,
    val isLogin: Boolean = false,
    val isCourseDetailDialogOpen: Boolean = false,
    val nodeHeaders: IntRange = (1..10),
    val weekHeaders: List<String> = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日"),
    val contentInCourseDetailDialog: List<Course> = emptyList()
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    private val mutex = Mutex()

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        ScheduleUiState(
            currentWeek = 1,
            dayOfWeek = LocalDate.now().dayOfWeek.value,
            currentNode = getCurrentNode()
        )
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val startLocalDateStateFlow = userPreferenceRepository.observeStartLocalDate().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DEFAULT_VALUE_START_LOCALDATE
    )

    init {
        viewModelScope.launch {
            userPreferenceRepository.observeIsLogin()
                .collect {
                    Log.d("ScheduleViewModel", "observeIsLogin: $it")
                    _uiState.update { uiState ->
                        uiState.copy(isLogin = it)
                    }
                    if (it) {
                        getCourses()
                    }
                }
        }
        viewModelScope.launch {
            startLocalDateStateFlow.collect { startLocalDate ->
                val currentWeek = getWeekCount(startLocalDate, LocalDate.now())
                _uiState.update { uiState ->
                    uiState.copy(currentWeek = currentWeek)
                }
            }
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

    private suspend fun getCourses() {
        Log.d("ScheduleViewModel", "getCourses")
        mutex.withLock {
            _uiState.update { uiState ->
                uiState.copy(isTimetableLoading = true)
            }
            Log.d("ScheduleViewModel", "getCourses: ${_uiState.value.isTimetableLoading}")
            chaoxingRepository.getSchedule().collect { coursesResponse ->
                Log.d("ScheduleViewModel", "getCourses: ${coursesResponse.body()}")
                val map = coursesResponse.body()?.associateBy { course ->
                    Pair(course.weekDayNo!!, (course.nodeNo!! + 1) / 2)
                }
                if (map != null) {
                    _uiState.update { uiState ->
                        uiState.copy(courses = map, isTimetableLoading = false)
                    }
                }
            }
        }
    }
}
