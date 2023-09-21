package com.dart.campushelper.ui.grade

import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.app.AppRepository
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.GradeResponse
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.ui.Root
import com.dart.campushelper.utils.Constants.Companion.GRADE_HTTP_URL
import com.dart.campushelper.utils.DefaultCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import org.jsoup.Jsoup
import java.lang.reflect.Type

class GradeViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    var grades = MutableStateFlow<List<Grade>>(emptyList())
        private set

    private var backupGrades = emptyList<Grade>()

    var courseSorts = MutableStateFlow<List<String>>(emptyList())
        private set

    var courseSortsSelected = MutableStateFlow<MutableMap<String, Boolean>>(mutableMapOf())
        private set

    var semesters = MutableStateFlow<List<String>>(emptyList())
        private set

    var semestersSelected = MutableStateFlow<MutableMap<String, Boolean>>(mutableMapOf())
        private set

    var rankingAvailable = MutableStateFlow<Boolean>(true)
        private set

    var rankingInfo = MutableStateFlow(RankingInfo())
        private set

    var isRankingInfoLoading = MutableStateFlow<Boolean>(true)
        private set

    var isGradesLoading = MutableStateFlow<Boolean>(true)
        private set

    fun loadGrades() {
        Root.client.newCall(
            Request.Builder()
                .url(GRADE_HTTP_URL)
                .get()
                .build()
        ).enqueue(DefaultCallback(
            scope = viewModelScope,
            uiRelatedLoadingIndicators = listOf(isGradesLoading),
            actionWhenResponseSuccess = { response ->
                val json = response.body?.string()
                val type: Type = object : TypeToken<GradeResponse?>() {}.type
                val res: GradeResponse = Gson().fromJson(json, type)
                viewModelScope.launch {
                    grades.emit(res.results)
                    val courseSortList = res.results.map {
                        it.courseSort ?: ""
                    }.toSet().toList()
                    courseSorts.emit(courseSortList)
                    courseSortsSelected.emit(courseSortList.map {
                        it to true
                    }.toMutableStateMap())
                    val semesterList = res.results.map {
                        it.xnxq ?: ""
                    }.toSet().toList()
                    semesters.emit(semesterList)
                    semestersSelected.emit(semesterList.map {
                        it to true
                    }.toMutableStateMap())
                }
            }
        ))
    }

    fun loadRankingInfo(semesterList: List<String>? = null) {
        isRankingInfoLoading.value = true
        var semesterListStr = ""
        semesterList?.forEach {
            semesterListStr += "$it,"
        }
        Root.client.newCall(
            Request.Builder()
                .url(
                    "https://hbut.jw.chaoxing.com/admin/cjgl/xscjbbdy/getXscjpm?xh=" +
                            Root.username +
                            "&sznj=" +
                            Root.enterUniversityYear +
                            "&xnxq=" +
                            semesterListStr
                )
                .get()
                .build()
        ).enqueue(DefaultCallback(
            scope = viewModelScope,
            uiRelatedLoadingIndicators = listOf(isRankingInfoLoading),
            actionWhenResponseSuccess = { it ->
                val html = it.body?.string()
                val res = RankingInfo()
                Jsoup.parse(html).run {
                    select("table")[1].select("td").forEachIndexed { index, element ->
                        val value = element.text().contains("/").run {
                            if (this) {
                                element.text().split("/").let {
                                    if (it[0] == "" || it[1] == "") {
                                        Pair(0, 0)
                                    } else {
                                        Pair(it[0].toInt(), it[1].toInt())
                                    }
                                }
                            } else {
                                Pair(0, 0)
                            }
                        }

                        when (index) {
                            1 -> res.byGPAByInstitute = value
                            2 -> res.byGPAByMajor = value
                            3 -> res.byGPAByClass = value
                            5 -> res.byScoreByInstitute = value
                            6 -> res.byScoreByMajor = value
                            7 -> res.byScoreByClass = value
                        }
                    }
                }
                viewModelScope.launch {
                    rankingInfo.emit(res)
                }
            }
        ))
    }

    fun filterGrades(text: String) {
        if (backupGrades.isEmpty()) {
            backupGrades = grades.value
        }
        viewModelScope.launch {
            val courseSortsSelectedMapKey = courseSortsSelected.value.filter {
                it.value
            }.keys.toList()
            val semestersSelectedMapKey = semestersSelected.value.filter {
                it.value
            }.keys.toList()
            grades.emit(backupGrades.filter { grade ->
                grade.name.contains(text) && courseSortsSelectedMapKey.contains(
                    grade.courseSort
                ) && semestersSelectedMapKey.contains(grade.xnxq ?: "")
            })
            rankingAvailable.emit(!courseSortsSelected.value.containsValue(false) && text == "")
            if (rankingAvailable.value) {
                loadRankingInfo(semestersSelected.value.filter {
                    it.value
                }.keys.toList())
            }
        }
    }

    companion object {
        fun provideFactory(
            appRepository: AppRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GradeViewModel(appRepository) as T
            }
        }
    }
}