package com.dart.campushelper.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.AppDataStore
import com.dart.campushelper.data.app.AppRepository
import com.dart.campushelper.model.Course
import com.dart.campushelper.ui.Root
import com.dart.campushelper.utils.DefaultCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.Request
import java.lang.reflect.Type
import java.time.LocalDate


class ScheduleViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    var courses = MutableStateFlow<Map<Pair<Int, Int>, Course>>(mutableMapOf())
        private set

    var isTimetableLoading = MutableStateFlow(true)
        private set

    val currentWeek = appRepository.observeCurrentWeek().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AppDataStore.DEFAULT_VALUE_DISPLAYED_WEEK
    )

    val dayOfWeek = appRepository.observeDayOfWeek().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        AppDataStore.DEFAULT_VALUE_DAY_OF_WEEK
    )

    init {
        viewModelScope.launch {
            appRepository.observeStartLocalDate().collect {
                val currentWeek = it?.let { localDate ->
                    val days = LocalDate.now().dayOfYear - localDate.dayOfYear
                    (days / 7)
                } ?: 1
                appRepository.changeCurrentWeek(currentWeek)
            }
            appRepository.observeIsLogin().collect {
                if (it == false) {
                    courses.emit(mutableMapOf())
                }
            }
        }
        loadCourses()
    }

    fun loadCourses() {

        isTimetableLoading.value = true
        Root.client.newCall(
            Request.Builder()
                .url(
                    "https://hbut.jw.chaoxing.com/admin/pkgl/xskb/sdpkkbList?xnxq=" +
                            Root.semesterYearAndNo +
                            "&xhid=" +
                            Root.username +
                            "&xqdm=" +
                            if (Root.semesterYearAndNo.isNotEmpty()) Root.semesterYearAndNo.last() else ""
                )
                .get()
                .build()
        ).enqueue(DefaultCallback(
            scope = viewModelScope,
            uiRelatedLoadingIndicators = listOf(isTimetableLoading),
            actionWhenResponseSuccess = { response ->
                val json = response.body?.string()
                val listType: Type = object : TypeToken<ArrayList<Course?>?>() {}.type
                val list: List<Course> = Gson().fromJson(json, listType)
                var tmp = mutableMapOf<Pair<Int, Int>, Course>()
                for (course in list) {
                    tmp[Pair(course.weekDayNo!!, (course.nodeNo!! + 1) / 2)] = course
                }
                viewModelScope.launch {
                    courses.emit(tmp)
                }
            }
        ))
    }

    companion object {
        fun provideFactory(
            appRepository: AppRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScheduleViewModel(appRepository) as T
            }
        }
    }
}
