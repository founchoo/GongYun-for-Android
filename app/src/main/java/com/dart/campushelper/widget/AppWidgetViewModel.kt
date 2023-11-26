package com.dart.campushelper.widget

import androidx.annotation.WorkerThread
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.model.Course
import com.dart.campushelper.utils.getCurrentNode
import com.dart.campushelper.utils.getWeekCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AppWidgetUiState(
    val courses: List<Course> = emptyList(),
    val currentWeek: Int,
    val dayOfWeek: Int,
    val currentNode: Int,
)

class AppWidgetViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
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

    @WorkerThread
    suspend fun getTodaySchedule() {
        getCurrentWeek(null)
        // Log.d("AppWidgetViewModel", "getTodaySchedule: ")
        val result = chaoxingRepository.getSchedule(null)
        if (result != null) {
            val courses = result.filter { course ->
                course.nodeNo!! % 2 != 0 && course.weekDayNo == _uiState.value.dayOfWeek && course.weekNoList.contains(
                    _uiState.value.currentWeek
                )
            }
            // Log.d("AppWidgetViewModel", "courses.size: ${courses.size}")
            _uiState.update { uiState ->
                uiState.copy(courses = courses)
            }
        }
    }

    @WorkerThread
    private suspend fun getCurrentWeek(semesterYearAndNo: String?) {
        val list = chaoxingRepository.getCalendar(semesterYearAndNo)
        if (list != null) {
            val first = list[0]
            val day = first.monday ?: (first.tuesday ?: (first.wednesday
                ?: (first.thursday ?: (first.friday ?: (first.saturday ?: first.sunday ?: "")))))
            val date =
                first.yearAndMonth + "-" + (if (day.toInt() < 10) "0$day" else day)
            val startLocalDate =
                LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            if (semesterYearAndNo == null) {
                val currentWeek = getWeekCount(startLocalDate, LocalDate.now())
                _uiState.update { uiState ->
                    uiState.copy(currentWeek = currentWeek)
                }
            }
        }
    }
}