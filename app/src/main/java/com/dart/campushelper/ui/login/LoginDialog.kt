package com.dart.campushelper.ui.login

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.dart.campushelper.ui.rememberVisibility
import com.dart.campushelper.ui.rememberVisibilityOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowLoginDialog(
    loginViewModel: LoginViewModel
) {

    val loginUiState by loginViewModel.uiState.collectAsState()

    var displayPassword by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    displayPassword = isPressed

    AlertDialog(
        title = {
            Text(text = "综合教务管理系统")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = loginUiState.username,
                    onValueChange = { loginViewModel.onUsernameChanged(it) },
                    label = { Text("账号") },
                )
                OutlinedTextField(
                    value = loginUiState.password,
                    onValueChange = { loginViewModel.onPasswordChanged(it) },
                    label = { Text("密码") },
                    trailingIcon = {
                        IconButton(
                            onClick = { },
                            interactionSource = interactionSource
                        ) {
                            Icon(
                                imageVector = if (displayPassword) rememberVisibility() else rememberVisibilityOff(),
                                contentDescription = "密码",
                            )
                        }
                    },
                    visualTransformation = if (displayPassword) VisualTransformation.None else PasswordVisualTransformation()
                )
            }
        },
        onDismissRequest = {
            loginViewModel.onHideLoginDialogRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    loginViewModel.login()
                }
            ) {
                Text("登录")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    loginViewModel.onHideLoginDialogRequest()
                }
            ) {
                Text("取消")
            }
        }
    )
}