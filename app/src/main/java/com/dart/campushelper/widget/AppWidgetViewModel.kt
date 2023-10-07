package com.dart.campushelper.widget

import android.util.Log
import androidx.annotation.WorkerThread
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.model.Course
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.getCurrentNode
import com.dart.campushelper.utils.getWeekCount
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
import javax.inject.Inject

data class AppWidgetUiState(
    val courses: List<Course> = emptyList(),
    val currentWeek: Int,
    val dayOfWeek: Int,
    val currentNode: Int,
)

class AppWidgetViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
    userPreferenceRepository: UserPreferenceRepository
) {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        AppWidgetUiState(
            currentWeek = 1,
            dayOfWeek = LocalDate.now().dayOfWeek.value,
            currentNode = getCurrentNode()
        )
    )
    val uiState: StateFlow<AppWidgetUiState> = _uiState.asStateFlow()

    private val startLocalDateStateFlow = userPreferenceRepository.observeStartLocalDate().stateIn(
        MainActivity.scope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeStartLocalDate().first()
        }
    )

    private val semesterYearAndNoStateFlow = userPreferenceRepository.observeSemesterYearAndNo().stateIn(
        scope = MainActivity.scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            userPreferenceRepository.observeSemesterYearAndNo().first()
        }
    )

    private val usernameStateFlow: StateFlow<String> = userPreferenceRepository.observeUsername()
        .stateIn(
            scope = MainActivity.scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeUsername().first()
            }
        )

    @WorkerThread
    suspend fun observeStartLocalDate() {
        MainActivity.scope.launch {
            startLocalDateStateFlow.collect { startLocalDate ->
                val currentWeek = getWeekCount(startLocalDate, LocalDate.now())
                _uiState.update { uiState ->
                    uiState.copy(currentWeek = currentWeek)
                }
            }
        }
    }

    @WorkerThread
    suspend fun getTodaySchedule() {

        Log.d("AppWidgetViewModel", "getTodaySchedule: ")
        // TODO
        val result = chaoxingRepository.getSchedule()
        if (result != null) {
            val courses = result.filter { course ->
                course.nodeNo!! % 2 != 0 && course.weekDayNo == _uiState.value.dayOfWeek && course.weekNoList.contains(
                    _uiState.value.currentWeek
                )
            }
            Log.d("AppWidgetViewModel", "courses.size: ${courses?.size ?: -1}")
            _uiState.update { uiState ->
                uiState.copy(courses = courses)
            }
        }
    }
}