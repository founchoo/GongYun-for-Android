package com.dart.campushelper.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.CampusHelperApplication
import com.dart.campushelper.ui.MainActivity
import com.dart.campushelper.ui.login.LoginViewModel
import com.dart.campushelper.ui.login.ShowLoginDialog
import com.dart.campushelper.ui.rememberAccountCircle
import com.dart.campushelper.ui.rememberChat
import com.dart.campushelper.ui.rememberClearNight
import com.dart.campushelper.ui.rememberCode
import com.dart.campushelper.ui.rememberInfo
import com.dart.campushelper.ui.rememberPalette
import com.dart.campushelper.ui.rememberPushPin
import com.dart.campushelper.utils.Constants.Companion.GITHUB_URL
import com.dart.campushelper.utils.Constants.Companion.QQ_GROUP_NUMBER
import com.dart.campushelper.utils.DropdownMenuPreference
import com.dart.campushelper.utils.PreferenceHeader
import com.dart.campushelper.utils.SwitchPreference
import com.dart.campushelper.utils.TextPreference
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    loginViewModel: LoginViewModel
) {

    val loginUiState by loginViewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()

    LazyColumn() {
        item {
            PreferenceHeader(text = "账户")
            TextPreference(
                title = if (settingsUiState.isLogin) "登出" else "登录",
                description = if (settingsUiState.isLogin) "欢迎 ${settingsUiState.username}" else "请先登录",
                imageVector = rememberAccountCircle()
            ) {
                if (settingsUiState.isLogin) {
                    settingsViewModel.onShowLogoutConfirmDialogRequest()
                } else {
                    loginViewModel.onShowLoginDialogRequest()
                }
            }
            PreferenceHeader(text = "课表")
            // Pin course info widget to desktop
            SwitchPreference(
                imageVector = rememberPushPin(),
                value = settingsUiState.isPin,
                title = "（实验性）固定课程到桌面",
                description = "请确保已授予\"桌面快捷方式\"权限",
                onValueChanged = {
                    settingsViewModel.changeIsPin(it)
                }
            )
            PreferenceHeader(text = "主题")
            SwitchPreference(
                imageVector = rememberPalette(),
                title = "系统主题色",
                description = "开启后将跟随系统主题色",
                value = settingsUiState.enableSystemColor,
                onValueChanged = {
                    settingsViewModel.changeEnableSystemColor(it)
                }
            )
            DropdownMenuPreference(
                imageVector = rememberClearNight(),
                title = "深色主题",
                value = settingsUiState.selectedDarkMode,
                selections = listOf(
                    "跟随系统",
                    "开启",
                    "关闭"
                ),
                onValueChanged = {
                    settingsViewModel.changeSelectedDarkMode(it)
                }
            )
            PreferenceHeader(text = "关于")
            TextPreference(
                title = "校园助手",
                description = "copyright 2023 摘叶飞镖 ver ${settingsUiState.appVersion}",
                imageVector = rememberInfo()
            ) {
                // settingsViewModel.clearCookies()
            }
            TextPreference(
                title = "反馈",
                description = "加入群聊提供你的意见及建议",
                imageVector = rememberChat()
            ) {
                settingsViewModel.onShowFeedbackUrlConfirmDialogRequest()
            }
            TextPreference(
                title = "开源代码",
                description = "你的开发将为此应用贡献一份力量",
                imageVector = rememberCode()
            ) {
                settingsViewModel.onShowSourceCodeUrlConfirmDialogRequest()
            }
        }
    }

    if (loginUiState.isShowLoginDialog) {
        ShowLoginDialog(loginViewModel)
    }

    addTextAlertDialog(
        isShowDialog = settingsUiState.openLogoutConfirmDialog,
        actionAfterConfirm = {
            loginViewModel.logout()
            settingsViewModel.onHideLogoutConfirmDialogRequest()
        },
        onDismissRequest = { settingsViewModel.onHideLogoutConfirmDialogRequest() },
        contentText = "确定要登出吗？"
    )

    val clipboardManager = LocalClipboardManager.current

    addTextAlertDialog(
        isShowDialog = settingsUiState.openFeedbackUrlConfirmDialog,
        actionAfterConfirm = {
            settingsViewModel.onHideFeedbackUrlConfirmDialogRequest()
            // Copy QQ group number
            clipboardManager.setText(AnnotatedString(QQ_GROUP_NUMBER))
            settingsViewModel.viewModelScope.launch {
                MainActivity.snackBarHostState.showSnackbar("已复制 QQ 群号码")
            }
        },
        onDismissRequest = { settingsViewModel.onHideFeedbackUrlConfirmDialogRequest() },
        contentText = "您即将复制 QQ 群号码"
    )

    addTextAlertDialog(
        isShowDialog = settingsUiState.openSourceCodeUrlConfirmDialog,
        actionAfterConfirm = {
            settingsViewModel.onHideSourceCodeUrlConfirmDialogRequest()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse(GITHUB_URL)
            CampusHelperApplication.context.startActivity(intent)
        },
        onDismissRequest = { settingsViewModel.onHideSourceCodeUrlConfirmDialogRequest() },
        contentText = "您即将离开此应用，确认后将跳转到浏览器打开 Github 仓库"
    )
}

@Composable
fun addTextAlertDialog(
    isShowDialog: Boolean,
    actionAfterConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    titleText: String = "提示",
    contentText: String
) {
    if (isShowDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = contentText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    actionAfterConfirm()
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("取消")
                }
            }
        )
    }
}
