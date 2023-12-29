package com.dart.campushelper.ui.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.TextAlertDialog
import com.dart.campushelper.ui.component.listitem.BasicListItem
import com.dart.campushelper.ui.component.listitem.DropdownListItem
import com.dart.campushelper.ui.component.listitem.SelectionItem
import com.dart.campushelper.ui.component.listitem.SwitchListItem
import com.dart.campushelper.ui.login.LoginDialog
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.utils.visitWebsite
import com.dart.campushelper.viewmodel.DarkMode
import com.dart.campushelper.viewmodel.LoginViewModel
import com.dart.campushelper.viewmodel.SettingsViewModel
import com.dart.campushelper.viewmodel.toStringResourceId

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnrememberedMutableState", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    loginViewModel: LoginViewModel
) {
    val loginUiState by loginViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    val cbManager = LocalClipboardManager.current

    Column(Modifier.verticalScroll(rememberScrollState())) {
        BasicListItem(leadingText = stringResource(R.string.account))
        BasicListItem(
            headlineText = if (settingsUiState.isLogin) stringResource(R.string.logout) else stringResource(
                R.string.login
            ),
            supportingText = if (settingsUiState.isLogin) "${stringResource(R.string.welcome)} ${
                settingsUiState.username.replaceWithStars(
                    settingsUiState.isScreenshotMode
                )
            }" else stringResource(
                R.string.unlogin_message
            ),
            leadingImageVector = Icons.Outlined.AccountCircle
        ) {
            if (settingsUiState.isLogin) {
                settingsViewModel.onShowLogoutConfirmDialogRequest()
            } else {
                loginViewModel.onShowLoginDialogRequest()
            }
        }
        BasicListItem(leadingText = stringResource(R.string.schedule_label))
        SwitchListItem(
            leadingImageVector = Icons.Outlined.NotificationsActive,
            value = settingsUiState.isLessonReminderEnabled,
            headlineText = stringResource(R.string.lesson_reminder_title),
            supportingText = stringResource(R.string.lesson_reminder_desc),
            onValueChanged = {
                settingsViewModel.changeIsLessonReminderEnabled(it)
            }
        )
        SwitchListItem(
            leadingImageVector = Icons.Outlined.DoNotDisturbOn,
            value = settingsUiState.isOtherCourseDisplay,
            headlineText = stringResource(R.string.show_non_week_course_title),
            supportingText = stringResource(R.string.show_non_week_course_desc),
            onValueChanged = {
                settingsViewModel.changeIsOtherCourseDisplay(it)
            }
        )
        SwitchListItem(
            leadingImageVector = Icons.Outlined.CalendarViewDay,
            value = settingsUiState.isYearDisplay,
            headlineText = stringResource(R.string.show_year_title),
            supportingText = stringResource(R.string.show_year_desc),
            onValueChanged = {
                settingsViewModel.changeIsYearDisplay(it)
            }
        )
        SwitchListItem(
            leadingImageVector = Icons.Outlined.Today,
            value = settingsUiState.isDateDisplay,
            headlineText = stringResource(R.string.show_date_title),
            supportingText = stringResource(R.string.show_date_desc),
            onValueChanged = {
                settingsViewModel.changeIsDateDisplay(it)
            }
        )
        SwitchListItem(
            leadingImageVector = Icons.Outlined.Schedule,
            value = settingsUiState.isTimeDisplay,
            headlineText = stringResource(R.string.show_node_time_title),
            supportingText = stringResource(R.string.show_node_time_desc),
            onValueChanged = {
                settingsViewModel.changeIsTimeDisplay(it)
            }
        )
        // Pin course info widget to desktop
        BasicListItem(
            leadingImageVector = Icons.Outlined.PushPin,
            headlineText = stringResource(R.string.show_pin_title),
            supportingText = stringResource(R.string.show_pin_desc),
            onClick = {
                settingsViewModel.pin()
            }
        )
        BasicListItem(leadingText = stringResource(R.string.display))
        SwitchListItem(
            leadingImageVector = Icons.Outlined.Palette,
            headlineText = stringResource(R.string.system_color_title),
            supportingText = stringResource(R.string.system_color_desc),
            value = settingsUiState.isSystemColor,
            onValueChanged = {
                settingsViewModel.changeEnableSystemColor(it)
            }
        )
        DropdownListItem(
            leadingImageVector = Icons.Outlined.Nightlight,
            headlineText = stringResource(R.string.dark_mode),
            value = DarkMode.values()[settingsUiState.selectedDarkModeIndex],
            selections = DarkMode.values().map {
                SelectionItem(stringResource(it.toStringResourceId()), it)
            },
            onValueChanged = { index, _ ->
                settingsViewModel.changeSelectedDarkModeIndex(index)
            }
        )
        DropdownListItem(
            leadingImageVector = Icons.Outlined.Language,
            headlineText = stringResource(R.string.language),
            value = settingsUiState.languageList[settingsUiState.selectedLanguageIndex].value,
            selections = settingsUiState.languageList,
            onValueChanged = { index, _ ->
                settingsViewModel.changeLanguage(index)
            }
        )
        BasicListItem(leadingText = stringResource(R.string.about))
        BasicListItem(
            headlineText = stringResource(R.string.app_name),
            supportingText = "copyright 2023 摘叶飞镖 ver ${settingsUiState.appVersion}",
            leadingImageVector = Icons.Outlined.Info
        ) {
            // settingsViewModel.clearCookies()
            settingsViewModel.changeDevSectionShow(!settingsUiState.isDevSectionShow)
        }
        BasicListItem(
            headlineText = stringResource(R.string.dev_title),
            supportingText = stringResource(R.string.dev_name),
            leadingPainter = painterResource(R.drawable.dev_avatar),
        ) {
            visitWebsite(context.getString(R.string.dev_github_url))
        }
        BasicListItem(
            headlineText = stringResource(R.string.feedback_title),
            supportingText = stringResource(R.string.feedback_desc),
            leadingImageVector = Icons.AutoMirrored.Outlined.Chat
        ) {

            settingsViewModel.copyText(cbManager)
        }
        BasicListItem(
            headlineText = stringResource(R.string.open_source_code_title),
            supportingText = stringResource(R.string.open_source_code_desc),
            leadingImageVector = Icons.Outlined.Code
        ) {
            visitWebsite(context.getString(R.string.project_github_url))
        }
        if (settingsUiState.isDevSectionShow) {
            BasicListItem(leadingText = stringResource(R.string.dev))
            SwitchListItem(
                leadingImageVector = Icons.Outlined.Screenshot,
                headlineText = stringResource(R.string.screenshot_mode_title),
                supportingText = stringResource(R.string.screenshot_mode_desc),
                value = settingsUiState.isScreenshotMode,
                onValueChanged = {
                    settingsViewModel.changeIsScreenshotMode(it)
                }
            )
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
