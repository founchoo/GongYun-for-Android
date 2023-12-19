package com.dart.campushelper.viewmodel

import androidx.annotation.WorkerThread
import com.dart.campushelper.data.DataStoreRepository
import com.dart.campushelper.data.NetworkRepository
import com.dart.campushelper.model.Course
import com.dart.campushelper.utils.getCurrentSmallNode
import com.dart.campushelper.utils.getWeekCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import java.time.LocalDateTime
import javax.inject.Inject

data class AppWidgetUiState(
    val courses: List<Course>? = null,
    val currentWeek: Int? = null,
    val dayOfWeek: Int,
    val currentNode: Int,
    val lastUpdateDateTime: LocalDateTime? = null,
)

class AppWidgetViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val dataStoreRepository: DataStoreRepository,
) {
    val scope = CoroutineScope(Dispatchers.IO)

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        AppWidgetUiState(
            dayOfWeek = LocalDate.now().dayOfWeek.value,
            currentNode = getCurrentSmallNode(),
        )
    )
    val uiState: StateFlow<AppWidgetUiState> = _uiState.asStateFlow()

    private val isLoginStateFlow: StateFlow<Boolean> = dataStoreRepository.observeIsLogin().stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            dataStoreRepository.observeIsLogin().first()
        }
    )

    init {
        scope.launch {
            isLoginStateFlow.collect {
                getTodaySchedule()
            }
        }
    }

    @WorkerThread
    suspend fun getTodaySchedule() {
        _uiState.update {
            it.copy(
                currentWeek = getWeekCount(
                    networkRepository.getSemesterStartDate(null),
                    LocalDate.now()
                )
            )
        }
        _uiState.update {
            it.copy(
                courses = networkRepository.getSchedule(null)?.filter { course ->
                    course.bigNodeNo!! % 2 != 0 && course.weekDayNo == _uiState.value.dayOfWeek && course.weekNoList.contains(
                        _uiState.value.currentWeek
                    )
                },
                lastUpdateDateTime = LocalDateTime.now()
            )
        }
    }
}