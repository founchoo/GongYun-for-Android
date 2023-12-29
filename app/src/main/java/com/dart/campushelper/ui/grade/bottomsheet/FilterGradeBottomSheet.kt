package com.dart.campushelper.ui.grade.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.North
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.South
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.ColumnCard
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.utils.AcademicYearAndSemester.Companion.getReadableString
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel
import com.dart.campushelper.viewmodel.SortBasis
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterGradeBottomSheet(uiState: GradeUiState, viewModel: GradeViewModel) {

    BasicBottomSheet(
        isBottomSheetShow = uiState.openFilterSheet,
        title = R.string.filter_grade_title,
        onDismissRequest = { viewModel.setOpenFilterSheet(false) },
        actions = {
            TooltipIconButton(label = R.string.reset, imageVector = Icons.Outlined.RestartAlt) {
                viewModel.viewModelScope.launch {
                    viewModel.resetFilter()
                    viewModel.filterGrades()
                }
            }
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.verticalScroll(
                rememberScrollState()
            )
        ) {
            ColumnCard(
                title = stringResource(R.string.sort_title),
                icon = Icons.AutoMirrored.Outlined.Sort,
                isElevated = false,
            ) {
                SingleChoiceSegmentedButtonRow(Modifier.padding(top = 8.dp)) {
                    uiState.sortBasisList.forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = item.selected,
                            onClick = {
                                viewModel.sortGradesBy(item)
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = SortBasis.values().size
                            ),
                            icon = {
                                if (item.asc) {
                                    SegmentedButtonDefaults.Icon(
                                        active = item.selected,
                                        activeContent = {
                                            Icon(
                                                Icons.Outlined.North,
                                                null,
                                                Modifier.size(SegmentedButtonDefaults.IconSize)
                                            )
                                        },
                                    )
                                } else {
                                    SegmentedButtonDefaults.Icon(
                                        active = item.selected,
                                        activeContent = {
                                            Icon(
                                                Icons.Outlined.South,
                                                null,
                                                Modifier.size(SegmentedButtonDefaults.IconSize)
                                            )
                                        },
                                    )
                                }
                            }
                        ) {
                            Text(stringResource(id = item.sortBasis.getResId()))
                        }
                    }
                }
            }
            // Semester filter section
            if (uiState.semesters != null) {
                ColumnCard(
                    title = stringResource(R.string.semester_title),
                    icon = Icons.Outlined.Timeline,
                    isElevated = false,
                    actions = {
                        Checkbox(
                            checked = uiState.semesters.find { !it.selected } == null,
                            onCheckedChange = { viewModel.switchAllSemesterSelected() }
                        )
                    }
                ) {
                    Column {
                        val startSemester = uiState.semesters.firstOrNull()
                        uiState.semesters.forEachIndexed { _, item ->
                            DropdownMenuItem(
                                trailingIcon = {
                                    Checkbox(checked = item.selected, onCheckedChange = {
                                        viewModel.switchSemesterSelected(item)
                                    })
                                },
                                text = {
                                    Text(startSemester?.let {
                                        getReadableString(
                                            it.value,
                                            item.value
                                        )
                                    } ?: "")
                                },
                                onClick = { viewModel.switchSemesterSelected(item) }
                            )
                        }
                    }
                }
            }
            // Course type section
            if (uiState.courseTypes != null) {
                ColumnCard(
                    title = stringResource(R.string.course_type_title),
                    icon = Icons.Outlined.Category,
                    isElevated = false,
                    actions = {
                        Checkbox(
                            checked = uiState.courseTypes.find { !it.selected } == null,
                            onCheckedChange = { viewModel.switchAllCourseTypeSelected() }
                        )
                    }
                ) {
                    Column {
                        uiState.courseTypes.forEachIndexed { _, item ->
                            DropdownMenuItem(
                                trailingIcon = {
                                    Checkbox(checked = item.selected, onCheckedChange = {
                                        viewModel.switchCourseSortSelected(item)
                                    })
                                },
                                text = { Text(item.label) },
                                onClick = { viewModel.switchCourseSortSelected(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}