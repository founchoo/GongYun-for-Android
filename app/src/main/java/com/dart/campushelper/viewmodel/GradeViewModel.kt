package com.dart.campushelper.viewmodel

import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.CampusHelperApplication.Companion.context
import com.dart.campushelper.R
import com.dart.campushelper.data.DataStoreRepository
import com.dart.campushelper.data.NetworkRepository
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.model.SubRankingType
import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

enum class SortBasis {
    GRADE,
    CREDIT;

    override fun toString(): String {
        return when (this) {
            GRADE -> context.getString(R.string.grade_label)
            CREDIT -> context.getString(R.string.credit_label)
        }
    }
}

class SortBasisItem(val sortBasis: SortBasis) {

    private var _selected: Boolean = false
    val selected: Boolean
        get() = _selected

    private var _asc: Boolean = false
    val asc: Boolean
        get() = _asc

    fun setSelected(value: Boolean) {
        _selected = value
    }

    fun setAsc(value: Boolean) {
        _asc = value
    }
}

data class GradeUiState(
    val rankingAvailable: Boolean = true,
    val rankingInfo: RankingInfo = RankingInfo(),
    val isRankingInfoLoading: Boolean = true,
    val grades: List<Grade> = emptyList(),
    val isGradesLoading: Boolean = true,
    val courseTypes: List<String> = emptyList(),
    val semesters: List<String> = emptyList(),
    val courseTypesSelected: Map<String, Boolean> = emptyMap(),
    val semestersSelected: Map<String, Boolean> = emptyMap(),
    val gradePointAverage: Double = 0.0,
    val averageScore: Double = 0.0,
    val searchKeyword: String = "",
    val isGradeDetailDialogOpen: Boolean = false,
    val isShowLineChart: Boolean = false,
    val isLineChartLoading: Boolean = true,
    val contentForGradeDetailDialog: Grade = Grade(),
    val overallScoreData: List<FloatEntry> = emptyList(),
    val gradeDistribution: List<Int> = emptyList(),
    val entryModelForRankingColumnChart: ComposedChartEntryModel<ChartEntryModel>? = null,
    val isScreenshotMode: Boolean = false,
    val isSearchBarShow: Boolean = false,
    val isSearchBarActive: Boolean = false,
    val openFilterSheet: Boolean = false,
    val openSummarySheet: Boolean = false,
    val fabVisibility: Boolean? = null,
    val sortBasisList: List<SortBasisItem> = SortBasis.values().map {
        SortBasisItem(it)
    }
)

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(GradeUiState())
    val uiState: StateFlow<GradeUiState> = _uiState.asStateFlow()

    private val isScreenshotModeStateFlow: StateFlow<Boolean> =
        dataStoreRepository.observeIsScreenshotMode().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepository.observeIsScreenshotMode().first()
            }
        )

    private val isLoginStateFlow: StateFlow<Boolean> =
        dataStoreRepository.observeIsLogin().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepository.observeIsLogin().first()
            }
        )

    private var _backedGrades: List<Grade>? = null

    init {
        viewModelScope.launch {
            isLoginStateFlow.collect {
                if (it) {
                    getGrades()
                    getStudentRankingInfo()
                }
            }
        }
        viewModelScope.launch {
            isScreenshotModeStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isScreenshotMode = value)
                }
            }
        }
    }

    fun setIsGradeDetailDialogOpen(value: Boolean) {
        _uiState.update {
            it.copy(isGradeDetailDialogOpen = value)
        }
    }

    fun setSearchKeyword(keyword: String) {
        _uiState.update {
            it.copy(searchKeyword = keyword)
        }
        filterGrades()
    }

    fun setContentForGradeDetailDialog(value: Grade) {
        _uiState.update {
            it.copy(contentForGradeDetailDialog = value)
        }
    }

    suspend fun loadLineChart() {
        _uiState.update {
            it.copy(isLineChartLoading = true)
        }
        val gradesResult = networkRepository.getGrades()
        if (gradesResult != null) {
            val sorted =
                gradesResult.results.sortedBy { grade -> grade.yearAndSemester } + Grade()
            var flag = sorted.first().yearAndSemester
            val grades = mutableListOf<Grade>()
            val model = mutableListOf<FloatEntry>()
            var no = 1

            sorted.forEach {
                if (it.yearAndSemester != flag) {
                    model.add(FloatEntry(no.toFloat(), calculateGPA(grades).toFloat()))
                    grades.clear()
                    no++
                    flag = it.yearAndSemester
                }
                grades.add(it)
            }
            _uiState.update {
                it.copy(overallScoreData = model, isLineChartLoading = false)
            }
        }
    }

    private fun getScoreRangeIndex(score: Int): Int {
        return when (score) {
            in 0..59 -> 0
            in 60..69 -> 1
            in 70..79 -> 2
            in 80..89 -> 3
            in 90..100 -> 4
            else -> 0
        }
    }

    fun parseScoreRangeIndex(index: Int): String {
        return when (index) {
            0 -> "0-59"
            1 -> "60-69"
            2 -> "70-79"
            3 -> "80-89"
            4 -> "90-100"
            else -> "0-59"
        } + context.getString(R.string.score)
    }

    suspend fun getGrades() {
        _uiState.update {
            it.copy(isGradesLoading = true)
        }
        viewModelScope.launch {
            val gradesResult = networkRepository.getGrades()
            if (gradesResult != null) {
                val grades = gradesResult.results
                _backedGrades = grades
                val courseTypes = grades.map {
                    it.courseType
                }.toSet().toList()
                val semesters = grades.map { grade ->
                    grade.yearAndSemester ?: ""
                }.toSet().toList().sorted()
                _uiState.update {
                    it.copy(
                        courseTypes = courseTypes,
                        courseTypesSelected = courseTypes.map { sortName ->
                            sortName to true
                        }.toMutableStateMap(),
                        semesters = semesters,
                        semestersSelected = semesters.mapIndexed { index, semesterName ->
                            semesterName to (index == semesters.size - 1)
                        }.toMutableStateMap(),
                    )
                }
                filterGrades()
                _uiState.update {
                    it.copy(isGradesLoading = false)
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

    private fun updateGradeDistribution() {
        val tmp = mutableListOf(0, 0, 0, 0, 0)
        _uiState.value.grades.forEach {
            tmp[getScoreRangeIndex(it.score)]++
        }
        _uiState.update {
            it.copy(
                gradeDistribution = tmp
            )
        }
        // Log.d("GradeViewModel", "updateGradeDistribution: ${_uiState.value.gradeDistribution}")
    }

    private suspend fun getStudentRankingInfo() {
        _uiState.update {
            it.copy(isRankingInfoLoading = true)
        }
        val rankingInfo = networkRepository.getStudentRankingInfo(
            _uiState.value.semestersSelected.filterValues { isSelected ->
                isSelected
            }.keys,
        )
        if (rankingInfo != null) {
            _uiState.update {
                it.copy(
                    rankingInfo = rankingInfo,
                    isRankingInfoLoading = false,
                )
            }
            _uiState.update {
                it.copy(
                    entryModelForRankingColumnChart = generateChartModelForGrade(HostRankingType.GPA) + generateChartModelForGrade(
                        HostRankingType.SCORE
                    ),
                )
            }
        }
    }

    fun filterGrades() {
        _uiState.update {
            it.copy(
                grades = _backedGrades!!.filter { grade ->
                    grade.name.replace(" ", "").lowercase().contains(
                        it.searchKeyword.replace(" ", "").lowercase()
                    ) && it.courseTypesSelected.filterValues { isSelected -> isSelected }
                        .containsKey(
                            grade.courseType
                        ) && it.semestersSelected.filterValues { isSelected -> isSelected }
                        .containsKey(grade.yearAndSemester)
                },
                rankingAvailable = !it.courseTypesSelected.containsValue(false) && it.searchKeyword.isEmpty()
            )
        }
        sortGrades()
        updateGPA()
        updateAverageScore()
        updateGradeDistribution()
        viewModelScope.launch {
            getStudentRankingInfo()
        }
    }

    private fun generateChartModelForGrade(hostRankingType: HostRankingType): ChartEntryModel {
        return entryModelOf(
            SubRankingType.values().map { subRankingType ->
                _uiState.value.rankingInfo.getRanking(
                    hostRankingType,
                    subRankingType
                ).run {
                    entryOf(
                        subRankingType.ordinal,
                        if (this == null) 0 else 1 - this.ranking.toFloat() / this.total
                    )
                }
            }
        )
    }

    fun changeCourseSortSelected(courseSort: String, selected: Boolean) {
        _uiState.update {
            it.copy(
                courseTypesSelected = it.courseTypesSelected.toMutableMap().apply {
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

    fun setIsSearchBarShow(value: Boolean) {
        _uiState.update {
            it.copy(isSearchBarShow = value)
        }
    }

    fun setOpenFilterSheet(value: Boolean) {
        _uiState.update {
            it.copy(openFilterSheet = value)
        }
    }

    fun setOpenSummarySheet(value: Boolean) {
        _uiState.update {
            it.copy(openSummarySheet = value)
        }
    }

    fun setFabVisibility(value: Boolean) {
        _uiState.update {
            it.copy(fabVisibility = value)
        }
    }

    fun sortGradesBy(index: Int, sortBasisItem: SortBasisItem) {
        _uiState.update {
            it.copy(
                sortBasisList = it.sortBasisList.onEach {
                    if (it == sortBasisItem) {
                        if (!it.selected) {
                            it.setSelected(true)
                        } else if (!it.asc) {
                            it.setAsc(true)
                        } else {
                            it.setAsc(false)
                            it.setSelected(false)
                        }
                    } else {
                        it.setSelected(false)
                    }
                }
            )
        }
        filterGrades()
    }

    private fun sortGrades() {
        val found = _uiState.value.sortBasisList.find { it.selected }
        if (found != null && found.selected) {
            _uiState.update {
                it.copy(
                    grades = it.grades.sortedBy {
                        when (found.sortBasis) {
                            SortBasis.GRADE -> it.score.toDouble().times(if (found.asc) 1 else -1)
                            SortBasis.CREDIT -> it.credit.times(if (found.asc) 1 else -1)
                        }
                    },
                )
            }
        }
    }
}