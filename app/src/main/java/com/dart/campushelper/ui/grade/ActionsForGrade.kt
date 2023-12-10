package com.dart.campushelper.ui.grade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel

@Composable
fun ActionsForGrade(
    viewModel: GradeViewModel,
    uiState: GradeUiState,
) {
    AnimatedVisibility(
        visible = !uiState.isSearchBarShow,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        TooltipIconButton(
            label = R.string.search,
            imageVector = Icons.Outlined.Search,
            onClick = {
                viewModel.setIsSearchBarShow(true)
            },
        )
    }
    TooltipIconButton(
        label = R.string.data_summary,
        imageVector = Icons.Outlined.ShowChart,
        onClick = {
            viewModel.loadLineChart()
            viewModel.setOpenSummarySheet(true)
        },
    )
}
