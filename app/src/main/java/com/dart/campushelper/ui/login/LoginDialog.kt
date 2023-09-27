package com.dart.campushelper.ui.login

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.dart.campushelper.ui.rememberVisibility
import com.dart.campushelper.ui.rememberVisibilityOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowLoginDialog(
    loginViewModel: LoginViewModel
) {

    val uiState by loginViewModel.uiState.collectAsState()

    var displayPassword by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    displayPassword = isPressed

    AlertDialog(
        title = {
            Text(text = "综合教务管理系统")
        },
        text = {
            Column(verticalArrangement = Arrangement.SpaceAround) {
                TextField(
                    value = uiState.username,
                    onValueChange = { loginViewModel.onUsernameChanged(it) },
                    label = { Text("学号") },
                    isError = uiState.loginInfoError,
                    supportingText = {
                        if (uiState.loginInfoError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "学号或密码错误",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        if (uiState.loginInfoError) {
                            Icon(Icons.Filled.Warning,"error", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                )
                TextField(
                    value = uiState.password,
                    onValueChange = { loginViewModel.onPasswordChanged(it) },
                    label = { Text("密码") },
                    isError = uiState.loginInfoError,
                    supportingText = {
                        if (uiState.loginInfoError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = "学号或密码错误",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        if (uiState.loginInfoError) {
                            Icon(Icons.Filled.Warning,"error", tint = MaterialTheme.colorScheme.error)
                        } else {
                            IconButton(
                                onClick = { },
                                interactionSource = interactionSource
                            ) {
                                Icon(
                                    imageVector = if (displayPassword) rememberVisibility() else rememberVisibilityOff(),
                                    contentDescription = "密码",
                                )
                            }
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