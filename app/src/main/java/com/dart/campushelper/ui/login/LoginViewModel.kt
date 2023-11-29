package com.dart.campushelper.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.DataStoreRepository
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_PASSWORD
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_SEMESTER_YEAR_AND_NO
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.data.NetworkRepository
import com.dart.campushelper.utils.Constants.Companion.LOGIN_INFO_ERROR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class LoginUiState(
    val isShowLoginDialog: Boolean = false,
    val username: String = "",
    val password: String = "",
    val loginInfoError: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login() {
        viewModelScope.launch {
            val loginResource =
                networkRepository.login(_uiState.value.username, _uiState.value.password, true)
            // Log.d("LoginViewModel", loginResource.toString())
            if (loginResource.isSuccess) {
                val studentInfoResult = networkRepository.getStudentInfo()
                if (studentInfoResult != null) {
                    // Log.d("LoginViewModel", studentInfoResult.data!!.records[0].dataXnxq!!)
                    runBlocking {
                        dataStoreRepository.changeSemesterYearAndNo(studentInfoResult.data!!.records[0].dataXnxq!!)
                    }
                    // Log.d("LoginViewModel", studentInfoResult.data!!.records[0].rxnj!!)
                    runBlocking {
                        dataStoreRepository.changeEnterUniversityYear(studentInfoResult.data!!.records[0].rxnj!!)
                    }
                }
                runBlocking {
                    dataStoreRepository.changeUsername(_uiState.value.username)
                }
                runBlocking {
                    dataStoreRepository.changePassword(_uiState.value.password)
                }
                runBlocking {
                    dataStoreRepository.changeIsLogin(true)
                }
                _uiState.update { uiState ->
                    uiState.copy(isShowLoginDialog = false)
                }
            } else {
                if (loginResource.message == LOGIN_INFO_ERROR) {
                    _uiState.update { uiState ->
                        uiState.copy(loginInfoError = true)
                    }
                }
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
            dataStoreRepository.changeUsername(DEFAULT_VALUE_USERNAME)
            dataStoreRepository.changePassword(DEFAULT_VALUE_PASSWORD)
            dataStoreRepository.changeEnterUniversityYear(DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR)
            dataStoreRepository.changeSemesterYearAndNo(DEFAULT_VALUE_SEMESTER_YEAR_AND_NO)
            dataStoreRepository.changeIsLogin(false)
            dataStoreRepository.changeCookies(emptyList())
        }
    }
}