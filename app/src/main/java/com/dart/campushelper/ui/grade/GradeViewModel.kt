package com.dart.campushelper.ui.grade

import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.RankingInfo
import com.patrykandpatrick.vico.core.entry.FloatEntry
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
import org.jsoup.Jsoup
import javax.inject.Inject

data class GradeUiState(
    val rankingAvailable: Boolean = true,
    val rankingInfo: RankingInfo = RankingInfo(),
    val isRankingInfoLoading: Boolean = true,
    val grades: List<Grade> = emptyList(),
    val isGradesLoading: Boolean = true,
    val courseSorts: List<String> = emptyList(),
    val semesters: List<String> = emptyList(),
    val courseSortsSelected: Map<String, Boolean> = emptyMap(),
    val semestersSelected: Map<String, Boolean> = emptyMap(),
    val showLoginPlaceholder: Boolean = false,
    val gradePointAverage: Double = 0.0,
    val averageScore: Double = 0.0,
    val searchKeyword: String = "",
    val isGradeDetailDialogOpen: Boolean = false,
    val isShowLineChart: Boolean = false,
    val isLineChartLoading: Boolean = true,
    val contentForGradeDetailDialog: Grade = Grade(),
    val chartData: List<FloatEntry> = emptyList(),
)

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(GradeUiState())
    val uiState: StateFlow<GradeUiState> = _uiState.asStateFlow()

    private val usernameStateFlow: StateFlow<String> = userPreferenceRepository.observeUsername()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeUsername().first()
            }
        )

    private val isLoginStateFlow: StateFlow<Boolean> =
        userPreferenceRepository.observeIsLogin().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeIsLogin().first()
            }
        )

    private val enterUniversityYearStateFlow =
        userPreferenceRepository.observeEnterUniversityYear().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeEnterUniversityYear().first()
            }
        )

    private var _backupGrades = emptyList<Grade>()

    init {
        viewModelScope.launch {
            combine(
                isLoginStateFlow,
                usernameStateFlow,
                enterUniversityYearStateFlow
            ) { isLogin, username, enterUniversityYear ->
                listOf(isLogin.toString(), username, enterUniversityYear)
            }.collect {
                // Log.d("GradeViewModel", "observeIsLogin: $it")
                _uiState.update { uiState ->
                    uiState.copy(showLoginPlaceholder = it[1].isEmpty())
                }
                if (it[0] == true.toString() && it[1].isNotEmpty() && it[2].isNotEmpty()) {
                    getGrades()
                    getStudentRankingInfo()
                }
            }
        }
    }

    fun setIsGradeDetailDialogOpen(value: Boolean) {
        _uiState.update {
            it.copy(isGradeDetailDialogOpen = value)
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

    fun setIsShowLineChart(value: Boolean) {
        _uiState.update {
            it.copy(isShowLineChart = value)
        }
    }

    suspend fun loadLineChart() {
        _uiState.update {
            it.copy(isLineChartLoading = true)
        }
        val gradesResult = chaoxingRepository.getGrades()
        if (gradesResult != null) {
            val sorted = gradesResult.results.sortedBy { grade -> grade.semesterYearAndNo } + Grade()
            var flag = sorted.first().semesterYearAndNo
            val grades = mutableListOf<Grade>()
            val model = mutableListOf<FloatEntry>()
            var no = 1

            sorted.forEach {
                if (it.semesterYearAndNo != flag) {
                    model.add(FloatEntry(no.toFloat(), calculateGPA(grades).toFloat()))
                    grades.clear()
                    no++
                    flag = it.semesterYearAndNo
                }
                grades.add(it)
            }
            _uiState.update {
                it.copy(chartData = model, isLineChartLoading = false)
            }
        }
    }

    suspend fun getGrades() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isGradesLoading = true)
            }
            val gradesResult = chaoxingRepository.getGrades()
            if (gradesResult != null) {
                val grades = gradesResult.results
                _uiState.update {
                    it.copy(
                        grades = grades,
                    )
                }
                updateGPA()
                updateAverageScore()
                val courseSortList = grades.map {
                    it.courseType
                }.toSet().toList()
                val semesterList = grades.map { grade ->
                    grade.semesterYearAndNo ?: ""
                }.toSet().toList().sorted()
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

    private fun calculateGPA(grades: List<Grade>): Double {
        return grades.sumOf { grade ->
            (grade.score / 10.0 - 5) * grade.credit
        } / grades.sumOf { grade ->
            grade.credit
        }
    }

    private fun calculateAverageScore(grades: List<Grade>): Double {
        return grades.sumOf { grade ->
            grade.score.toDouble()
        } / grades.size
    }

    private fun updateGPA() {
        _uiState.update {
            it.copy(
                gradePointAverage = calculateGPA(it.grades),
            )
        }
    }

    private fun updateAverageScore() {
        _uiState.update {
            it.copy(
                averageScore = calculateAverageScore(it.grades),
            )
        }
    }

    suspend fun getStudentRankingInfo() {
        _uiState.update {
            it.copy(isRankingInfoLoading = true)
        }
        val stuRankInfoResult = chaoxingRepository.getStudentRankingInfo(
            _uiState.value.semestersSelected.map {
                if (it.value) {
                    it.key
                } else {
                    ""
                }
            }.joinToString(","),
        )
        if (stuRankInfoResult != null) {
            val rankingInfo = RankingInfo()
            Jsoup.parse(stuRankInfoResult).run {
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
            _uiState.update {
                it.copy(isRankingInfoLoading = false, rankingInfo = rankingInfo)
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
                        grade.courseType
                    ) && semestersSelectedMapKey.contains(grade.semesterYearAndNo ?: "")
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