package com.dart.campushelper.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_SELECTED_DARK_MODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    init {
        viewModelScope.launch {
            userPreferenceRepository.observeEnableSystemColor().collect { input ->
                _uiState.update { it.copy(enableSystemColor = input ?: false) }
            }
        }
        viewModelScope.launch {
            userPreferenceRepository.observeSelectedDarkMode().collect() { input ->
                _uiState.update { it.copy(darkMode = input) }
            }
        }
    }
}