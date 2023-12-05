package com.dart.campushelper.ui.grade

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.North
import androidx.compose.material.icons.outlined.South
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.ColumnCard
import com.dart.campushelper.ui.component.isScrollingUp
import com.dart.campushelper.viewmodel.GradeViewModel
import com.dart.campushelper.viewmodel.SortBasis
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.placeholder
import io.github.fornewid.placeholder.material3.shimmer
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi", "CoroutineCreationDuringComposition", "UnrememberedMutableState")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class, ExperimentalMaterialApi::class,
)
@Composable
fun GradeScreen(
    viewModel: GradeViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    val listState = rememberLazyListState()
    val isScrollingUp by mutableStateOf(listState.isScrollingUp())
    val scope = rememberCoroutineScope()
    fun refresh() = scope.launch {
        viewModel.getGrades()
    }

    val pullRefreshState = rememberPullRefreshState(uiState.isGradesLoading, ::refresh)

    viewModel.setFabVisibility(isScrollingUp)

    Box(
        modifier = Modifier
            .pullRefresh(pullRefreshState)
            .semantics { isTraversalGroup = true },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .placeholder(
                    visible = uiState.isGradesLoading,
                    highlight = PlaceholderHighlight.shimmer()
                ),
            verticalArrangement = Arrangement.SpaceAround,
            state = listState,
        ) {
            if (uiState.grades.isEmpty()) {
                item {
                    Text(
                        text = if (uiState.searchKeyword.isEmpty()) stringResource(R.string.no_grades) else stringResource(
                            R.string.no_query_grades
                        ),
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                items(uiState.grades) { grade ->
                    GradeItem(grade, uiState, viewModel)
                }
            }
        }
        PullRefreshIndicator(
            uiState.isGradesLoading,
            pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }

    GradeDetailDialog(
        uiState = uiState,
        grade = uiState.contentForGradeDetailDialog,
        onDismissRequest = {
            viewModel.setIsGradeDetailDialogOpen(false)
        }
    )

    BasicBottomSheet(
        isBottomSheetShow = uiState.openFilterSheet,
        title = R.string.filter_grade_title,
        onDismissRequest = { viewModel.setOpenFilterSheet(false) },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
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
                            val value = uiState.semestersSelected.containsValue(false)
                            uiState.semesters.forEach { semester ->
                                viewModel.changeSemesterSelected(
                                    semester, value
                                )
                            }
                            viewModel.filterGrades()
                        },
                        label = {
                            Text(
                                if (uiState.semestersSelected.containsValue(false))
                                    stringResource(R.string.select_all) else stringResource(R.string.deselect_all)
                            )
                        }
                    )
                }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(-5.dp)
                ) {
                    for (semester in uiState.semesters) {
                        FilterChip(
                            selected = uiState.semestersSelected[semester] ?: false,
                            onClick = {
                                viewModel.changeSemesterSelected(
                                    semester,
                                    !(uiState.semestersSelected[semester] ?: false)
                                )
                                viewModel.filterGrades()
                            },
                            label = { Text(semester) },
                            leadingIcon = {
                                SegmentedButtonDefaults.Icon(
                                    active = uiState.semestersSelected[semester] == true,
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
            // Course type section
            ColumnCard(
                title = stringResource(R.string.course_type_title),
                icon = Icons.Outlined.Category,
                isElevated = false,
                actions = {
                    AssistChip(
                        onClick = {
                            val value = uiState.courseTypesSelected.containsValue(false)
                            uiState.courseTypes.forEach { semester ->
                                viewModel.changeCourseSortSelected(
                                    semester, value
                                )
                            }
                            viewModel.filterGrades()
                        },
                        label = {
                            Text(
                                if (uiState.courseTypesSelected.containsValue(false)) stringResource(
                                    R.string.select_all
                                ) else stringResource(R.string.deselect_all)
                            )
                        }
                    )
                }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(-5.dp)
                ) {
                    for (courseType in uiState.courseTypes) {
                        FilterChip(
                            selected = uiState.courseTypesSelected[courseType] ?: false,
                            onClick = {
                                viewModel.changeCourseSortSelected(
                                    courseType,
                                    !(uiState.courseTypesSelected[courseType] ?: false)
                                )
                                viewModel.filterGrades()
                            },
                            label = { Text(courseType) },
                            leadingIcon = {
                                SegmentedButtonDefaults.Icon(
                                    active = uiState.courseTypesSelected[courseType] == true,
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
