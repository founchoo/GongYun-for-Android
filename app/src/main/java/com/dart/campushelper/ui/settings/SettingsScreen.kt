package com.dart.campushelper.ui.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dart.campushelper.App
import com.dart.campushelper.ui.*
import com.dart.campushelper.ui.login.LoginViewModel
import com.dart.campushelper.ui.login.ShowLoginDialog
import com.dart.campushelper.utils.Constants.Companion.JOIN_QQ_GROUP_URL
import com.dart.campushelper.utils.DropdownMenuPreference
import com.dart.campushelper.utils.PreferenceHeader
import com.dart.campushelper.utils.SwitchPreference
import com.dart.campushelper.utils.TextPreference

@SuppressLint("UnrememberedMutableState", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.provideFactory(App.container.appRepository)
    )

    val isLogin by settingsViewModel.isLogin.collectAsState()
    val username by settingsViewModel.username.collectAsState()
    val enableSystemColor by settingsViewModel.enableSystemColor.collectAsState()
    val selectedDarkMode by settingsViewModel.selectedDarkModel.collectAsState()
    val isPin by settingsViewModel.isPin.collectAsState()

    var openFeedbackUrlConfirmDialog = remember { mutableStateOf(false) }
    var openSourceCodeUrlConfirmDialog = remember { mutableStateOf(false) }
    var openLogoutConfirmDialog = remember { mutableStateOf(false) }

    LazyColumn() {
        item {
            PreferenceHeader(text = "账户")
            TextPreference(
                title = if (isLogin == true) "登出" else "登录",
                description = if (isLogin == true) "欢迎 ${username}" else "请先登录",
                imageVector = rememberAccountCircle()
            ) {
                if (isLogin == true) {
                    openLogoutConfirmDialog.value = true
                } else {
                    loginViewModel.isShowLoginDialog = true
                }
            }
            /*PreferenceHeader(text = "课表")
            // Pin course info widget to desktop
            SwitchPreference(
                imageVector = rememberPushPin(),
                defaultValue = isPin ?: false,
                title = "固定微件到桌面",
                onValueChanged = {
                    settingsViewModel.changeIsPin(it)
                }
            )*/
            PreferenceHeader(text = "主题")
            SwitchPreference(
                imageVector = rememberPalette(),
                title = "系统主题色",
                defaultValue = enableSystemColor ?: false,
                onValueChanged = {
                    settingsViewModel.changeEnableSystemColor(it)
                }
            )
            DropdownMenuPreference(
                imageVector = rememberClearNight(),
                title = "深色主题",
                defaultValue = selectedDarkMode,
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
                description = "copyright 2023 摘叶飞镖",
                imageVector = rememberInfo()
            ) {
            }
            TextPreference(
                title = "反馈",
                description = "加入群聊提供你的意见及建议",
                imageVector = rememberChat()
            ) {
                openFeedbackUrlConfirmDialog.value = true
            }
            TextPreference(
                title = "开源代码",
                description = "你的开发将为此应用贡献一份力量",
                imageVector = rememberCode()
            ) {
                openSourceCodeUrlConfirmDialog.value = true
            }
        }
    }

    if (loginViewModel.isShowLoginDialog) {
        ShowLoginDialog(loginViewModel)
    }

    addTextAlertDialog(
        openDialog = openLogoutConfirmDialog,
        actionAfterConfirm = { loginViewModel.logout() },
        contentText = "确定要登出吗？"
    )

    addTextAlertDialog(
        openDialog = openFeedbackUrlConfirmDialog,
        actionAfterConfirm = {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse(JOIN_QQ_GROUP_URL)
            App.context.startActivity(intent)
        },
        contentText = "您即将离开此应用，确认后将跳转到浏览器打开 QQ 群入群链接"
    )

    addTextAlertDialog(
        openDialog = openSourceCodeUrlConfirmDialog,
        actionAfterConfirm = {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse("https://github.com/founchoo")
            App.context.startActivity(intent)
        },
        contentText = "您即将离开此应用，确认后将跳转到浏览器打开 Github 仓库"
    )
}

@Composable
fun addTextAlertDialog(
    openDialog: MutableState<Boolean>,
    actionAfterConfirm: () -> Unit,
    titleText: String = "提示",
    contentText: String
) {
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
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
                    openDialog.value = false
                    actionAfterConfirm()
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("取消")
                }
            }
        )
    }
}
