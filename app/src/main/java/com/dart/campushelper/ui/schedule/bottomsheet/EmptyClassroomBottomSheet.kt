package com.dart.campushelper.ui.schedule.bottomsheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun EmptyClassroomBottomSheet(
    uiState: ScheduleUiState,
    viewModel: ScheduleViewModel,
    vararg args: Any
) {
    BasicBottomSheet(
        isBottomSheetShow = uiState.isShowEmptyClassroomSheet,
        title = R.string.free_room,
        onDismissRequest = {
            viewModel.setIsShowEmptyClassroomSheet(false)
        }
    ) {
        LoadOnlineDataLayout(
            dataSource = uiState.emptyClassrooms,
            loadData = {
                viewModel.loadEmptyClassroom()
            },
            autoLoadingArgs = arrayOf(
                uiState.dayOfWeekOnHoldingCourse,
                uiState.nodeNoOnHoldingCourse
            ),
            autoLoadWhenDataLoaded = true,
            contentWhenDataSourceIsEmpty = {
                Text(
                    text = stringResource(
                        R.string.empty_room_without_content_desc,
                        *args
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            contentWhenDataSourceIsNotEmpty = { classrooms ->
                Column {
                    Text(
                        text = stringResource(
                            R.string.empty_room_with_content_desc,
                            *args
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    HorizontalPagerTabRow(
                        tabs = uiState.buildingNames,
                        dataSource = classrooms,
                    ) { data, target ->
                        LazyColumn {
                            items(data.filter { it.buildingName == target }) {
                                ListItem(
                                    headlineContent = { Text(text = it.roomName ?: "") },
                                    supportingContent = {
                                        Text(
                                            text = "${it.buildingName}${
                                                it.floor.let { floor ->
                                                    if (floor == null) {
                                                        ""
                                                    } else {
                                                        " | $floor 楼"
                                                    }
                                                }
                                            }"
                                        )
                                    },
                                    trailingContent = {
                                        Text(
                                            text = it.functionalAreaName ?: ""
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