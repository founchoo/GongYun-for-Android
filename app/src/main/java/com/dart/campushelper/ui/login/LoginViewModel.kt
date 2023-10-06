package com.dart.campushelper.ui.login

import android.util.Log
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES
import com.dart.campushelper.model.getSemesterStartLocalDate
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.utils.Constants.Companion.LOGIN_INFO_ERROR
import com.dart.campushelper.utils.Constants.Companion.NETWORK_CONNECT_ERROR
import com.dart.campushelper.utils.Constants.Companion.RETRY
import com.dart.campushelper.utils.network.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import javax.inject.Inject

data class LoginUiState(
    val isShowLoginDialog: Boolean = false,
    val username: String = "",
    val password: String = "",
    val loginInfoError: Boolean = false
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
            val loginResource =
                chaoxingRepository.login(_uiState.value.username, _uiState.value.password)
            when (loginResource.status) {
                Status.SUCCESS -> {
                    val studentInfoResult = chaoxingRepository.getStudentInfo()
                    if (studentInfoResult.status == Status.SUCCESS) {
                        Log.d("LoginViewModel", studentInfoResult.data!!.data!!.records[0].dataXnxq!!)
                        runBlocking {
                            userPreferenceRepository.changeSemesterYearAndNo(studentInfoResult.data!!.data!!.records[0].dataXnxq!!)
                        }
                        Log.d("LoginViewModel", studentInfoResult.data!!.data!!.records[0].rxnj!!)
                        runBlocking {
                            userPreferenceRepository.changeEnterUniversityYear(studentInfoResult.data!!.data!!.records[0].rxnj!!)
                        }
                    }
                    val weekInfoResponseResource = chaoxingRepository.getWeekInfo()
                    if (weekInfoResponseResource.status == Status.SUCCESS) {
                        Log.d("LoginViewModel", weekInfoResponseResource.data!!.getSemesterStartLocalDate().toString())
                        runBlocking {
                            userPreferenceRepository.changeStartLocalDate(
                                weekInfoResponseResource.data!!.getSemesterStartLocalDate()!!
                            )
                        }
                    }
                    runBlocking {
                        userPreferenceRepository.changeUsername(_uiState.value.username)
                    }
                    runBlocking {
                        userPreferenceRepository.changePassword(_uiState.value.password)
                    }
                    runBlocking {
                        userPreferenceRepository.changeIsLogin(true)
                    }
                    _uiState.update { uiState ->
                        uiState.copy(isShowLoginDialog = false)
                    }
                }

                Status.ERROR -> {
                    userPreferenceRepository.changeIsLogin(false)
                    if (loginResource.message == LOGIN_INFO_ERROR) {
                        _uiState.update {
                            it.copy(loginInfoError = true)
                        }
                    } else {
                        val result = MainActivity.snackBarHostState.showSnackbar(
                            NETWORK_CONNECT_ERROR,
                            RETRY
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            login()
                        }
                    }
                }

                Status.INVALID -> {}
            }
        }
    }

    fun onUsernameChanged(input: String) {
        _uiState.update {
            it.copy(loginInfoError = false, username = input)
        }
    }

    fun onPasswordChanged(input: String) {
        _uiState.update {
            it.copy(loginInfoError = false, password = input)
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
            userPreferenceRepository.changeUsername(VALUES.DEFAULT_VALUE_USERNAME)
            userPreferenceRepository.changePassword(VALUES.DEFAULT_VALUE_PASSWORD)
            userPreferenceRepository.changeEnterUniversityYear(VALUES.DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR)
            userPreferenceRepository.changeSemesterYearAndNo(VALUES.DEFAULT_VALUE_SEMESTER_YEAR_AND_NO)
            userPreferenceRepository.changeIsLogin(false)
            userPreferenceRepository.changeStartLocalDate(LocalDate.now())
            userPreferenceRepository.changeCookies(emptyList())
        }
    }
}