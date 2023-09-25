package com.dart.campushelper.widget

import android.util.Log
import androidx.annotation.WorkerThread
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.model.Course
import com.dart.campushelper.utils.getCurrentNode
import com.dart.campushelper.utils.getWeekCount
import com.dart.campushelper.utils.network.Status
import kotlinx.coroutines.MainScope
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
        MainScope(),
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeStartLocalDate().first()
        }
    )

    @WorkerThread
    suspend fun observeStartLocalDate() {
        MainScope().launch {
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
        val result = chaoxingRepository.getSchedule()
        if (result.status == Status.SUCCESS) {
            val courses = result.data!!.filter { course ->
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