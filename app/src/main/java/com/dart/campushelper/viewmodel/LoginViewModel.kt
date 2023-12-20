package com.dart.campushelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.repo.DataStoreRepo
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_PASSWORD
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_YEAR_AND_SEMESTER
import com.dart.campushelper.repo.DataStoreRepo.Companion.MOCK_VALUE_PASSWORD
import com.dart.campushelper.repo.DataStoreRepo.Companion.MOCK_VALUE_USERNAME
import com.dart.campushelper.repo.NetworkRepo
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
    val loginResponse: Boolean? = true,
    val inputEnabled: Boolean = true,
    val isLoading: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val networkRepo: NetworkRepo,
    private val dataStoreRepo: DataStoreRepo
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun fillMockInfo() {
        _uiState.update { it.copy(inputEnabled = false) }
        onUsernameChanged(MOCK_VALUE_USERNAME)
        onPasswordChanged(MOCK_VALUE_PASSWORD)
    }

    fun clearMockInfo() {
        _uiState.update { it.copy(inputEnabled = true) }
        onUsernameChanged("")
        onPasswordChanged("")
    }

    fun login() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            when (val response =
                networkRepo.login(_uiState.value.username, _uiState.value.password)) {
                true -> {
                    runBlocking {
                        dataStoreRepo.changeUsername(_uiState.value.username)
                    }
                    runBlocking {
                        dataStoreRepo.changePassword(_uiState.value.password)
                    }
                    val studentInfoResult = networkRepo.getStudentInfo()
                    if (studentInfoResult != null) {
                        runBlocking {
                            dataStoreRepo.changeSemesterYearAndNo(studentInfoResult.dataXnxq!!)
                        }
                        runBlocking {
                            dataStoreRepo.changeEnterUniversityYear(studentInfoResult.rxnj!!)
                        }
                    }
                    runBlocking {
                        dataStoreRepo.changeIsLogin(true)
                    }
                    _uiState.update { uiState ->
                        uiState.copy(isShowLoginDialog = false)
                    }
                }

                else -> {
                    _uiState.update { uiState ->
                        uiState.copy(loginResponse = response)
                    }
                }
            }
            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    fun onUsernameChanged(input: String) {
        _uiState.update {
            it.copy(loginResponse = true, username = input)
        }
    }

    fun onPasswordChanged(input: String) {
        _uiState.update {
            it.copy(loginResponse = true, password = input)
        }
    }

    fun onHideLoginDialogRequest() {
        _uiState.update {
            it.copy(isShowLoginDialog = false)
        }
    }

    fun onShowLoginDialogRequest() {
        clearMockInfo()
        _uiState.update {
            it.copy(isShowLoginDialog = true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreRepo.changeUsername(DEFAULT_VALUE_USERNAME)
            dataStoreRepo.changePassword(DEFAULT_VALUE_PASSWORD)
            dataStoreRepo.changeEnterUniversityYear(DEFAULT_VALUE_ENTER_UNIVERSITY_YEAR)
            dataStoreRepo.changeSemesterYearAndNo(DEFAULT_VALUE_YEAR_AND_SEMESTER)
            dataStoreRepo.changeIsLogin(false)
            dataStoreRepo.changeCookies(emptyList())
        }
    }
}