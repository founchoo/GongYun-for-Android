package com.dart.campushelper.ui.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

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
                viewModel.viewModelScope.launch {
                    viewModel.loadEmptyClassroom(
                        uiState.dayOfWeekOnHoldingCourse,
                        uiState.nodeNoOnHoldingCourse
                    )
                }
            },
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
                val pagerState = rememberPagerState { uiState.buildingNames?.size ?: 0 }
                Column {
                    Text(
                        text = stringResource(
                            R.string.empty_room_with_content_desc,
                            *args
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    SecondaryScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                    ) {
                        uiState.buildingNames?.forEachIndexed { index, buildingName ->
                            Tab(
                                text = { Text(buildingName) },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    viewModel.viewModelScope.launch {
                                        pagerState.scrollToPage(
                                            index
                                        )
                                    }
                                },
                            )
                        }
                    }
                    HorizontalPager(state = pagerState) { pageIndex ->
                        LazyColumn {
                            items(classrooms.filter {
                                it.buildingName == uiState.buildingNames?.get(pageIndex)
                            }) {
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