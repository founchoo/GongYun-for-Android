package com.dart.campushelper.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_PASSWORD
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.utils.ResponseErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class MainUiState(
    val isLogin: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    private val mutex = Mutex()

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val usernameStateFlow: StateFlow<String> = userPreferenceRepository.observeUsername()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_VALUE_USERNAME
        )

    val passwordStateFlow: StateFlow<String> = userPreferenceRepository.observePassword()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_VALUE_PASSWORD
        )

    init {
        viewModelScope.launch {
            userPreferenceRepository.observeIsLogin().collect { input ->
                Log.d("MainViewModel", "isLogin: $input")
                _uiState.update {
                    it.copy(isLogin = input)
                }
            }
        }
        viewModelScope.launch {
            combine(
                usernameStateFlow,
                passwordStateFlow
            ) { username, password ->
                listOf(username, password)
            }.collect {
                Log.d("MainViewModel", "combine: $it")
                if (!_uiState.value.isLogin && it[0].isNotEmpty() && it[1].isNotEmpty()) {
                    recoverLogin(it[0], it[1])
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferenceRepository.changeIsLogin(false)
        }
    }

    private suspend fun recoverLogin(username: String, password: String) {
        mutex.withLock {
            chaoxingRepository.login(username, password)
                .collect { response ->
                    ResponseErrorHandler(
                        response = response,
                        scope = viewModelScope,
                        responseSuccessCode = 302,
                        ignoreResponseNull = true,
                        actionWhenResponseSuccess = {
                            viewModelScope.launch {
                                chaoxingRepository.getStudentInfo()
                                    .collect { studentInfoResponse ->
                                        ResponseErrorHandler(
                                            response = studentInfoResponse,
                                            scope = viewModelScope,
                                            actionWhenResponseSuccess = {
                                                val studentInfo =
                                                    studentInfoResponse.body()?.data?.records?.get(
                                                        0
                                                    )
                                                MainActivity.userCache.semesterYearAndNo =
                                                    studentInfo?.dataXnxq ?: ""
                                                MainActivity.userCache.enterUniversityYear =
                                                    studentInfo?.rxnj ?: ""
                                                MainActivity.userCache.studentId =
                                                    studentInfo?.xh ?: ""
                                                viewModelScope.launch {
                                                    userPreferenceRepository.changeIsLogin(true)
                                                }
                                            }
                                        )
                                    }
                            }
                        },
                    )
                }
        }
    }
}