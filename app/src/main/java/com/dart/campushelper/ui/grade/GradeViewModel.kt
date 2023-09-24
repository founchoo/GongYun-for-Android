package com.dart.campushelper.ui.grade

import android.util.Log
import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.utils.ResponseErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.Jsoup
import javax.inject.Inject

data class GradesUiState(
    val rankingAvailable: Boolean = true,
    val rankingInfo: RankingInfo = RankingInfo(),
    val isRankingInfoLoading: Boolean = true,
    val grades: List<Grade> = emptyList(),
    val isGradesLoading: Boolean = true,
    val courseSorts: List<String> = emptyList(),
    val semesters: List<String> = emptyList(),
    val courseSortsSelected: Map<String, Boolean> = emptyMap(),
    val semestersSelected: Map<String, Boolean> = emptyMap(),
    val isLogin: Boolean = false,
    val gradePointAverage: Double = 0.0,
    val averageScore: Double = 0.0,
    val searchKeyword: String = "",
    val isGradeDetailDialogOpen: Boolean = false,
    val contentForGradeDetailDialog: Grade = Grade(),
)

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    private val mutex = Mutex()

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    private var _backupGrades = emptyList<Grade>()

    init {
        viewModelScope.launch {
            userPreferenceRepository.observeIsLogin().collect {
                Log.d("GradeViewModel", "observeIsLogin: $it")
                _uiState.update { uiState ->
                    uiState.copy(isLogin = it)
                }
                if (it) {
                    getGrades()
                    getStudentRankingInfo()
                }
            }
        }
    }

    fun showGradeDetailDialog() {
        _uiState.update {
            it.copy(isGradeDetailDialogOpen = true)
        }
    }

    fun hideGradeDetailDialog() {
        _uiState.update {
            it.copy(isGradeDetailDialogOpen = false)
        }
    }

    fun changeSearchKeyword(keyword: String) {
        _uiState.update {
            it.copy(searchKeyword = keyword)
        }
    }

    fun setContentForGradeDetailDialog(value: Grade) {
        _uiState.update {
            it.copy(contentForGradeDetailDialog = value)
        }
    }

    private suspend fun getGrades() {
        mutex.withLock {
            viewModelScope.launch {
                chaoxingRepository.getGrades().collect { gradesResponse ->
                    val grades = gradesResponse?.body()?.results
                    if (grades != null) {
                        _uiState.update {
                            it.copy(
                                grades = grades,
                            )
                        }
                        updateGPA()
                        updateAverageScore()
                        val courseSortList = grades.map {
                            it.courseSort ?: ""
                        }.toSet().toList()
                        val semesterList = grades.map { grade ->
                            grade.xnxq ?: ""
                        }.toSet().toList()
                        _uiState.update {
                            it.copy(
                                courseSorts = courseSortList,
                                courseSortsSelected = courseSortList.map { sortName ->
                                    sortName to true
                                }.toMutableStateMap(),
                                semesters = semesterList,
                                semestersSelected = semesterList.map { semesterName ->
                                    semesterName to true
                                }.toMutableStateMap(),
                                isGradesLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateGPA() {
        _uiState.update {
            it.copy(
                gradePointAverage = it.grades.sumOf { grade ->
                    (grade.score / 10.0 - 5) * grade.credit
                } / it.grades.sumOf { grade ->
                    grade.credit
                },
            )
        }
    }

    private fun updateAverageScore() {
        _uiState.update {
            it.copy(
                averageScore = it.grades.sumOf { grade ->
                    grade.score.toDouble()
                } / it.grades.size,
            )
        }
    }

    private suspend fun getStudentRankingInfo() {
        mutex.withLock {
            _uiState.update {
                it.copy(isRankingInfoLoading = true)
            }
            chaoxingRepository.getStudentRankingInfo(
                _uiState.value.semestersSelected.map {
                    if (it.value) {
                        it.key
                    } else {
                        ""
                    }
                }.joinToString(",")
            ).collect { rankingInfoResponse ->
                val rankingInfo = RankingInfo()
                ResponseErrorHandler(
                    response = rankingInfoResponse,
                    scope = viewModelScope,
                    actionWhenResponseSuccess = { it ->
                        Jsoup.parse(it).run {
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
                                    1 -> rankingInfo.byGPAByInstitute = value
                                    2 -> rankingInfo.byGPAByMajor = value
                                    3 -> rankingInfo.byGPAByClass = value
                                    5 -> rankingInfo.byScoreByInstitute = value
                                    6 -> rankingInfo.byScoreByMajor = value
                                    7 -> rankingInfo.byScoreByClass = value
                                }
                            }
                        }
                    }
                )
                _uiState.update {
                    it.copy(isRankingInfoLoading = false, rankingInfo = rankingInfo)
                }
            }
        }
    }

    fun filterGrades(text: String) {
        if (_backupGrades.isEmpty()) {
            _backupGrades = _uiState.value.grades
        }
        val courseSortsSelectedMapKey = _uiState.value.courseSortsSelected.filter {
            it.value
        }.keys.toList()
        val semestersSelectedMapKey = _uiState.value.semestersSelected.filter {
            it.value
        }.keys.toList()
        _uiState.update {
            it.copy(
                grades = _backupGrades.filter { grade ->
                    grade.name.contains(text) && courseSortsSelectedMapKey.contains(
                        grade.courseSort
                    ) && semestersSelectedMapKey.contains(grade.xnxq ?: "")
                },
                rankingAvailable = !_uiState.value.courseSortsSelected.containsValue(false) && text == ""
            )
        }
        updateGPA()
        updateAverageScore()
        viewModelScope.launch {
            getStudentRankingInfo()
        }
    }

    fun changeCourseSortSelected(courseSort: String, selected: Boolean) {
        _uiState.update {
            it.copy(
                courseSortsSelected = it.courseSortsSelected.toMutableMap().apply {
                    this[courseSort] = selected
                }
            )
        }
    }

    fun changeSemesterSelected(semester: String, selected: Boolean) {
        _uiState.update {
            it.copy(
                semestersSelected = it.semestersSelected.toMutableMap().apply {
                    this[semester] = selected
                }
            )
        }
    }
}