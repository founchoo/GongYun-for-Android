package com.dart.campushelper.ui.schedule

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseHoldingTooltip(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

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
                            viewModel.viewModelScope.launch {
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
}