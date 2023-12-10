package com.dart.campushelper.ui.grade.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.North
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.South
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.ColumnCard
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel
import com.dart.campushelper.viewmodel.SortBasis

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterGradeBottomSheet(uiState: GradeUiState, viewModel: GradeViewModel) {
    BasicBottomSheet(
        isBottomSheetShow = uiState.openFilterSheet,
        title = R.string.filter_grade_title,
        onDismissRequest = { viewModel.setOpenFilterSheet(false) },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        actions = {
            TooltipIconButton(label = R.string.reset, imageVector = Icons.Outlined.RestartAlt) {
                viewModel.resetFilter()
                viewModel.filterGrades()
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            ColumnCard(
                title = stringResource(R.string.sort_title),
                icon = Icons.AutoMirrored.Outlined.Sort,
                isElevated = false,
            ) {
                SingleChoiceSegmentedButtonRow {
                    uiState.sortBasisList.forEachIndexed { index, item ->
                        SegmentedButton(
                            selected = item.selected,
                            onClick = {
                                viewModel.sortGradesBy(index, item)
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
                            Text(item.sortBasis.toString())
                        }
                    }
                }
            }
            // Semester filter section
            ColumnCard(
                title = stringResource(R.string.semester_title),
                icon = Icons.Outlined.Timeline,
                isElevated = false,
                actions = {
                    AssistChip(
                        onClick = {
                            (uiState.semesters?.find { !it.selected } != null).let {
                                uiState.semesters?.forEachIndexed { index, _ ->
                                    viewModel.changeSemesterSelected(
                                        index, it
                                    )
                                }
                                viewModel.filterGrades()
                            }
                        },
                        label = {
                            Text(
                                if (uiState.semesters?.find { !it.selected } != null)
                                    stringResource(R.string.select_all) else stringResource(R.string.deselect_all)
                            )
                        }
                    )
                }
            ) {
                if (uiState.semesters != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(-5.dp)
                    ) {
                        uiState.semesters.forEachIndexed { index, yearAndSemester ->
                            FilterChip(
                                selected = yearAndSemester.selected,
                                onClick = {
                                    viewModel.changeSemesterSelected(
                                        index,
                                        !yearAndSemester.selected
                                    )
                                    viewModel.filterGrades()
                                },
                                label = {
                                    Text(yearAndSemester.let {
                                        "${stringResource(it.yearResId)} ${
                                            stringResource(
                                                it.semesterResId
                                            )
                                        }"
                                    })
                                },
                                leadingIcon = {
                                    SegmentedButtonDefaults.Icon(
                                        active = yearAndSemester.selected,
                                        activeContent = {
                                            Icon(
                                                Icons.Outlined.Check,
                                                null,
                                                Modifier.size(SegmentedButtonDefaults.IconSize)
                                            )
                                        },
                                    )
                                }
                            )
                        }
                    }
                }
            }
            // Course type section
            ColumnCard(
                title = stringResource(R.string.course_type_title),
                icon = Icons.Outlined.Category,
                isElevated = false,
                actions = {
                    AssistChip(
                        onClick = {
                            (uiState.courseTypes?.find { !it.selected } != null).let {
                                uiState.courseTypes?.forEachIndexed { index, _ ->
                                    viewModel.changeCourseSortSelected(
                                        index, it
                                    )
                                }
                                viewModel.filterGrades()
                            }
                        },
                        label = {
                            Text(
                                if (uiState.courseTypes?.find { !it.selected } != null) stringResource(
                                    R.string.select_all
                                ) else stringResource(R.string.deselect_all)
                            )
                        }
                    )
                }
            ) {
                if (uiState.courseTypes != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(-5.dp)
                    ) {
                        uiState.courseTypes.forEachIndexed { index, courseType ->
                            FilterChip(
                                selected = courseType.selected,
                                onClick = {
                                    viewModel.changeCourseSortSelected(index, !courseType.selected)
                                    viewModel.filterGrades()
                                },
                                label = { Text(courseType.name) },
                                leadingIcon = {
                                    SegmentedButtonDefaults.Icon(
                                        active = courseType.selected,
                                        activeContent = {
                                            Icon(
                                                Icons.Outlined.Check,
                                                null,
                                                Modifier.size(SegmentedButtonDefaults.IconSize)
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}