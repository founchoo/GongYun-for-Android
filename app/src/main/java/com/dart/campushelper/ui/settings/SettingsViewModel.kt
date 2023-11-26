package com.dart.campushelper.ui.settings

import android.appwidget.AppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.BuildConfig
import com.dart.campushelper.CampusHelperApplication
import com.dart.campushelper.data.UserPreferenceRepository
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_IS_LOGIN
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_SELECTED_DARK_MODE
import com.dart.campushelper.data.VALUES.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.ui.pin
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

data class SettingsUiState(
    val isLogin: Boolean = DEFAULT_VALUE_IS_LOGIN,
    val username: String = DEFAULT_VALUE_USERNAME,
    val enableSystemColor: Boolean = DEFAULT_VALUE_ENABLE_SYSTEM_COLOR,
    val selectedDarkMode: String = DEFAULT_VALUE_SELECTED_DARK_MODE,
    val isOtherCourseDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isYearDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isDateDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isTimeDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val openLogoutConfirmDialog: Boolean = false,
    val openFeedbackUrlConfirmDialog: Boolean = false,
    val openSourceCodeUrlConfirmDialog: Boolean = false,
    val appVersion: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferenceRepository: UserPreferenceRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appVersion = BuildConfig.VERSION_NAME
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val usernameStateFlow = userPreferenceRepository.observeUsername().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeUsername().first()
        }
    )

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

    private val isOtherCourseDisplayStateFlow = userPreferenceRepository.observeIsOtherCourseDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeIsOtherCourseDisplay().first()
        }
    )

    private val isYearDisplayStateFlow = userPreferenceRepository.observeIsYearDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeIsYearDisplay().first()
        }
    )

    private val isDateDisplayStateFlow = userPreferenceRepository.observeIsDateDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeIsDateDisplay().first()
        }
    )

    private val isTimeDisplayStateFlow = userPreferenceRepository.observeIsTimeDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            userPreferenceRepository.observeIsTimeDisplay().first()
        }
    )

    private val isLoginStateFlow: StateFlow<Boolean> = userPreferenceRepository.observeIsLogin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            userPreferenceRepository.observeIsLogin().first()
        }
    )

    init {
        viewModelScope.launch {
            usernameStateFlow.collect { value ->
                _uiState.update {
                    it.copy(username = value)
                }
            }
        }
        viewModelScope.launch {
            isLoginStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isLogin = value)
                }
            }
        }
        viewModelScope.launch {
            enableSystemColorStateFlow.collect { value ->
                _uiState.update {
                    it.copy(enableSystemColor = value ?: false)
                }
            }
        }
        viewModelScope.launch {
            selectedDarkModeStateFlow.collect { value ->
                _uiState.update {
                    it.copy(selectedDarkMode = value)
                }
            }
        }
        viewModelScope.launch {
            isOtherCourseDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isOtherCourseDisplay = value)
                }
            }
        }
        viewModelScope.launch {
            isYearDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isYearDisplay = value)
                }
            }
        }
        viewModelScope.launch {
            isDateDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isDateDisplay = value)
                }
            }
        }
        viewModelScope.launch {
            isTimeDisplayStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isTimeDisplay = value)
                }
            }
        }
    }

    fun clearCookies() {
        viewModelScope.launch {
            userPreferenceRepository.changeCookies(emptyList())
            MainActivity.snackBarHostState.showSnackbar("Cookies 已清除，请开始调试")
        }
    }

    fun changeEnableSystemColor(enable: Boolean) {
        viewModelScope.launch {
            userPreferenceRepository.changeEnableSystemColor(enable)
        }
    }

    fun changeSelectedDarkMode(darkMode: String) {
        viewModelScope.launch {
            userPreferenceRepository.changeSelectedDarkMode(darkMode)
        }
    }

    fun pin() {
        val widgetManager = AppWidgetManager.getInstance(CampusHelperApplication.context)
        // Get a list of our app widget providers to retrieve their info
        val widgetProviders =
            widgetManager.getInstalledProvidersForPackage(
                CampusHelperApplication.context.packageName,
                null
            )
        widgetProviders[0].pin(CampusHelperApplication.context)
    }

    fun changeIsOtherCourseDisplay(isOtherCourseDisplay: Boolean) {
        viewModelScope.launch {
            userPreferenceRepository.changeIsOtherCourseDisplay(isOtherCourseDisplay)
        }
    }

    fun changeIsYearDisplay(isYearDisplay: Boolean) {
        viewModelScope.launch {
            userPreferenceRepository.changeIsYearDisplay(isYearDisplay)
        }
    }

    fun changeIsDateDisplay(isDateDisplay: Boolean) {
        viewModelScope.launch {
            userPreferenceRepository.changeIsDateDisplay(isDateDisplay)
        }
    }

    fun changeIsTimeDisplay(isTimeDisplay: Boolean) {
        viewModelScope.launch {
            userPreferenceRepository.changeIsTimeDisplay(isTimeDisplay)
        }
    }

    fun onShowLogoutConfirmDialogRequest() {
        _uiState.update {
            it.copy(openLogoutConfirmDialog = true)
        }
    }

    fun onHideLogoutConfirmDialogRequest() {
        _uiState.update {
            it.copy(openLogoutConfirmDialog = false)
        }
    }

    fun onShowFeedbackUrlConfirmDialogRequest() {
        _uiState.update {
            it.copy(openFeedbackUrlConfirmDialog = true)
        }
    }

    fun onHideFeedbackUrlConfirmDialogRequest() {
        _uiState.update {
            it.copy(openFeedbackUrlConfirmDialog = false)
        }
    }

    fun onShowSourceCodeUrlConfirmDialogRequest() {
        _uiState.update {
            it.copy(openSourceCodeUrlConfirmDialog = true)
        }
    }

    fun onHideSourceCodeUrlConfirmDialogRequest() {
        _uiState.update {
            it.copy(openSourceCodeUrlConfirmDialog = false)
        }
    }
}
