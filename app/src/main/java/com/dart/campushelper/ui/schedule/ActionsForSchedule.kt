package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun ActionsForSchedule(viewModel: ScheduleViewModel, uiState: ScheduleUiState) {

    val scope = rememberCoroutineScope()
    var isMenuExpanded by remember { mutableStateOf(false) }

    TooltipIconButton(
        label = R.string.last_week,
        imageVector = Icons.Outlined.ChevronLeft,
        enabled = uiState.browsedWeek > 1,
        onClick = {
            viewModel.setBrowsedWeek(uiState.browsedWeek - 1)
        },
    )

    TooltipIconButton(
        label = R.string.next_week,
        imageVector = Icons.Outlined.ChevronRight,
        enabled = uiState.browsedWeek < 20,
        onClick = {
            viewModel.setBrowsedWeek(uiState.browsedWeek + 1)
        },
    )

    TooltipIconButton(
        label = R.string.more,
        imageVector = Icons.Outlined.MoreVert,
        onClick = {
            isMenuExpanded = true
        },
    )

    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = { isMenuExpanded = false },
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Outlined.Lightbulb, null)
            },
            text = { Text(stringResource(R.string.help)) },
            onClick = {
                scope.launch {
                    uiState.holdingCourseTooltipState.show()
                }
                isMenuExpanded = false
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Outlined.EditNote, null)
            },
            text = { Text(stringResource(R.string.schedule_other_info)) },
            onClick = {
                isMenuExpanded = false
                viewModel.setIsShowScheduleNotesSheet(true)
                viewModel.loadScheduleNotes()
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Outlined.Upcoming, null)
            },
            text = { Text(stringResource(R.string.plan_courses)) },
            onClick = {
                isMenuExpanded = false
                viewModel.setIsShowPlannedScheduleSheet(true)
                viewModel.loadPlannedSchedule()
            },
        )
    }

    var weekSliderPosition by remember { mutableFloatStateOf(uiState.browsedWeek.toFloat()) }
    var semesterDropdownMenuExpanded by remember { mutableStateOf(false) }
    var selectedSemester by remember { mutableStateOf(uiState.browsedSemester) }
    val hapticFeedback = LocalHapticFeedback.current

    if (uiState.isShowWeekSliderDialog) {
        AlertDialog(
            confirmButton = {
                TextButton({
                    viewModel.setBrowsedWeek(weekSliderPosition.toInt())
                    if (selectedSemester != uiState.browsedSemester) {
                        scope.launch {
                            viewModel.getCourses(selectedSemester)
                        }
                    }
                    viewModel.setBrowsedSemester(selectedSemester)
                    viewModel.setIsShowWeekSliderDialog(false)
                }) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton({
                    weekSliderPosition = uiState.currentWeek.toFloat()
                    viewModel.setBrowsedWeek(uiState.currentWeek)
                    selectedSemester = uiState.semesters.last()
                    if (selectedSemester != uiState.browsedSemester) {
                        scope.launch {
                            viewModel.getCourses(selectedSemester)
                        }
                    }
                    viewModel.setBrowsedSemester(uiState.semesters.last().toString())
                    viewModel.setIsShowWeekSliderDialog(false)
                }) {
                    Text(stringResource(R.string.reset))
                }
            },
            onDismissRequest = {
                weekSliderPosition = uiState.browsedWeek.toFloat()
                selectedSemester = uiState.browsedSemester
                viewModel.setIsShowWeekSliderDialog(false)
            },
            title = { Text(stringResource(R.string.switch_schedule)) },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                    ) {
                        Text(text = stringResource(R.string.switch_schedule))
                        Text(
                            text = String.format(
                                stringResource(
                                    R.string.week_indicator
                                ), weekSliderPosition.toInt()
                            ),
                            modifier = Modifier.weight(1F, true),
                            textAlign = TextAlign.Right,
                        )
                    }
                    Slider(
                        value = weekSliderPosition,
                        onValueChange = {
                            if (abs(it - weekSliderPosition) >= 0.9f) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            weekSliderPosition = it
                        },
                        steps = 20,
                        valueRange = 1f..20f,
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = stringResource(R.string.switch_year_semester))
                    Spacer(modifier = Modifier.height(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = semesterDropdownMenuExpanded,
                        onExpandedChange = {
                            semesterDropdownMenuExpanded = !semesterDropdownMenuExpanded
                        }
                    ) {
                        TextField(
                            modifier = Modifier.menuAnchor(),
                            readOnly = true,
                            value = selectedSemester,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.selected_year_semester)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterDropdownMenuExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = semesterDropdownMenuExpanded,
                            onDismissRequest = { semesterDropdownMenuExpanded = false }
                        ) {
                            uiState.semesters.forEach { semester ->
                                DropdownMenuItem(
                                    text = {
                                        Text(semester)
                                    },
                                    onClick = {
                                        selectedSemester = semester
                                        semesterDropdownMenuExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    modifier = if (semester == selectedSemester) {
                                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                    } else {
                                        Modifier
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
