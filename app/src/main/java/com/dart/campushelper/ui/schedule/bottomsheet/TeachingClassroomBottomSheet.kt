package com.dart.campushelper.ui.schedule.bottomsheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
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
import com.dart.campushelper.ui.component.HorizontalPagerTabRow
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.utils.Constants
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
            autoLoadingArgs = arrayOf(
                uiState.dayOfWeekOnHoldingCourse,
                uiState.nodeNoOnHoldingCourse
            ),
            autoLoadWhenDataLoaded = true,
            contentWhenDataSourceIsEmpty = {
                Text(
                    text = stringResource(
                        R.string.occupied_room_without_content_desc,
                        *args
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = Constants.DEFAULT_PADDING)
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
                        modifier = Modifier.padding(horizontal = Constants.DEFAULT_PADDING)
                    )
                    HorizontalPagerTabRow(
                        tabs = uiState.buildingNames,
                        dataSource = courses,
                    ) { data, target ->
                        LazyColumn {
                            items(data.filter { it.classroomName?.split("-")?.get(0) == target }) {
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
                    }
                }
            },
        )
    }
}