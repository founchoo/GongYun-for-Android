package com.dart.campushelper.ui.grade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingActionButtonForGrade(uiState: GradeUiState, viewModel: GradeViewModel) {

    val density = LocalDensity.current
    AnimatedVisibility(
        visible = uiState.fabVisibility ?: false,
        enter = slideInHorizontally {
            with(density) { 80.dp.roundToPx() }
        },
        exit = slideOutHorizontally {
            with(density) { 80.dp.roundToPx() }
        },
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(stringResource(R.string.filter_grade_title))
                }
            },
            state = rememberTooltipState()
        ) {
            FloatingActionButton(
                onClick = {
                    viewModel.setOpenFilterSheet(true)
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterAlt,
                    contentDescription = null,
                )
            }
        }
    }
}