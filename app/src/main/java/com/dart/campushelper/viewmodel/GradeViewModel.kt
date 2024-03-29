package com.dart.campushelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.R
import com.dart.campushelper.model.CourseType
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.model.SimplGrade
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.repo.DataStoreRepo
import com.dart.campushelper.repo.NetworkRepo
import com.dart.campushelper.repo.Result
import com.dart.campushelper.repo.SimplGradeRepo
import com.dart.campushelper.ui.main.MainActivity
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

data class YearAndSemester(
    val value: String,
    val selected: Boolean
)

enum class SortBasis {
    SEMESTER,
    GRADE,
    CREDIT;

    fun getResId(): Int {
        return when (this) {
            SEMESTER -> R.string.semester
            GRADE -> R.string.grade_label
            CREDIT -> R.string.credit_label
        }
    }
}

data class SortBasisItem(
    val sortBasis: SortBasis,
    val selected: Boolean,
    val asc: Boolean
) {
    constructor(sortBasis: SortBasis) : this(sortBasis, false, false)
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
    val sortBasisList: List<SortBasisItem>,
    val startYearAndSemester: String? = null,
)

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val networkRepo: NetworkRepo,
    private val dataStoreRepo: DataStoreRepo,
    private val simplGradeRepo: SimplGradeRepo
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        GradeUiState(
            sortBasisList = getInitSortBasisList()
        )
    )
    val uiState: StateFlow<GradeUiState> = _uiState.asStateFlow()

    private val isScreenshotModeStateFlow: StateFlow<Boolean> =
        dataStoreRepo.observeIsScreenshotMode().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepo.observeIsScreenshotMode().first()
            }
        )

    private val enterUniversityYearStateFlow =
        dataStoreRepo.observeEnterUniversityYear().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepo.observeEnterUniversityYear().first()
            }
        )

    private val isLoginStateFlow = dataStoreRepo.observeIsLogin().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeIsLogin().first()
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
        viewModelScope.launch {
            isLoginStateFlow.collect {
                if (it) {
                    getGrades()
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

    fun updateGradeReadStatus(grade: Grade) {
        if (!grade.isRead) {
            viewModelScope.launch {
                simplGradeRepo.update(grade.courseId.orEmpty(), true)
                getGrades()
            }
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
        } + context.getString(R.string.score)
    }

    suspend fun getGrades() {
        val needResetFilter = _backedGrades == null
        _backedGrades = networkRepo.getGrades()
        val localGrades = simplGradeRepo.getAll()
        val gradesToAdd = mutableListOf<SimplGrade>()
        _backedGrades?.forEach { grade ->
            val found = localGrades.find { it.gradeId == grade.courseId }
            if (found == null) {
                gradesToAdd.add(SimplGrade(grade.courseId.orEmpty()))
            }
            grade.isRead = found?.isRead ?: false
        }
        if (gradesToAdd.isNotEmpty()) {
            simplGradeRepo.insertAll(*gradesToAdd.toTypedArray())
        }
        val courseTypes = networkRepo.getCourseTypeList()
        _uiState.update {
            it.copy(courseTypes = _backedGrades?.asSequence()?.map { it.courseTypeRaw }
                ?.filterNotNull()?.toSet()
                ?.map { courseTypeValue ->
                    CourseType(
                        courseTypeValue,
                        courseTypes?.find { it.value == courseTypeValue }?.label
                            ?: "$courseTypeValue${context.getString(R.string.unidentified)}",
                        true
                    )
                }
                ?.sortedBy { it.value }?.toList()
            )
        }
        if (needResetFilter) {
            resetFilter()
        }
        filterGrades()
    }

    fun resetFilter() {
        val semesters = _backedGrades?.map {
            it.yearAndSemester ?: ""
        }?.toSet()?.toList()?.sorted()
        _uiState.update {
            it.copy(
                semesters = semesters?.map { semester ->
                    YearAndSemester(semester, true)
                },
                courseTypes = it.courseTypes?.map { courseType ->
                    courseType.copy(selected = true)
                },
                sortBasisList = getInitSortBasisList()
            )
        }
    }

    private fun getInitSortBasisList(): List<SortBasisItem> {
        return SortBasis.values().map {
            if (it == SortBasis.SEMESTER) {
                SortBasisItem(it, true, false)
            } else {
                SortBasisItem(it)
            }
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
            val rankingInfo = networkRepo.getStudentRankingInfo(it)
            _uiState.update {
                it.copy(
                    rankingInfo = Result(rankingInfo),
                )
            }
            _uiState.update {
                it.copy(
                    entryModelForRankingColumnChart = generateChartModelForGrade(HostRankingType.GPA) +
                            generateChartModelForGrade(HostRankingType.SCORE),
                )
            }
        }
    }

    fun filterGrades() {
        val formattedSearchKeyword = _uiState.value.searchKeyword.replace(" ", "").lowercase()
        val selectedCourseTypes = _uiState.value.courseTypes?.filter { it.selected }
            ?.map { it.value }
        val selectedYearAndSemesters =
            _uiState.value.semesters?.filter { it.selected }?.map { it.value }
        _uiState.update {
            it.copy(
                grades = Result(_backedGrades?.filter { grade ->
                    grade.name.replace(" ", "").lowercase().contains(formattedSearchKeyword) &&
                            (_uiState.value.courseTypes?.map { it.value }
                                ?.contains(
                                    grade.courseTypeRaw
                                ) == false ||
                                    selectedCourseTypes?.contains(
                                        grade.courseTypeRaw
                                    ) == true) &&
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

    fun switchCourseSortSelected(item: CourseType) {
        _uiState.update {
            it.copy(
                courseTypes = it.courseTypes?.map {
                    if (it == item) {
                        it.copy(selected = !it.selected)
                    } else {
                        it
                    }
                }
            )
        }
        filterGrades()
    }

    fun switchSemesterSelected(item: YearAndSemester) {
        _uiState.update {
            it.copy(
                semesters = it.semesters?.map {
                    if (it == item) {
                        it.copy(selected = !it.selected)
                    } else {
                        it
                    }
                }
            )
        }
        filterGrades()
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

    fun sortGradesBy(sortBasisItem: SortBasisItem) {
        _uiState.update {
            it.copy(
                sortBasisList = it.sortBasisList.map {
                    if (it == sortBasisItem) {
                        if (!it.selected) {
                            it.copy(selected = true)
                        } else if (!it.asc) {
                            it.copy(asc = true)
                        } else {
                            it.copy(selected = false, asc = false)
                        }
                    } else {
                        it.copy(selected = false)
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
                            SortBasis.SEMESTER -> it.yearAndSemester?.replace("-", "")?.toDouble()
                                ?.times(if (found.asc) 1 else -1)

                            SortBasis.GRADE -> it.score.toDouble().times(if (found.asc) 1 else -1)
                            SortBasis.CREDIT -> it.credit.times(if (found.asc) 1 else -1)
                        }
                    }),
                )
            }
        }
    }

    fun switchAllSemesterSelected() {
        val futureSelected = _uiState.value.semesters?.find { !it.selected } != null
        _uiState.update {
            it.copy(
                semesters = _uiState.value.semesters?.map {
                    it.copy(selected = futureSelected)
                }
            )
        }
        filterGrades()
    }

    fun switchAllCourseTypeSelected() {
        val futureSelected = _uiState.value.courseTypes?.find { !it.selected } != null
        _uiState.update {
            it.copy(
                courseTypes = _uiState.value.courseTypes?.map {
                    it.copy(selected = futureSelected)
                }
            )
        }
        filterGrades()
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _backedGrades?.forEach {
                simplGradeRepo.update(it.courseId.orEmpty(), true)
            }
            getGrades()
            MainActivity.snackBarHostState.showSnackbar(context.getString(R.string.mark_all_as_read_success))
        }
    }
}