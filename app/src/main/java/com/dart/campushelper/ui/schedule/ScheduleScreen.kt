package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.dart.campushelper.utils.DayOfWeek
import com.dart.campushelper.viewmodel.ScheduleViewModel

@SuppressLint("UnrememberedMutableState")
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
    ScheduleTable(uiState, viewModel)

    // Tooltips
    CourseHoldingTooltip(uiState, viewModel)

    // Dialogs
    CourseDetailDialog(uiState, viewModel)

    // Bottom sheets
    WeekSliderBottomSheet(uiState, viewModel)
    TeachingClassroomBottomSheet(uiState, viewModel, *args)
    EmptyClassroomBottomSheet(uiState, viewModel, *args)
    ScheduleNotesBottomSheet(uiState, viewModel)
    PlannedScheduleBottomSheet(uiState, viewModel)
}
