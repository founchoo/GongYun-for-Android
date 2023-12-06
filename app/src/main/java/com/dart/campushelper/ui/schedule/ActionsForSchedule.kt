package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
        label = R.string.last_week,
        imageVector = Icons.Outlined.ChevronLeft,
        enabled = uiState.browsedWeek > 1,
        onClick = {
            viewModel.setBrowsedWeek(uiState.browsedWeek - 1)
        },
    )

    TooltipIconButton(
        label = R.string.next_week,
        imageVector = Icons.Outlined.ChevronRight,
        enabled = uiState.browsedWeek < 20,
        onClick = {
            viewModel.setBrowsedWeek(uiState.browsedWeek + 1)
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
                Icon(Icons.Outlined.Lightbulb, null)
            },
            text = { Text(stringResource(R.string.help)) },
            onClick = {
                viewModel.viewModelScope.launch {
                    uiState.holdingCourseTooltipState.show()
                }
                isMenuExpanded = false
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Outlined.EditNote, null)
            },
            text = { Text(stringResource(R.string.schedule_other_info)) },
            onClick = {
                isMenuExpanded = false
                viewModel.setIsShowScheduleNotesSheet(true)
                viewModel.viewModelScope.launch {
                    viewModel.loadScheduleNotes()
                }
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
                viewModel.loadPlannedSchedule()
            },
        )
    }
}
