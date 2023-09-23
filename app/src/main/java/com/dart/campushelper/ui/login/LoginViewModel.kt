package com.dart.campushelper.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.api.ChaoxingService
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES
import com.dart.campushelper.ui.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class LoginUiState(
    val isShowLoginDialog: Boolean = false,
    val username: String = "",
    val password: String = "",
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login() {
        viewModelScope.launch {
            chaoxingRepository.login(_uiState.value.username, _uiState.value.password)
                .collect { it ->
                    if (it.code() == 302) {
                        chaoxingRepository.getStudentInfo().collect { studentInfoResponse ->
                            val studentInfo = studentInfoResponse.body()?.data?.records?.get(0)
                            MainActivity.userCache.semesterYearAndNo = studentInfo?.dataXnxq ?: ""
                            MainActivity.userCache.enterUniversityYear = studentInfo?.rxnj ?: ""
                            MainActivity.userCache.studentId = studentInfo?.xh ?: ""
                        }
                        chaoxingRepository.getWeekInfo().collect { weekInfoResponse ->
                            val startWeekMonthAndDay = weekInfoResponse.body()?.data?.first()?.date
                            if (startWeekMonthAndDay != null) {
                                var month = startWeekMonthAndDay.split("-")[0]
                                if (month.length == 1) {
                                    month = "0$month"
                                }
                                var day = startWeekMonthAndDay.split("-")[1]
                                if (day.length == 1) {
                                    day = "0$day"
                                }
                                userPreferenceRepository.changeStartLocalDate(
                                    LocalDate.parse("${LocalDate.now().year}-$month-$day")
                                )
                            }
                        }
                        userPreferenceRepository.changeUsername(_uiState.value.username)
                        userPreferenceRepository.changePassword(_uiState.value.password)
                        userPreferenceRepository.changeIsLogin(true)
                        _uiState.update { uiState ->
                            uiState.copy(isShowLoginDialog = false)
                        }
                        MainActivity.snackBarHostState.showSnackbar("登录成功")
                    } else {
                        userPreferenceRepository.changeIsLogin(false)
                        MainActivity.snackBarHostState.showSnackbar("登录失败")
                    }
                }
        }
    }

    fun onUsernameChanged(input: String) {
        _uiState.update {
            it.copy(username = input)
        }
    }

    fun onPasswordChanged(input: String) {
        _uiState.update {
            it.copy(password = input)
        }
    }

    fun onHideLoginDialogRequest() {
        _uiState.update {
            it.copy(isShowLoginDialog = false)
        }
    }

    fun onShowLoginDialogRequest() {
        _uiState.update {
            it.copy(isShowLoginDialog = true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferenceRepository.changeIsLogin(false)
        }
        viewModelScope.launch {
            MainActivity.userCache.reset()
            userPreferenceRepository.changeUsername(VALUES.DEFAULT_VALUE_USERNAME)
            userPreferenceRepository.changePassword(VALUES.DEFAULT_VALUE_PASSWORD)
            userPreferenceRepository.changeStartLocalDate(LocalDate.now())
            ChaoxingService.cookies.emit(emptyList())
        }
    }
}