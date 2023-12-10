package com.dart.campushelper.viewmodel

import android.appwidget.AppWidgetManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.BuildConfig
import com.dart.campushelper.CampusHelperApplication.Companion.context
import com.dart.campushelper.data.DataStoreRepository
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_IS_LOGIN
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_SELECTED_DARK_MODE
import com.dart.campushelper.data.DataStoreRepository.Companion.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.ui.main.MainActivity
import com.dart.campushelper.ui.component.preference.SelectionItem
import com.dart.campushelper.ui.main.pin
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
    val selectedDarkModeIndex: Int = DEFAULT_VALUE_SELECTED_DARK_MODE,
    val isOtherCourseDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isYearDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isDateDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isTimeDisplay: Boolean = DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY,
    val isScreenshotMode: Boolean = false,
    val openLogoutConfirmDialog: Boolean = false,
    val openFeedbackUrlConfirmDialog: Boolean = false,
    val openSourceCodeUrlConfirmDialog: Boolean = false,
    val openDevProfileUrlConfirmDialog: Boolean = false,
    val appVersion: String = BuildConfig.VERSION_NAME,
    val isDevSectionShow: Boolean = false,
    val languageList: List<SelectionItem<String>>,
    val selectedLanguageIndex: Int,
    val currentYearAndSemester: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private var _languageMap = mapOf(
        "中文（简体）" to "zh",
        "English" to "en",
    )

    // UI state exposed to the UI
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            languageList = _languageMap.map {
                SelectionItem(it.key, it.value)
            },
            selectedLanguageIndex = _languageMap.values.indexOf(
                AppCompatDelegate.getApplicationLocales()[0]?.language
            ).let { if (it == -1) 0 else it },
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

    private val isOtherCourseDisplayStateFlow =
        dataStoreRepository.observeIsOtherCourseDisplay().stateIn(
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

    private val isScreenshotModeStateFlow: StateFlow<Boolean> =
        dataStoreRepository.observeIsScreenshotMode().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepository.observeIsScreenshotMode().first()
            }
        )

    private val yearAndSemesterStateFlow: StateFlow<String> =
        dataStoreRepository.observeYearAndSemester().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepository.observeYearAndSemester().first()
            }
        )

    init {
        viewModelScope.launch {
            usernameStateFlow.collect { value ->
                _uiState.update { it.copy(username = value) }
            }
        }
        viewModelScope.launch {
            isLoginStateFlow.collect { value ->
                _uiState.update { it.copy(isLogin = value) }
            }
        }
        viewModelScope.launch {
            enableSystemColorStateFlow.collect { value ->
                _uiState.update { it.copy(isSystemColor = value) }
            }
        }
        viewModelScope.launch {
            selectedDarkModeStateFlow.collect { value ->
                _uiState.update { it.copy(selectedDarkModeIndex = value) }
            }
        }
        viewModelScope.launch {
            isOtherCourseDisplayStateFlow.collect { value ->
                _uiState.update { it.copy(isOtherCourseDisplay = value) }
            }
        }
        viewModelScope.launch {
            isYearDisplayStateFlow.collect { value ->
                _uiState.update { it.copy(isYearDisplay = value) }
            }
        }
        viewModelScope.launch {
            isDateDisplayStateFlow.collect { value ->
                _uiState.update { it.copy(isDateDisplay = value) }
            }
        }
        viewModelScope.launch {
            isTimeDisplayStateFlow.collect { value ->
                _uiState.update { it.copy(isTimeDisplay = value) }
            }
        }
        viewModelScope.launch {
            isScreenshotModeStateFlow.collect { value ->
                _uiState.update { it.copy(isScreenshotMode = value) }
            }
        }
        viewModelScope.launch {
            yearAndSemesterStateFlow.collect { value ->
                _uiState.update { it.copy(currentYearAndSemester = value) }
            }
        }
        changeLanguage(_uiState.value.selectedLanguageIndex)
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

    fun changeSelectedDarkModeIndex(darkModeIndex: Int) {
        viewModelScope.launch {
            dataStoreRepository.changeSelectedDarkMode(darkModeIndex)
        }
        _uiState.update {
            it.copy(
                selectedDarkModeIndex = darkModeIndex
            )
        }
    }

    fun changeIsScreenshotMode(isScreenshotMode: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.changeIsScreenshotMode(isScreenshotMode)
        }
    }

    fun pin() {
        val widgetManager = AppWidgetManager.getInstance(context)
        // Get a list of our app widget providers to retrieve their info
        val widgetProviders =
            widgetManager.getInstalledProvidersForPackage(
                context.packageName,
                null
            )
        widgetProviders[0].pin(context)
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

    fun onShowDevProfileUrlConfirmDialogRequest() {
        _uiState.update {
            it.copy(openDevProfileUrlConfirmDialog = true)
        }
    }

    fun onHideDevProfileUrlConfirmDialogRequest() {
        _uiState.update {
            it.copy(openDevProfileUrlConfirmDialog = false)
        }
    }

    fun changeDevSectionShow(isDevSectionShow: Boolean) {
        _uiState.update {
            it.copy(isDevSectionShow = isDevSectionShow)
        }
    }

    fun getLanguageCode(name: String): String {
        return _languageMap[name] ?: _languageMap.toList()[0].second
    }

    fun changeLanguage(index: Int) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(_languageMap.toList()[index].second)
        )
        _uiState.update {
            it.copy(
                selectedLanguageIndex = index,
            )
        }
    }
}
