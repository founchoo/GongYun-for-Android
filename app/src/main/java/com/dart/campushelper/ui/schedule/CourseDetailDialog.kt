package com.dart.campushelper.ui.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.listitem.BasicListItem
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel

@Composable
fun CourseDetailDialog(
    uiState: ScheduleUiState,
    viewModel: ScheduleViewModel,
) {
    if (uiState.isCourseDetailDialogOpen) {
        AlertDialog(
            confirmButton = {},
            dismissButton = {
                TextButton({
                    viewModel.setIsCourseDetailDialogOpen(false)
                }) {
                    Text(stringResource(R.string.close))
                }
            },
            onDismissRequest = { viewModel.setIsCourseDetailDialogOpen(false) },
            title = { Text(stringResource(R.string.schedule_switcher_title)) },
            text = {
                Column {
                    val coursesOnBrowsedWeek =
                        uiState.contentInCourseDetailDialog.filter { it.weekNoList.contains(uiState.browsedWeek) }
                    val coursesOnNonBrowsedWeek =
                        uiState.contentInCourseDetailDialog.filter { !it.weekNoList.contains(uiState.browsedWeek) }
                    if (coursesOnBrowsedWeek.isNotEmpty()) {
                        BasicListItem(leadingText = stringResource(R.string.current_observed_week_course_title))
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            coursesOnBrowsedWeek.forEach { course ->
                                ListItem(
                                    headlineContent = { Text("${course.courseName}") },
                                    supportingContent = {
                                        Text(
                                            "${course.classroomName} / ${
                                                String.format(
                                                    stringResource(
                                                        R.string.week_indicator
                                                    ), course.zc
                                                )
                                            }"
                                        )
                                    },
                                    trailingContent = { Text("${course.teacherName}") }
                                )
                            }
                        }
                    }
                    if (coursesOnNonBrowsedWeek.isNotEmpty()) {
                        BasicListItem(leadingText = stringResource(R.string.non_current_observed_week_course_title))
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            coursesOnNonBrowsedWeek.forEach { course ->
                                ListItem(
                                    headlineContent = { Text("${course.courseName}") },
                                    supportingContent = {
                                        Text(
                                            "${course.classroomName} / ${
                                                String.format(
                                                    stringResource(
                                                        R.string.week_indicator
                                                    ), course.zc
                                                )
                                            }"
                                        )
                                    },
                                    trailingContent = { Text("${course.teacherName}") }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}