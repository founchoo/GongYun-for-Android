package com.dart.campushelper.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.App.Companion.instance
import com.dart.campushelper.R
import com.dart.campushelper.data.DataStoreRepository
import com.dart.campushelper.data.NetworkRepository
import com.dart.campushelper.data.Result
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.utils.AcademicYearAndSemester
import com.patrykandpatrick.vico.core.chart.composed.ComposedChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.extension.setFieldValue
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

data class YearAndSemester(
    @StringRes val yearResId: Int,
    @StringRes val semesterResId: Int,
    val value: String,
    var selected: Boolean
)

data class CourseType(val value: String, val label: String, val selected: Boolean)

enum class SortBasis {
    GRADE,
    CREDIT;

    override fun toString(): String {
        return when (this) {
            GRADE -> instance.getString(R.string.grade_label)
            CREDIT -> instance.getString(R.string.credit_label)
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
    val rankingInfo: Result<RankingInfo> = Result(),
    val grades: Result<List<Grade>> = Result(),
    val courseTypes: List<CourseType>? = null,
    val semesters: List<YearAndSemester>? = null,
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
    },
    val startYearAndSemester: String? = null,
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

    private val enterUniversityYearStateFlow =
        dataStoreRepository.observeEnterUniversityYear().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepository.observeEnterUniversityYear().first()
            }
        )

    private var _backedGrades: List<Grade>? = null

    init {
        viewModelScope.launch {
            isScreenshotModeStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isScreenshotMode = value)
                }
            }
        }
        viewModelScope.launch {
            enterUniversityYearStateFlow.collect { value ->
                if (value.toIntOrNull() != null) {
                    _uiState.update { it.copy(startYearAndSemester = "${value}-${value.toInt() + 1}-1") }
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

    fun loadLineChart() {
        if (_backedGrades != null) {
            val sorted =
                _backedGrades!!.sortedBy { grade -> grade.yearAndSemester }.plus(Grade())
            var flag = sorted.first().yearAndSemester
            val grades = mutableListOf<Grade>()
            val model = mutableListOf<FloatEntry>()
            var no = 1

            sorted.forEach {
                if (it.yearAndSemester != flag) {
                    model.add(FloatEntry(no.toFloat(), calculateGPA(grades)!!.toFloat()))
                    grades.clear()
                    no++
                    flag = it.yearAndSemester
                }
                grades.add(it)
            }
            _uiState.update {
                it.copy(overallScoreData = model)
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
        } + instance.getString(R.string.score)
    }

    suspend fun getGrades() {
        val firstLoad = _backedGrades == null
        _backedGrades = null
        viewModelScope.launch {
            _uiState.update {
                it.copy(courseTypes = networkRepository.getCourseTypeList())
            }
            val gradesResult = networkRepository.getGrades()
            _backedGrades = gradesResult
            if (firstLoad) resetFilter()
            filterGrades()
        }
    }

    fun resetFilter() {
        val semesters = _backedGrades?.map { grade ->
            grade.yearAndSemester ?: ""
        }?.toSet()?.toList()?.sorted()
        _uiState.update {
            it.copy(
                semesters = semesters?.map {
                    YearAndSemester(
                        AcademicYearAndSemester.getYearResId(
                            semesters.first(),
                            it
                        ),
                        AcademicYearAndSemester.getSemesterResId(it),
                        it,
                        it == semesters.last()
                    )
                },
                courseTypes = it.courseTypes?.map { courseType ->
                    courseType.copy(selected = true)
                },
                sortBasisList = SortBasis.values().map { SortBasisItem(it) }
            )
        }
    }

    private fun calculateGPA(grades: List<Grade>?): Double? = grades?.let {
        grades.sumOf { grade ->
            (grade.score / 10.0 - 5) * grade.credit
        } / grades.sumOf { grade ->
            grade.credit
        }
    }

    private fun calculateAverageScore(grades: List<Grade>?): Double? = grades?.let {
        grades.sumOf { grade ->
            grade.score.toDouble()
        } / grades.size
    }

    private fun updateGPA() {
        _uiState.update {
            it.copy(
                gradePointAverage = calculateGPA(it.grades.data) ?: Double.NaN,
            )
        }
    }

    private fun updateAverageScore() {
        _uiState.update {
            it.copy(
                averageScore = calculateAverageScore(it.grades.data) ?: Double.NaN,
            )
        }
    }

    private fun updateGradeDistribution() {
        val tmp = mutableListOf(0, 0, 0, 0, 0)
        _uiState.value.grades.data?.forEach {
            tmp[getScoreRangeIndex(it.score)]++
        }
        _uiState.update {
            it.copy(
                gradeDistribution = tmp
            )
        }
    }

    suspend fun getRankingInfo() {
        _uiState.value.semesters?.filter { it.selected }?.map { it.value }?.let {
            val rankingInfo = networkRepository.getStudentRankingInfo(it)
            _uiState.update {
                it.copy(
                    rankingInfo = Result(rankingInfo),
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
        val formattedSearchKeyword = _uiState.value.searchKeyword.replace(" ", "").lowercase()
        val selectedCourseTypes = _uiState.value.courseTypes?.filter { it.selected }
            ?.map { it.value.toString() }
        val selectedYearAndSemesters =
            _uiState.value.semesters?.filter { it.selected }?.map { it.value }
        _uiState.update {
            it.copy(
                grades = Result(_backedGrades?.filter { grade ->
                    grade.name.replace(" ", "").lowercase().contains(formattedSearchKeyword) &&
                            selectedCourseTypes?.contains(grade.courseTypeRaw) == true &&
                            selectedYearAndSemesters?.contains(grade.yearAndSemester) == true
                }),
                rankingAvailable = it.courseTypes?.find { !it.selected } == null && it.searchKeyword.isEmpty()
            )
        }
        sortGrades()
        updateGPA()
        updateAverageScore()
        updateGradeDistribution()
    }

    private fun generateChartModelForGrade(hostRankingType: HostRankingType): ChartEntryModel {
        return entryModelOf(
            SubRankingType.values().map { subRankingType ->
                _uiState.value.rankingInfo.data?.getRanking(
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

    fun changeCourseSortSelected(index: Int, selected: Boolean) {
        _uiState.update {
            it.copy(
                courseTypes = it.courseTypes.apply {
                    this?.get(index)?.setFieldValue("selected", selected)
                }
            )
        }
    }

    fun changeSemesterSelected(index: Int, selected: Boolean) {
        _uiState.update {
            it.copy(
                semesters = it.semesters?.apply {
                    this[index].selected = selected
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
                    grades = Result(it.grades.data?.sortedBy {
                        when (found.sortBasis) {
                            SortBasis.GRADE -> it.score.toDouble().times(if (found.asc) 1 else -1)
                            SortBasis.CREDIT -> it.credit.times(if (found.asc) 1 else -1)
                        }
                    }),
                )
            }
        }
    }
}