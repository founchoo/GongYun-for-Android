package com.dart.campushelper.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSliderDialog(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {
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
                        viewModel.viewModelScope.launch {
                            viewModel.loadSchedule(selectedSemester)
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
                        viewModel.viewModelScope.launch {
                            viewModel.loadSchedule(selectedSemester)
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
                            text = stringResource(
                                R.string.week_indicator, weekSliderPosition.toInt().toString()
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