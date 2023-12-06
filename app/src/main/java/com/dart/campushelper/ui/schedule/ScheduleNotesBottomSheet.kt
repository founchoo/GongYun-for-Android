package com.dart.campushelper.ui.schedule

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

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
                viewModel.viewModelScope.launch {
                    viewModel.loadScheduleNotes()
                }
            },
            contentWhenDataSourceIsEmpty = {
                Text(
                    text = stringResource(R.string.schedule_other_info_empty),
                    style = MaterialTheme.typography.bodySmall,
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