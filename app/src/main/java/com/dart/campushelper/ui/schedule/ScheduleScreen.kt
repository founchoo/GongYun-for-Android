package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheetWithDataList
import com.dart.campushelper.utils.combinationOfGradeAndSemesterToText
import com.dart.campushelper.utils.convertDayOfWeekToChinese
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val args = arrayOf(
        uiState.browsedWeek.toString(),
        convertDayOfWeekToChinese(
            uiState.dayOfWeekOnHoldingCourse
        ),
        "${uiState.nodeNoOnHoldingCourse * 2 - 1} - ${uiState.nodeNoOnHoldingCourse * 2}"
    )

    ScheduleTable(uiState = uiState, viewModel = viewModel)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(
            min(screenHeight, screenWidth) / 2
        ),
        tooltip = {
            RichTooltip(
                title = {
                    Text(
                        stringResource(R.string.check_room_tip_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                action = {
                    TextButton(
                        onClick = {
                            uiState.holdingCourseTooltipState.dismiss()
                            scope.launch {
                                uiState.holdingSemesterTooltipState.show()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.show_next_tip))
                    }
                }) {
                Text(stringResource(R.string.check_room_tip_desc))
            }
        },
        state = uiState.holdingCourseTooltipState
    ) {
    }

    CourseDetailDialog(uiState = uiState, viewModel = viewModel)


    BasicBottomSheetWithDataList(
        isBottomSheetShow = uiState.isShowNonEmptyClassroomSheet,
        title = R.string.occupied_room,
        descriptionWhenItemSourceIsEmpty = stringResource(
            R.string.occupied_room_without_content_desc,
            *args
        ),
        descriptionWhenItemSourceIsNotEmpty = stringResource(
            R.string.occupied_room_with_content_desc,
            *args
        ),
        isContentLoading = uiState.isNonEmptyClrLoading,
        itemSource = uiState.nonEmptyClassrooms,
        onDismissRequest = {
            viewModel.setIsShowNonEmptyClassroomSheet(false)
        }
    ) {
        var buildingIndexSelected by remember { mutableIntStateOf(0) }
        SecondaryScrollableTabRow(
            selectedTabIndex = buildingIndexSelected,
        ) {
            uiState.buildingNames.forEachIndexed { index, buildingName ->
                Tab(
                    text = { Text(buildingName) },
                    selected = buildingIndexSelected == index,
                    onClick = { buildingIndexSelected = index },
                )
            }
        }
        LazyColumn {
            items(it.filter {
                (it.classroomName?.split("-")?.get(0)
                    ?: "") == uiState.buildingNames[buildingIndexSelected]
            }) {
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

    BasicBottomSheetWithDataList(
        isBottomSheetShow = uiState.isShowEmptyClassroomSheet,
        title = R.string.free_room,
        descriptionWhenItemSourceIsEmpty = stringResource(
            R.string.empty_room_without_content_desc,
            *args
        ),
        descriptionWhenItemSourceIsNotEmpty = stringResource(
            R.string.empty_room_with_content_desc,
            *args
        ),
        isContentLoading = uiState.isEmptyClrLoading,
        itemSource = uiState.emptyClassrooms,
        onDismissRequest = {
            viewModel.setIsShowEmptyClassroomSheet(false)
        }
    ) {
        var buildingIndexSelected by remember { mutableIntStateOf(0) }
        SecondaryScrollableTabRow(
            selectedTabIndex = buildingIndexSelected,
        ) {
            uiState.buildingNames.forEachIndexed { index, buildingName ->
                Tab(
                    text = { Text(buildingName) },
                    selected = buildingIndexSelected == index,
                    onClick = { buildingIndexSelected = index },
                )
            }
        }
        LazyColumn {
            items(it.filter {
                it.buildingName == uiState.buildingNames[buildingIndexSelected]
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

    BasicBottomSheetWithDataList(
        isBottomSheetShow = uiState.isShowScheduleNotesSheet,
        title = R.string.schedule_other_info,
        descriptionWhenItemSourceIsEmpty = R.string.schedule_other_info_empty,
        isContentLoading = uiState.isScheduleNotesLoading,
        itemSource = uiState.scheduleNotes,
        onDismissRequest = {
            viewModel.setIsShowScheduleNotesSheet(false)
        }
    ) {
        LazyColumn {
            items(it) {
                ListItem(
                    headlineContent = { Text(text = it.title) },
                    supportingContent = { Text(text = it.description) },
                )
            }
        }
    }

    BasicBottomSheetWithDataList(
        isBottomSheetShow = uiState.isShowPlannedScheduleSheet,
        title = R.string.planned_schedule_info_title,
        descriptionWhenItemSourceIsEmpty = R.string.planned_schedule_info_empty,
        isContentLoading = uiState.isPlannedScheduleLoading,
        itemSource = uiState.plannedSchedule,
        onDismissRequest = {
            viewModel.setIsShowPlannedScheduleSheet(false)
        }
    ) {
        LazyColumn {
            items(it) {
                ListItem(
                    headlineContent = { Text(text = it.courseName.toString()) },
                    supportingContent = { Text(text = it.hostInstituteName.toString()) },
                    trailingContent = {
                        Text(
                            text = combinationOfGradeAndSemesterToText(
                                (it.grade ?: "0").toInt(),
                                (it.semester ?: "0").toInt()
                            )
                        )
                    }
                )
            }
        }
    }
}
