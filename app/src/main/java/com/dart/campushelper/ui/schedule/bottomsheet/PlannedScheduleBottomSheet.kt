package com.dart.campushelper.ui.schedule.bottomsheet

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.utils.AcademicYearAndSemester.Companion.getReadableString
import com.dart.campushelper.utils.Constants
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedScheduleBottomSheet(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {
    BasicBottomSheet(
        isBottomSheetShow = uiState.isShowPlannedScheduleSheet,
        title = R.string.planned_schedule_info_title,
        onDismissRequest = {
            viewModel.setIsShowPlannedScheduleSheet(false)
        }
    ) {
        LoadOnlineDataLayout(
            dataSource = uiState.plannedSchedule,
            loadData = { viewModel.loadPlannedSchedule() },
            contentWhenDataSourceIsEmpty = {
                Text(
                    text = stringResource(R.string.planned_schedule_info_empty),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = Constants.DEFAULT_PADDING)
                )
            },
            contentWhenDataSourceIsNotEmpty = {
                LazyColumn {
                    items(it) {
                        ListItem(
                            headlineContent = { Text(text = it.courseName.toString()) },
                            supportingContent = { Text(text = it.hostInstituteName.toString()) },
                            trailingContent = {
                                Text(
                                    text = getReadableString(
                                        (it.grade ?: "0").toInt(),
                                        (it.semester ?: "0").toInt()
                                    )
                                )
                            }
                        )
                    }
                }
            },
        )
    }
}