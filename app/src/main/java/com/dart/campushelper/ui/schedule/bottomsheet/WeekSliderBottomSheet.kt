package com.dart.campushelper.ui.schedule.bottomsheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.ui.component.listitem.DropdownListItem
import com.dart.campushelper.ui.component.listitem.SelectionItem
import com.dart.campushelper.ui.component.listitem.SliderListItem
import com.dart.campushelper.utils.AcademicYearAndSemester
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WeekSliderBottomSheet(
    uiState: ScheduleUiState,
    viewModel: ScheduleViewModel,
    pagerState: PagerState
) {
    BasicBottomSheet(
        isBottomSheetShow = uiState.isShowWeekSliderDialog,
        title = R.string.switch_schedule,
        onDismissRequest = { viewModel.setIsShowWeekSliderDialog(false) },
        actions = {
            TooltipIconButton(label = R.string.reset, imageVector = Icons.Outlined.RestartAlt) {
                viewModel.viewModelScope.launch {
                    uiState.currentWeek?.let { pagerState.scrollToPage(it - 1) }
                }
                viewModel.setBrowsedSemester(uiState.semesters.last())
            }
        },
    ) {
        Column {
            SliderListItem(
                value = uiState.browsedWeek?.toFloat() ?: 0f,
                minValue = 1f,
                maxValue = 20f,
                headlineText = stringResource(
                    R.string.switch_week
                ),
                supportingText = stringResource(
                    R.string.week_indicator, uiState.browsedWeek ?: 0
                ),
                onValueChanged = {
                    viewModel.viewModelScope.launch {
                        pagerState.scrollToPage(it.toInt() - 1)
                    }
                },
            )
            DropdownListItem(
                value = uiState.browsedSemester,
                headlineText = stringResource(R.string.switch_year_semester),
                selections = uiState.semesters.map {
                    SelectionItem(
                        AcademicYearAndSemester.getReadableString(
                            uiState.semesters.first(),
                            it
                        ), it
                    )
                },
                onValueChanged = { _, value ->
                    viewModel.setBrowsedSemester(value)
                }
            )
        }
    }
}