package com.dart.campushelper.ui.grade

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.R
import com.dart.campushelper.model.Grade
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.viewmodel.GradeUiState

@Composable
fun GradeDetailDialog(
    uiState: GradeUiState,
    grade: Grade,
    onDismissRequest: () -> Unit,
) {
    if (uiState.isGradeDetailDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                onDismissRequest()
            },
            title = {
                Text(text = grade.name)
            },
            text = {
                Column {
                    ListItem(
                        headlineContent = {
                            Text(
                                "${stringResource(R.string.gpa_label)} ${
                                    grade.gradePoint.toString()
                                        .replaceWithStars(uiState.isScreenshotMode)
                                }"
                            )
                        },
                        supportingContent = {
                            Text(
                                grade.composition.replaceWithStars(uiState.isScreenshotMode)
                            )
                        },
                        trailingContent = {
                            Text(
                                "${grade.yearAndSemester}\n" +
                                        "${uiState.courseTypes?.find { it.value == (grade.courseTypeRaw) }?.label}\n" +
                                        "${stringResource(R.string.credit_label)} ${grade.credit}"
                            )
                        },
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }
}
