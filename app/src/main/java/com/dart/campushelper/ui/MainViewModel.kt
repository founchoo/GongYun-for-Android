package com.dart.campushelper.ui

import android.util.Log
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.ChaoxingRepository
import com.dart.campushelper.data.UserPreferenceRepository
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
import javax.inject.Inject

data class MainUiState(
    val isLogin: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chaoxingRepository: ChaoxingRepository,
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val usernameStateFlow: StateFlow<String> = userPreferenceRepository.observeUsername()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observeUsername().first()
            }
        )

    val passwordStateFlow: StateFlow<String> = userPreferenceRepository.observePassword()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                userPreferenceRepository.observePassword().first()
            }
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

    private suspend fun getStuInfo() {
        val studentInfoResult = chaoxingRepository.getStudentInfo()
        Log.d("MainViewModel", "getStuInfo: ${studentInfoResult.status}")
        when (studentInfoResult.status) {
            Status.SUCCESS -> {
                MainActivity.userCache = studentInfoResult.data!!
                viewModelScope.launch {
                    userPreferenceRepository.changeIsLogin(true)
                }
            }
            Status.ERROR -> {
                val result = MainActivity.snackBarHostState.showSnackbar("应用预加载失败，请稍后重试", "重试")
                if (result == SnackbarResult.ActionPerformed) {
                    getStuInfo()
                }
            }
            Status.LOADING -> {}
        }
    }

    private suspend fun recoverLogin(username: String, password: String) {
        Log.d("MainViewModel", "recoverLogin: $username, $password")
        val loginResult = chaoxingRepository.login(username, password)
        Log.d("MainViewModel", "recoverLogin: ${loginResult.status}")
        when (loginResult.status) {
            Status.SUCCESS -> {
                getStuInfo()
            }
            Status.ERROR -> {
                val result = MainActivity.snackBarHostState.showSnackbar("应用预加载失败，请稍后重试", "重试")
                if (result == SnackbarResult.ActionPerformed) {
                    recoverLogin(username, password)
                }
            }
            Status.LOADING -> {}
        }
    }
}