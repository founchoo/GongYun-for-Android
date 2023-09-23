package com.dart.campushelper.ui.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_START_LOCALDATE
import com.dart.campushelper.model.Course
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
import java.time.LocalTime
import javax.inject.Inject

data class ScheduleUiState(
    val courses: Map<Pair<Int, Int>, Course> = emptyMap(),
    val isTimetableLoading: Boolean = true,
    val currentWeek: Int,
    // Indicate the day of week, 1 for Monday, 7 for Sunday
    val dayOfWeek: Int,
    // Indicate the node of the current day, 1 for 8:20 - 9:05, 2 for 9:10 - 9:55, etc.
    val currentNode: Int = 1,
    val isLogin: Boolean = false
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

    private fun getCurrentNode(): Int {
        val currentMins = LocalTime.now().hour * 60 + LocalTime.now().minute
        val nodeEnds = listOf("09:05", "09:55", "11:00", "11:50", "14:45", "15:35", "16:40", "17:30", "19:15", "20:05")
        nodeEnds.forEachIndexed { i, node ->
            val nodeEndMins = node.split(":")[0].toInt() * 60 + node.split(":")[1].toInt()
            if (currentMins <= nodeEndMins) {
                return i + 1
            }
        }
        return 1
    }

    private fun getWeekCount(startLocalDate: LocalDate?, endLocalDate: LocalDate?): Int {
        return if (startLocalDate != null && endLocalDate != null) {
            startLocalDate.let {
                val days = endLocalDate.dayOfYear - it.dayOfYear
                (days / 7)
            }
        } else {
            1
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
