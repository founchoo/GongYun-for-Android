package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@Composable
fun ActionsForSchedule(viewModel: ScheduleViewModel, uiState: ScheduleUiState) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    TooltipIconButton(
        label = R.string.help,
        imageVector = Icons.Outlined.Lightbulb,
        onClick = {
            viewModel.viewModelScope.launch {
                uiState.holdingCourseTooltipState.show()
            }
        },
    )

    TooltipIconButton(
        label = R.string.switch_schedule,
        imageVector = Icons.Outlined.Tune,
        onClick = {
            viewModel.setIsShowWeekSliderDialog(true)
        },
    )

    TooltipIconButton(
        label = R.string.more,
        imageVector = Icons.Outlined.MoreVert,
        onClick = {
            isMenuExpanded = true
        },
    )

    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = { isMenuExpanded = false },
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Outlined.EditNote, null)
            },
            text = { Text(stringResource(R.string.schedule_other_info)) },
            onClick = {
                isMenuExpanded = false
                viewModel.setIsShowScheduleNotesSheet(true)
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Outlined.Upcoming, null)
            },
            text = { Text(stringResource(R.string.plan_courses)) },
            onClick = {
                isMenuExpanded = false
                viewModel.setIsShowPlannedScheduleSheet(true)
            },
        )
    }
}
