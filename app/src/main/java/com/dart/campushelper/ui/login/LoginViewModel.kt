package com.dart.campushelper.ui.login

import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.api.ChaoxingService
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES
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
                        MainActivity.userCache = studentInfoResult.data!!
                    }
                    val semesterStartLocalDateResult = chaoxingRepository.getSemesterStartLocalDate()
                    if (semesterStartLocalDateResult.status == Status.SUCCESS) {
                        userPreferenceRepository.changeStartLocalDate(
                            semesterStartLocalDateResult.data!!
                        )
                    }
                    userPreferenceRepository.changeUsername(_uiState.value.username)
                    userPreferenceRepository.changePassword(_uiState.value.password)
                    userPreferenceRepository.changeIsLogin(true)
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
                        val result = MainActivity.snackBarHostState.showSnackbar(NETWORK_CONNECT_ERROR, RETRY)
                        if (result == SnackbarResult.ActionPerformed) {
                            login()
                        }
                    }
                }
                Status.LOADING -> {}
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
            MainActivity.userCache.reset()
            userPreferenceRepository.changeIsLogin(false)
            userPreferenceRepository.changeStartLocalDate(LocalDate.now())
            ChaoxingService.cookies.emit(emptyList())
        }
    }
}