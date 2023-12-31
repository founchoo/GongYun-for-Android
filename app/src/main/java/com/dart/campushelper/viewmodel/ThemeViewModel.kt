package com.dart.campushelper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.repo.DataStoreRepo
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_SELECTED_DARK_MODE
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

data class ThemeUiState(
    val darkMode: Int = DEFAULT_VALUE_SELECTED_DARK_MODE,
    val enableSystemColor: Boolean = DEFAULT_VALUE_ENABLE_SYSTEM_COLOR,
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val dataStoreRepo: DataStoreRepo
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    private val enableSystemColorStateFlow =
        dataStoreRepo.observeEnableSystemColor().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                dataStoreRepo.observeEnableSystemColor().first()
            }
        )

    private val selectedDarkModeStateFlow =
        dataStoreRepo.observeSelectedDarkMode().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                dataStoreRepo.observeSelectedDarkMode().first()
            }
        )

    init {
        viewModelScope.launch {
            enableSystemColorStateFlow.collect { input ->
                _uiState.update { it.copy(enableSystemColor = input) }
            }
        }
        viewModelScope.launch {
            selectedDarkModeStateFlow.collect { input ->
                _uiState.update { it.copy(darkMode = input) }
            }
        }
    }
}

enum class DarkMode {
    SYSTEM,
    ON,
    OFF,
}

fun DarkMode.toStringResourceId(): Int {
    return when (this) {
        DarkMode.SYSTEM -> R.string.follow_system
        DarkMode.ON -> R.string.turn_on
        DarkMode.OFF -> R.string.turn_off
    }
}