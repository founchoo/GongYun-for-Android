package com.dart.campushelper.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_SELECTED_DARK_MODE
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
    val darkMode: String = DEFAULT_VALUE_SELECTED_DARK_MODE,
    val enableSystemColor: Boolean = DEFAULT_VALUE_ENABLE_SYSTEM_COLOR,
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    private val enableSystemColorStateFlow =
        userPreferenceRepository.observeEnableSystemColor().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                userPreferenceRepository.observeEnableSystemColor().first()
            }
        )

    private val selectedDarkModeStateFlow =
        userPreferenceRepository.observeSelectedDarkMode().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                userPreferenceRepository.observeSelectedDarkMode().first()
            }
        )

    init {
        viewModelScope.launch {
            enableSystemColorStateFlow.collect { input ->
                _uiState.update { it.copy(enableSystemColor = input ?: false) }
            }
        }
        viewModelScope.launch {
            selectedDarkModeStateFlow.collect { input ->
                _uiState.update { it.copy(darkMode = input) }
            }
        }
    }
}