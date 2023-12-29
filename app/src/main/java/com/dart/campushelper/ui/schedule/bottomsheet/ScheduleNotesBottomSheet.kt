package com.dart.campushelper.ui.schedule.bottomsheet

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.utils.Constants
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleNotesBottomSheet(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {
    BasicBottomSheet(
        isBottomSheetShow = uiState.isShowScheduleNotesSheet,
        title = R.string.schedule_other_info,
        onDismissRequest = {
            viewModel.setIsShowScheduleNotesSheet(false)
        }
    ) {
        LoadOnlineDataLayout(
            dataSource = uiState.scheduleNotes,
            loadData = {
                viewModel.loadScheduleNotes()
            },
            contentWhenDataSourceIsEmpty = {
                Text(
                    text = stringResource(R.string.schedule_other_info_empty),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = Constants.DEFAULT_PADDING)
                )
            },
            contentWhenDataSourceIsNotEmpty = {
                LazyColumn {
                    items(it) {
                        ListItem(
                            headlineContent = { Text(text = it.title) },
                            supportingContent = { Text(text = it.description) },
                        )
                    }
                }
            }
        )
    }
}