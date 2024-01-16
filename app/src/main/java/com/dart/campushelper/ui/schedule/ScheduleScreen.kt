package com.dart.campushelper.ui.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dart.campushelper.ui.schedule.bottomsheet.EmptyClassroomBottomSheet
import com.dart.campushelper.ui.schedule.bottomsheet.PlannedScheduleBottomSheet
import com.dart.campushelper.ui.schedule.bottomsheet.ScheduleNotesBottomSheet
import com.dart.campushelper.ui.schedule.bottomsheet.TeacherScheduleBottomSheet
import com.dart.campushelper.ui.schedule.bottomsheet.TeachingClassroomBottomSheet
import com.dart.campushelper.ui.schedule.bottomsheet.WeekSliderBottomSheet
import com.dart.campushelper.utils.DayOfWeek
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val args = arrayOf(
        uiState.browsedWeek.toString(),
        DayOfWeek.instance.convertDayOfWeekToText(
            uiState.dayOfWeekOnHoldingCourse
        ),
        "${uiState.nodeNoOnHoldingCourse * 2 - 1} - ${uiState.nodeNoOnHoldingCourse * 2}"
    )

    // Home
    if (uiState.currentWeek != null) {
        val pagerState =
            rememberPagerState(initialPage = max(0, uiState.currentWeek!! - 1), pageCount = { 20 })
        ScheduleTable(uiState, viewModel, pagerState)
        WeekSliderBottomSheet(uiState, viewModel, pagerState)
    }

    // Tooltips
    CourseHoldingTooltip(uiState, viewModel)

    // Dialogs
    CourseDetailDialog(uiState, viewModel)

    // Bottom sheets
    TeachingClassroomBottomSheet(uiState, viewModel, *args)
    EmptyClassroomBottomSheet(uiState, viewModel, *args)
    ScheduleNotesBottomSheet(uiState, viewModel)
    PlannedScheduleBottomSheet(uiState, viewModel)
    TeacherScheduleBottomSheet(uiState, viewModel)
}
