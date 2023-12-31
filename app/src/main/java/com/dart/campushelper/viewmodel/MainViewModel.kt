package com.dart.campushelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.repo.DataStoreRepo
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

data class MainUiState(
    val isLogin: Boolean = false,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataStoreRepo: DataStoreRepo
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val isLoginStateFlow: StateFlow<Boolean> = dataStoreRepo.observeIsLogin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            dataStoreRepo.observeIsLogin().first()
        }
    )

    init {
        viewModelScope.launch {
            isLoginStateFlow.collect {
                // Log.d("MainViewModel", "isLogin: $it")
                _uiState.update { mainUiState ->
                    mainUiState.copy(isLogin = it)
                }
            }
        }
    }
}