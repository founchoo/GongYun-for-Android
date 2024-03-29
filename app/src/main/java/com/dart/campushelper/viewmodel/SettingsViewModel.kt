package com.dart.campushelper.viewmodel

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.BuildConfig
import com.dart.campushelper.R
import com.dart.campushelper.alarm.GradeReminderRepository
import com.dart.campushelper.alarm.LessonReminderRepository
import com.dart.campushelper.receiver.AppWidgetPinnedReceiver
import com.dart.campushelper.receiver.AppWidgetReceiver
import com.dart.campushelper.repo.DataStoreRepo
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_ENABLE_SYSTEM_COLOR
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_IS_GRADE_REMINDER_ENABLED
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_IS_LESSON_REMINDER_ENABLED
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_IS_LOGIN
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_IS_OTHER_COURSE_DISPLAY
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_SELECTED_DARK_MODE
import com.dart.campushelper.repo.DataStoreRepo.Companion.DEFAULT_VALUE_USERNAME
import com.dart.campushelper.ui.component.listitem.SelectionItem
import com.dart.campushelper.ui.main.MainActivity
import com.dart.campushelper.utils.Notification
import com.dart.campushelper.utils.Permission
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
    val isLessonReminderEnabled: Boolean = DEFAULT_VALUE_IS_LESSON_REMINDER_ENABLED,
    val isGradeReminderEnabled: Boolean = DEFAULT_VALUE_IS_GRADE_REMINDER_ENABLED,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepo: DataStoreRepo,
    private val lessonReminderRepository: LessonReminderRepository,
    private val gradeReminderRepository: GradeReminderRepository,
    private val notification: Notification,
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

    private val usernameStateFlow = dataStoreRepo.observeUsername().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeUsername().first()
        }
    )

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

    private val isOtherCourseDisplayStateFlow =
        dataStoreRepo.observeIsOtherCourseDisplay().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            runBlocking {
                dataStoreRepo.observeIsOtherCourseDisplay().first()
            }
        )

    private val isYearDisplayStateFlow = dataStoreRepo.observeIsYearDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeIsYearDisplay().first()
        }
    )

    private val isDateDisplayStateFlow = dataStoreRepo.observeIsDateDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeIsDateDisplay().first()
        }
    )

    private val isTimeDisplayStateFlow = dataStoreRepo.observeIsTimeDisplay().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        runBlocking {
            dataStoreRepo.observeIsTimeDisplay().first()
        }
    )

    private val isLoginStateFlow: StateFlow<Boolean> = dataStoreRepo.observeIsLogin().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = runBlocking {
            dataStoreRepo.observeIsLogin().first()
        }
    )

    private val isScreenshotModeStateFlow: StateFlow<Boolean> =
        dataStoreRepo.observeIsScreenshotMode().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepo.observeIsScreenshotMode().first()
            }
        )

    private val yearAndSemesterStateFlow: StateFlow<String> =
        dataStoreRepo.observeYearAndSemester().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepo.observeYearAndSemester().first()
            }
        )

    private val isLessonReminderEnabledStateFlow: StateFlow<Boolean> =
        dataStoreRepo.observeIsLessonReminderEnabled().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepo.observeIsLessonReminderEnabled().first()
            }
        )

    private val isGradeReminderEnabledStateFlow: StateFlow<Boolean> =
        dataStoreRepo.observeIsGradeReminderEnabled().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                dataStoreRepo.observeIsGradeReminderEnabled().first()
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
                AppWidgetReceiver.updateBroadcast(context)
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
        viewModelScope.launch {
            isLessonReminderEnabledStateFlow.collect { value ->
                _uiState.update { it.copy(isLessonReminderEnabled = value) }
                if (value) {
                    if (Permission.requestNotificationPermission()) {
                        notification.createNotificationChannel()
                        lessonReminderRepository.setAlarm()
                    } else {
                        changeIsLessonReminderEnabled(false)
                    }
                } else {
                    lessonReminderRepository.cancelAlarm()
                }
            }
        }
        viewModelScope.launch {
            isGradeReminderEnabledStateFlow.collect { value ->
                _uiState.update { it.copy(isGradeReminderEnabled = value) }
                if (value) {
                    if (Permission.requestNotificationPermission()) {
                        notification.createNotificationChannel()
                        gradeReminderRepository.setAlarm()
                    } else {
                        changeIsGradeReminderEnabled(false)
                    }
                } else {
                    gradeReminderRepository.cancelAlarm()
                }
            }
        }
        changeLanguage(_uiState.value.selectedLanguageIndex)
    }

    fun clearCookies() {
        viewModelScope.launch {
            dataStoreRepo.changeCookies(emptyList())
            MainActivity.snackBarHostState.showSnackbar("Cookies 已清除，请开始调试")
        }
    }

    fun changeEnableSystemColor(enable: Boolean) {
        viewModelScope.launch {
            dataStoreRepo.changeEnableSystemColor(enable)
        }
    }

    fun changeSelectedDarkModeIndex(darkModeIndex: Int) {
        viewModelScope.launch {
            dataStoreRepo.changeSelectedDarkMode(darkModeIndex)
        }
        _uiState.update {
            it.copy(
                selectedDarkModeIndex = darkModeIndex
            )
        }
    }

    fun changeIsScreenshotMode(isScreenshotMode: Boolean) {
        viewModelScope.launch {
            dataStoreRepo.changeIsScreenshotMode(isScreenshotMode)
        }
    }

    fun pin() {
        val widgetManager = AppWidgetManager.getInstance(context)

        if (widgetManager.isRequestPinAppWidgetSupported) {
            val providerInfo = widgetManager.getInstalledProvidersForPackage(
                context.packageName,
                null
            )[0]
            val successCallback = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, AppWidgetPinnedReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            widgetManager.requestPinAppWidget(providerInfo.provider, null, successCallback)
            viewModelScope.launch {
                MainActivity.snackBarHostState.showSnackbar(context.getString(R.string.pin_request))
            }
        } else {
            viewModelScope.launch {
                MainActivity.snackBarHostState.showSnackbar(context.getString(R.string.unsupport_to_pin))
            }
        }
    }

    fun changeIsOtherCourseDisplay(isOtherCourseDisplay: Boolean) {
        viewModelScope.launch {
            dataStoreRepo.changeIsOtherCourseDisplay(isOtherCourseDisplay)
        }
    }

    fun changeIsYearDisplay(isYearDisplay: Boolean) {
        viewModelScope.launch {
            dataStoreRepo.changeIsYearDisplay(isYearDisplay)
        }
    }

    fun changeIsDateDisplay(isDateDisplay: Boolean) {
        viewModelScope.launch {
            dataStoreRepo.changeIsDateDisplay(isDateDisplay)
        }
    }

    fun changeIsTimeDisplay(isTimeDisplay: Boolean) {
        viewModelScope.launch {
            dataStoreRepo.changeIsTimeDisplay(isTimeDisplay)
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

    fun copyText(cbManager: ClipboardManager) {
        cbManager.setText(AnnotatedString(context.getString(R.string.qq_group_number)))
        viewModelScope.launch {
            MainActivity.snackBarHostState.showSnackbar(context.getString(R.string.copy_group_toast))
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

    fun changeIsLessonReminderEnabled(value: Boolean) {
        viewModelScope.launch {
            dataStoreRepo.changeIsLessonReminderEnabled(value)
        }
    }

    fun changeIsGradeReminderEnabled(value: Boolean) {
        viewModelScope.launch {
            dataStoreRepo.changeIsGradeReminderEnabled(value)
        }
    }
}
