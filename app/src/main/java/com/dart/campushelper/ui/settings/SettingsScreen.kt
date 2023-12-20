package com.dart.campushelper.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarViewDay
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Screenshot
import androidx.compose.material.icons.outlined.Today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.TextAlertDialog
import com.dart.campushelper.ui.component.preference.DropdownMenuPreference
import com.dart.campushelper.ui.component.preference.PreferenceHeader
import com.dart.campushelper.ui.component.preference.SelectionItem
import com.dart.campushelper.ui.component.preference.SwitchPreference
import com.dart.campushelper.ui.component.preference.TextPreference
import com.dart.campushelper.ui.login.LoginDialog
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.utils.visitWebsite
import com.dart.campushelper.viewmodel.DarkMode
import com.dart.campushelper.viewmodel.LoginViewModel
import com.dart.campushelper.viewmodel.SettingsViewModel
import com.dart.campushelper.viewmodel.toStringResourceId

@SuppressLint("UnrememberedMutableState", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    loginViewModel: LoginViewModel
) {
    val loginUiState by loginViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    val cbManager = LocalClipboardManager.current

    LazyColumn {
        item {
            PreferenceHeader(text = stringResource(R.string.account))
            TextPreference(
                title = if (settingsUiState.isLogin) stringResource(R.string.logout) else stringResource(
                    R.string.login
                ),
                description = if (settingsUiState.isLogin) "${stringResource(R.string.welcome)} ${
                    settingsUiState.username.replaceWithStars(
                        settingsUiState.isScreenshotMode
                    )
                }" else stringResource(
                    R.string.unlogin_message
                ),
                imageVector = Icons.Outlined.AccountCircle
            ) {
                if (settingsUiState.isLogin) {
                    settingsViewModel.onShowLogoutConfirmDialogRequest()
                } else {
                    loginViewModel.onShowLoginDialogRequest()
                }
            }
            PreferenceHeader(text = stringResource(R.string.schedule_label))
            SwitchPreference(
                imageVector = Icons.Outlined.NotificationsActive,
                value = settingsUiState.isLessonReminderEnabled,
                title = stringResource(R.string.lesson_reminder_title),
                description = stringResource(R.string.lesson_reminder_desc),
                onValueChanged = {
                    settingsViewModel.changeIsLessonReminderEnabled(it)
                }
            )
            SwitchPreference(
                imageVector = Icons.Outlined.DoNotDisturbOn,
                value = settingsUiState.isOtherCourseDisplay,
                title = stringResource(R.string.show_non_week_course_title),
                description = stringResource(R.string.show_non_week_course_desc),
                onValueChanged = {
                    settingsViewModel.changeIsOtherCourseDisplay(it)
                }
            )
            SwitchPreference(
                imageVector = Icons.Outlined.CalendarViewDay,
                value = settingsUiState.isYearDisplay,
                title = stringResource(R.string.show_year_title),
                description = stringResource(R.string.show_year_desc),
                onValueChanged = {
                    settingsViewModel.changeIsYearDisplay(it)
                }
            )
            SwitchPreference(
                imageVector = Icons.Outlined.Today,
                value = settingsUiState.isDateDisplay,
                title = stringResource(R.string.show_date_title),
                description = stringResource(R.string.show_date_desc),
                onValueChanged = {
                    settingsViewModel.changeIsDateDisplay(it)
                }
            )
            SwitchPreference(
                imageVector = Icons.Outlined.Schedule,
                value = settingsUiState.isTimeDisplay,
                title = stringResource(R.string.show_node_time_title),
                description = stringResource(R.string.show_node_time_desc),
                onValueChanged = {
                    settingsViewModel.changeIsTimeDisplay(it)
                }
            )
            // Pin course info widget to desktop
            TextPreference(
                imageVector = Icons.Outlined.PushPin,
                title = stringResource(R.string.show_pin_title),
                description = stringResource(R.string.show_pin_desc),
                onClick = {
                    settingsViewModel.pin()
                }
            )
            PreferenceHeader(text = stringResource(R.string.display))
            SwitchPreference(
                imageVector = Icons.Outlined.Palette,
                title = stringResource(R.string.system_color_title),
                description = stringResource(R.string.system_color_desc),
                value = settingsUiState.isSystemColor,
                onValueChanged = {
                    settingsViewModel.changeEnableSystemColor(it)
                }
            )
            DropdownMenuPreference(
                imageVector = Icons.Outlined.Nightlight,
                title = stringResource(R.string.dark_mode),
                value = DarkMode.values()[settingsUiState.selectedDarkModeIndex],
                selections = DarkMode.values().map {
                    SelectionItem(stringResource(it.toStringResourceId()), it)
                },
                onValueChanged = { index, _ ->
                    settingsViewModel.changeSelectedDarkModeIndex(index)
                }
            )
            DropdownMenuPreference(
                imageVector = Icons.Outlined.Language,
                title = stringResource(R.string.language),
                value = settingsUiState.languageList[settingsUiState.selectedLanguageIndex].value,
                selections = settingsUiState.languageList,
                onValueChanged = { index, _ ->
                    settingsViewModel.changeLanguage(index)
                }
            )
            PreferenceHeader(text = stringResource(R.string.about))
            TextPreference(
                title = stringResource(R.string.app_name),
                description = "copyright 2023 摘叶飞镖 ver ${settingsUiState.appVersion}",
                imageVector = Icons.Outlined.Info
            ) {
                // settingsViewModel.clearCookies()
                settingsViewModel.changeDevSectionShow(!settingsUiState.isDevSectionShow)
            }
            TextPreference(
                title = stringResource(R.string.dev_title),
                description = stringResource(R.string.dev_name),
                painter = painterResource(R.drawable.dev_avatar),
            ) {
                visitWebsite(context.getString(R.string.dev_github_url))
            }
            TextPreference(
                title = stringResource(R.string.feedback_title),
                description = stringResource(R.string.feedback_desc),
                imageVector = Icons.AutoMirrored.Outlined.Chat
            ) {

                settingsViewModel.copyText(cbManager)
            }
            TextPreference(
                title = stringResource(R.string.open_source_code_title),
                description = stringResource(R.string.open_source_code_desc),
                imageVector = Icons.Outlined.Code
            ) {
                visitWebsite(context.getString(R.string.project_github_url))
            }
            if (settingsUiState.isDevSectionShow) {
                PreferenceHeader(text = stringResource(R.string.dev))
                SwitchPreference(
                    imageVector = Icons.Outlined.Screenshot,
                    title = stringResource(R.string.screenshot_mode_title),
                    description = stringResource(R.string.screenshot_mode_desc),
                    value = settingsUiState.isScreenshotMode,
                    onValueChanged = {
                        settingsViewModel.changeIsScreenshotMode(it)
                    }
                )
            }
        }
    }

    if (loginUiState.isShowLoginDialog) {
        LoginDialog(loginViewModel)
    }

    TextAlertDialog(
        isShowDialog = settingsUiState.openLogoutConfirmDialog,
        actionAfterConfirm = {
            loginViewModel.logout()
            settingsViewModel.onHideLogoutConfirmDialogRequest()
        },
        onDismissRequest = { settingsViewModel.onHideLogoutConfirmDialogRequest() },
        contentText = stringResource(R.string.logout_message)
    )
}
