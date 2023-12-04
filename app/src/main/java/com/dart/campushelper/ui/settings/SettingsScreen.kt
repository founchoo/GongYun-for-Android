package com.dart.campushelper.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.CampusHelperApplication.Companion.context
import com.dart.campushelper.R
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.ui.component.DropdownMenuPreference
import com.dart.campushelper.ui.component.PreferenceHeader
import com.dart.campushelper.ui.component.SwitchPreference
import com.dart.campushelper.ui.component.TextAlertDialog
import com.dart.campushelper.ui.component.TextPreference
import com.dart.campushelper.ui.login.LoginViewModel
import com.dart.campushelper.ui.login.ShowLoginDialog
import com.dart.campushelper.ui.rememberAccountCircle
import com.dart.campushelper.ui.rememberCalendarViewDay
import com.dart.campushelper.ui.rememberChat
import com.dart.campushelper.ui.rememberClearNight
import com.dart.campushelper.ui.rememberCode
import com.dart.campushelper.ui.rememberDoNotDisturbOn
import com.dart.campushelper.ui.rememberInfo
import com.dart.campushelper.ui.rememberLanguage
import com.dart.campushelper.ui.rememberPalette
import com.dart.campushelper.ui.rememberPushPin
import com.dart.campushelper.ui.rememberSchedule
import com.dart.campushelper.ui.rememberScreenshotFrame
import com.dart.campushelper.ui.rememberToday
import com.dart.campushelper.ui.theme.DarkMode
import com.dart.campushelper.ui.theme.toStringResourceId
import com.dart.campushelper.utils.Constants.Companion.DEV_GITHUB_URL
import com.dart.campushelper.utils.Constants.Companion.PROJECT_GITHUB_URL
import com.dart.campushelper.utils.Constants.Companion.QQ_GROUP_NUMBER
import com.dart.campushelper.utils.replaceWithStars
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    loginViewModel: LoginViewModel
) {

    val loginUiState by loginViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

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
                }" else stringResource(R.string.unlogin_message),
                imageVector = rememberAccountCircle()
            ) {
                if (settingsUiState.isLogin) {
                    settingsViewModel.onShowLogoutConfirmDialogRequest()
                } else {
                    loginViewModel.onShowLoginDialogRequest()
                }
            }
            PreferenceHeader(text = stringResource(R.string.schedule_label))
            SwitchPreference(
                imageVector = rememberDoNotDisturbOn(),
                value = settingsUiState.isOtherCourseDisplay,
                title = stringResource(R.string.show_non_week_course_title),
                description = stringResource(R.string.show_non_week_course_desc),
                onValueChanged = {
                    settingsViewModel.changeIsOtherCourseDisplay(it)
                }
            )
            SwitchPreference(
                imageVector = rememberCalendarViewDay(),
                value = settingsUiState.isYearDisplay,
                title = stringResource(R.string.show_year_title),
                description = stringResource(R.string.show_year_desc),
                onValueChanged = {
                    settingsViewModel.changeIsYearDisplay(it)
                }
            )
            SwitchPreference(
                imageVector = rememberToday(),
                value = settingsUiState.isDateDisplay,
                title = stringResource(R.string.show_date_title),
                description = stringResource(R.string.show_date_desc),
                onValueChanged = {
                    settingsViewModel.changeIsDateDisplay(it)
                }
            )
            SwitchPreference(
                imageVector = rememberSchedule(),
                value = settingsUiState.isTimeDisplay,
                title = stringResource(R.string.show_node_time_title),
                description = stringResource(R.string.show_node_time_desc),
                onValueChanged = {
                    settingsViewModel.changeIsTimeDisplay(it)
                }
            )
            // Pin course info widget to desktop
            TextPreference(
                imageVector = rememberPushPin(),
                title = stringResource(R.string.show_pin_title),
                description = stringResource(R.string.show_pin_desc),
                onClick = {
                    settingsViewModel.pin()
                }
            )
            PreferenceHeader(text = stringResource(R.string.display))
            SwitchPreference(
                imageVector = rememberPalette(),
                title = stringResource(R.string.system_color_title),
                description = stringResource(R.string.system_color_desc),
                value = settingsUiState.isSystemColor,
                onValueChanged = {
                    settingsViewModel.changeEnableSystemColor(it)
                }
            )
            DropdownMenuPreference(
                imageVector = rememberClearNight(),
                title = stringResource(R.string.dark_mode),
                value = stringResource(DarkMode.values()[settingsUiState.selectedDarkModeIndex].toStringResourceId()),
                selections = DarkMode.values().map {
                    stringResource(it.toStringResourceId())
                },
                onValueChanged = { index, _ ->
                    settingsViewModel.changeSelectedDarkModeIndex(index)
                }
            )
            DropdownMenuPreference(
                imageVector = rememberLanguage(),
                title = stringResource(R.string.language),
                value = (listOf(stringResource(R.string.follow_system)) + settingsUiState.languageList)[settingsUiState.selectedLanguageIndex],
                selections = listOf(stringResource(R.string.follow_system)) + settingsUiState.languageList,
                onValueChanged = { index, item ->
                    settingsViewModel.changeLanguage(index, item)
                }
            )
            PreferenceHeader(text = stringResource(R.string.about))
            TextPreference(
                title = stringResource(R.string.app_name),
                description = "copyright 2023 摘叶飞镖 ver ${settingsUiState.appVersion}",
                imageVector = rememberInfo()
            ) {
                // settingsViewModel.clearCookies()
                settingsViewModel.changeDevSectionShow(!settingsUiState.isDevSectionShow)
            }
            TextPreference(
                title = stringResource(R.string.dev_title),
                description = stringResource(R.string.dev_name),
                painter = painterResource(R.drawable.dev_avatar),
            ) {
                settingsViewModel.onShowDevProfileUrlConfirmDialogRequest()
            }
            TextPreference(
                title = stringResource(R.string.feedback_title),
                description = stringResource(R.string.feedback_desc),
                imageVector = rememberChat()
            ) {
                settingsViewModel.onShowFeedbackUrlConfirmDialogRequest()
            }
            TextPreference(
                title = stringResource(R.string.open_source_code_title),
                description = stringResource(R.string.open_source_code_desc),
                imageVector = rememberCode()
            ) {
                settingsViewModel.onShowSourceCodeUrlConfirmDialogRequest()
            }
            if (settingsUiState.isDevSectionShow) {
                PreferenceHeader(text = stringResource(R.string.dev))
                SwitchPreference(
                    imageVector = rememberScreenshotFrame(),
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
        ShowLoginDialog(loginViewModel)
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

    val clipboardManager = LocalClipboardManager.current

    TextAlertDialog(
        isShowDialog = settingsUiState.openFeedbackUrlConfirmDialog,
        actionAfterConfirm = {
            settingsViewModel.onHideFeedbackUrlConfirmDialogRequest()
            // Copy QQ group number
            clipboardManager.setText(AnnotatedString(QQ_GROUP_NUMBER))
            settingsViewModel.viewModelScope.launch {
                MainActivity.snackBarHostState.showSnackbar(context.getString(R.string.copy_group_toast))
            }
        },
        onDismissRequest = { settingsViewModel.onHideFeedbackUrlConfirmDialogRequest() },
        contentText = stringResource(R.string.copy_group_message)
    )

    TextAlertDialog(
        isShowDialog = settingsUiState.openSourceCodeUrlConfirmDialog,
        actionAfterConfirm = {
            settingsViewModel.onHideSourceCodeUrlConfirmDialogRequest()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse(PROJECT_GITHUB_URL)
            context.startActivity(intent)
        },
        onDismissRequest = { settingsViewModel.onHideSourceCodeUrlConfirmDialogRequest() },
        contentText = stringResource(R.string.open_source_code_message)
    )

    TextAlertDialog(
        isShowDialog = settingsUiState.openDevProfileUrlConfirmDialog,
        actionAfterConfirm = {
            settingsViewModel.onHideDevProfileUrlConfirmDialogRequest()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse(DEV_GITHUB_URL)
            context.startActivity(intent)
        },
        onDismissRequest = { settingsViewModel.onHideDevProfileUrlConfirmDialogRequest() },
        contentText = stringResource(R.string.open_dev_profile_message)
    )
}
