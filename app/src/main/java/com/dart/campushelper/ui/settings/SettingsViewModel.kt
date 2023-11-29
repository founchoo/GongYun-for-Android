package com.dart.campushelper.ui.settings

import android.appwidget.AppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.BuildConfig
import com.dart.campushelper.CampusHelperApplication
import com.dart.campushelper.data.DataStoreRepository
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_IS_LOGIN
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_SELECTED_DARK_MODE
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_USERNAME
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
    val isSystemColor: Boolean = DEFAULT_VALUE_ENABLE_SYSTEM_COLOR,
    val selectedDarkMode: String = DEFAULT_VALUE_SELECTED_DARK_MODE,
    val isOtherCourseDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isYearDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isDateDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isTimeDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isScreenshotMode: Boolean = false,
    val openLogoutConfirmDialog: Boolean = false,
    val openFeedbackUrlConfirmDialog: Boolean = false,
    val openSourceCodeUrlConfirmDialog: Boolean = false,
    val appVersion: String = "",
    val isDevSectionShow: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            appVersion = BuildConfig.VERSION_NAME
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val usernameStateFlow = dataStoreRepository.observeUsername().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepository.observeUsername().first()
        }
    )

    private val enableSystemColorStateFlow =
        dataStoreRepository.observeEnableSystemColor().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                dataStoreRepository.observeEnableSystemColor().first()
            }
        )

    private val selectedDarkModeStateFlow =
        dataStoreRepository.observeSelectedDarkMode().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                dataStoreRepository.observeSelectedDarkMode().first()
            }
        )

    private val isOtherCourseDisplayStateFlow = dataStoreRepository.observeIsOtherCourseDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepository.observeIsOtherCourseDisplay().first()
        }
    )

    private val isYearDisplayStateFlow = dataStoreRepository.observeIsYearDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepository.observeIsYearDisplay().first()
        }
    )

    private val isDateDisplayStateFlow = dataStoreRepository.observeIsDateDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepository.observeIsDateDisplay().first()
        }
    )

    private val isTimeDisplayStateFlow = dataStoreRepository.observeIsTimeDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepository.observeIsTimeDisplay().first()
        }
    )

    private val isLoginStateFlow: StateFlow<Boolean> = dataStoreRepository.observeIsLogin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            dataStoreRepository.observeIsLogin().first()
        }
    )

    private val isScreenshotModeStateFlow: StateFlow<Boolean> = dataStoreRepository.observeIsScreenshotMode().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            dataStoreRepository.observeIsScreenshotMode().first()
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
                    it.copy(isSystemColor = value ?: DEFAULT_VALUE_ENABLE_SYSTEM_COLOR)
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
        viewModelScope.launch {
            isScreenshotModeStateFlow.collect { value ->
                _uiState.update {
                    it.copy(isScreenshotMode = value)
                }
            }
        }
    }

    fun clearCookies() {
        viewModelScope.launch {
            dataStoreRepository.changeCookies(emptyList())
            MainActivity.snackBarHostState.showSnackbar("Cookies 已清除，请开始调试")
        }
    }

    fun changeEnableSystemColor(enable: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.changeEnableSystemColor(enable)
        }
    }

    fun changeSelectedDarkMode(darkMode: String) {
        viewModelScope.launch {
            dataStoreRepository.changeSelectedDarkMode(darkMode)
        }
    }

    fun changeIsScreenshotMode(isScreenshotMode: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.changeIsScreenshotMode(isScreenshotMode)
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
            dataStoreRepository.changeIsOtherCourseDisplay(isOtherCourseDisplay)
        }
    }

    fun changeIsYearDisplay(isYearDisplay: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.changeIsYearDisplay(isYearDisplay)
        }
    }

    fun changeIsDateDisplay(isDateDisplay: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.changeIsDateDisplay(isDateDisplay)
        }
    }

    fun changeIsTimeDisplay(isTimeDisplay: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.changeIsTimeDisplay(isTimeDisplay)
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

    fun changeDevSectionShow(isDevSectionShow: Boolean) {
        _uiState.update {
            it.copy(isDevSectionShow = isDevSectionShow)
        }
    }
}
