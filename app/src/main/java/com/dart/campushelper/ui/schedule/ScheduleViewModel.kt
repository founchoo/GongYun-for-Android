package com.dart.campushelper.ui.schedule

import android.util.Log
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_START_LOCALDATE
import com.dart.campushelper.model.Course
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.getCurrentNode
import com.dart.campushelper.utils.getWeekCount
import com.dart.campushelper.utils.network.Status
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
import javax.inject.Inject

data class ScheduleUiState(
    val courses: Map<Pair<Int, Int>, Course> = emptyMap(),
    val isTimetableLoading: Boolean = true,
    val currentWeek: Int,
    // Indicate the day of week, 1 for Monday, 7 for Sunday
    val dayOfWeek: Int,
    // Indicate the node of the current day, 1 for 8:20 - 9:05, 2 for 9:10 - 9:55, etc.
    val currentNode: Int = 1,
    val showLoginPlaceholder: Boolean = false,
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

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        ScheduleUiState(
            currentWeek = 1,
            dayOfWeek = LocalDate.now().dayOfWeek.value,
            currentNode = getCurrentNode()
        )
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    val usernameStateFlow: StateFlow<String> = userPreferenceRepository.observeUsername()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeUsername().first()
            }
        )

    val isLoginStateFlow: StateFlow<Boolean> = userPreferenceRepository.observeIsLogin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            userPreferenceRepository.observeIsLogin().first()
        }
    )

    private val startLocalDateStateFlow = userPreferenceRepository.observeStartLocalDate().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DEFAULT_VALUE_START_LOCALDATE
    )

    init {
        viewModelScope.launch {
            combine(
                isLoginStateFlow,
                usernameStateFlow
            ) { isLogin, username ->
                Log.d("ScheduleViewModel", "observeIsLogin: $isLogin, $username")
                listOf(isLogin.toString(), username)
            }.collect {
                Log.d("ScheduleViewModel", "observeIsLogin: $it")
                _uiState.update { uiState ->
                    uiState.copy(showLoginPlaceholder = it[1].isEmpty())
                }
                if (it[0] == true.toString() && it[1].isNotEmpty()) {
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
                Log.d("ScheduleViewModel", "getCourses: $map")
                _uiState.update { uiState ->
                    uiState.copy(courses = map, isTimetableLoading = false)
                }
            }
            Status.ERROR -> {
                val result = MainActivity.snackBarHostState.showSnackbar("加载课表失败，请稍后重试", "重试")
                if (result == SnackbarResult.ActionPerformed) {
                    getCourses()
                }
            }
            Status.LOADING -> {

            }
        }
    }
}
