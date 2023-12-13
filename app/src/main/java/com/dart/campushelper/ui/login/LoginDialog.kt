package com.dart.campushelper.ui.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.api.NetworkService.Companion.BASE_URL
import com.dart.campushelper.utils.visitWebsite
import com.dart.campushelper.viewmodel.LoginViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginDialog(
    loginViewModel: LoginViewModel
) {

    val uiState by loginViewModel.uiState.collectAsState()

    var displayPassword by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    displayPassword = isPressed

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.login_dialog_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.SpaceAround) {
                TextField(
                    readOnly = !uiState.inputEnabled,
                    value = uiState.username,
                    onValueChange = { loginViewModel.onUsernameChanged(it) },
                    label = { Text(stringResource(R.string.student_number)) },
                    isError = uiState.loginResponse == false,
                    supportingText = {
                        if (uiState.loginResponse == false) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.login_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        if (uiState.loginResponse == false) {
                            Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                        }
                    },
                )
                TextField(
                    readOnly = !uiState.inputEnabled,
                    value = uiState.password,
                    onValueChange = { loginViewModel.onPasswordChanged(it) },
                    label = { Text(stringResource(R.string.password)) },
                    isError = uiState.loginResponse == false,
                    supportingText = {
                        if (uiState.loginResponse == false) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.login_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        if (uiState.loginResponse == false) {
                            Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                        } else if (uiState.inputEnabled) {
                            IconButton(
                                onClick = { },
                                interactionSource = interactionSource
                            ) {
                                Icon(
                                    imageVector = if (displayPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                    contentDescription = stringResource(R.string.password),
                                )
                            }
                        }
                    },
                    visualTransformation = if (displayPassword) VisualTransformation.None else PasswordVisualTransformation()
                )
                if (uiState.loginResponse == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.network_connection_error),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (uiState.inputEnabled) {
                    ClickableText(
                        modifier = Modifier
                            .basicMarquee()
                            .padding(bottom = 5.dp),
                        text = AnnotatedString(
                            stringResource(R.string.login_help_text, BASE_URL),
                            spanStyle = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ),
                        onClick = {
                            visitWebsite(BASE_URL)
                        }
                    )
                    ClickableText(
                        text = AnnotatedString(
                            stringResource(R.string.fill_mock_info),
                            spanStyle = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                            )
                        ),
                        onClick = {
                            loginViewModel.fillMockInfo()
                        }
                    )
                } else {
                    ClickableText(
                        text = AnnotatedString(
                            stringResource(R.string.clear_mock_info),
                            spanStyle = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                            )
                        ),
                        onClick = {
                            loginViewModel.clearMockInfo()
                        }
                    )
                }
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
                Text(stringResource(R.string.login))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    loginViewModel.onHideLoginDialogRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}