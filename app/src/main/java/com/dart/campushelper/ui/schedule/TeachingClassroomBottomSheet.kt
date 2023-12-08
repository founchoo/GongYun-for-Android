package com.dart.campushelper.ui.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.HorizontalPagerTabRow
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeachingClassroomBottomSheet(
    uiState: ScheduleUiState,
    viewModel: ScheduleViewModel,
    vararg args: Any
) {
    BasicBottomSheet(
        isBottomSheetShow = uiState.isShowTeachingClassroomSheet,
        title = R.string.occupied_room,
        onDismissRequest = {
            viewModel.setIsShowNonEmptyClassroomSheet(false)
        }
    ) {
        LoadOnlineDataLayout(
            dataSource = uiState.teachingClassrooms,
            loadData = {
                viewModel.loadTeachingClassrooms()
            },
            autoLoadingArgs = arrayOf(uiState.dayOfWeekOnHoldingCourse, uiState.nodeNoOnHoldingCourse),
            autoLoadWhenDataLoaded = true,
            contentWhenDataSourceIsEmpty = {
                Text(
                    text = stringResource(
                        R.string.occupied_room_without_content_desc,
                        *args
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            contentWhenDataSourceIsNotEmpty = { courses ->
                Column {
                    Text(
                        text = stringResource(
                            R.string.occupied_room_with_content_desc,
                            *args
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    HorizontalPagerTabRow(
                        tabNames = uiState.buildingNames,
                        dataSource = courses,
                        pairRule = {
                            it.classroomName?.split("-")?.get(0)
                                ?: ""
                        },
                    ) {
                        ListItem(
                            headlineContent = { Text(text = "${it.classroomNameHtml ?: ""} | ${it.courseName ?: ""}") },
                            trailingContent = { Text(text = it.teacherName ?: "") },
                            supportingContent = {
                                Text(
                                    text = it.className?.replace(
                                        ",",
                                        " | "
                                    ) ?: ""
                                )
                            },
                        )
                    }
                }
            },
        )
    }
}