package com.dart.campushelper.ui.grade

import android.util.Log
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.toMutableStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.RankingInfo
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.network.Status
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
    val showLoginPlaceholder: Boolean = false,
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

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(GradesUiState())
    val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()

    private val usernameStateFlow: StateFlow<String> = userPreferenceRepository.observeUsername()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeUsername().first()
            }
        )

    private val isLoginStateFlow: StateFlow<Boolean> = userPreferenceRepository.observeIsLogin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            userPreferenceRepository.observeIsLogin().first()
        }
    )

    private val enterUniversityYearStateFlow = userPreferenceRepository.observeEnterUniversityYear().stateIn(
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
                Log.d("GradeViewModel", "observeIsLogin: $it")
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

    suspend fun getGrades() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isGradesLoading = true)
            }
            val gradesResult = chaoxingRepository.getGrades()
            when (gradesResult.status) {
                Status.SUCCESS -> {
                    val grades = gradesResult.data!!.results
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
                Status.ERROR -> {
                    val result = MainActivity.snackBarHostState.showSnackbar("加载成绩失败，请稍后重试", "重试")
                    if (result == SnackbarResult.ActionPerformed) {
                        getGrades()
                    }
                }
                Status.INVALID -> {

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
        when (stuRankInfoResult.status) {
            Status.SUCCESS -> {
                val rankingInfo = RankingInfo()
                Jsoup.parse(stuRankInfoResult.data!!).run {
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
            Status.ERROR -> {
                val result = MainActivity.snackBarHostState.showSnackbar("加载排名失败", "重试")
                if (result == SnackbarResult.ActionPerformed) {
                    getStudentRankingInfo()
                }
            }
            Status.INVALID -> {

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