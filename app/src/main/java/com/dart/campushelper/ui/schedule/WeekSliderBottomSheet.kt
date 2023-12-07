package com.dart.campushelper.ui.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.DropdownMenuPreference
import com.dart.campushelper.ui.component.SliderPreference
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSliderBottomSheet(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {
    BasicBottomSheet(
        isBottomSheetShow = uiState.isShowWeekSliderDialog,
        title = R.string.switch_schedule,
        onDismissRequest = { viewModel.setIsShowWeekSliderDialog(false) },
        actions = {
            TooltipIconButton(label = R.string.reset, imageVector = Icons.Outlined.RestartAlt) {
                viewModel.setBrowsedSemester(uiState.semesters.last())
                uiState.currentWeek?.let { viewModel.setBrowsedWeek(it) }
            }
        },
    ) {
        Column {
            SliderPreference(
                value = uiState.browsedWeek?.toFloat() ?: 0f,
                minValue = 1f,
                maxValue = 20f,
                title = stringResource(
                    R.string.switch_week
                ),
                description = stringResource(
                    R.string.week_indicator, uiState.browsedWeek ?: 0
                ),
                onValueChanged = {
                    viewModel.setBrowsedWeek(it.toInt())
                },
            )
            DropdownMenuPreference(
                value = uiState.browsedSemester,
                title = stringResource(R.string.switch_year_semester),
                selections = uiState.semesters,
                onValueChanged = { _, value ->
                    viewModel.setBrowsedSemester(value)
                }
            )
        }
    }
}