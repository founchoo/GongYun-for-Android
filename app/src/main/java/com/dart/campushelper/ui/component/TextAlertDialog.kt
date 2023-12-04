package com.dart.campushelper.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.R

@Composable
fun TextAlertDialog(
    isShowDialog: Boolean,
    actionAfterConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    titleText: String = stringResource(R.string.dialog_title),
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
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}