package com.dart.campushelper.ui.schedule.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.ui.component.NoBorderTextField
import com.dart.campushelper.ui.main.focusSearchBarRequester
import com.dart.campushelper.utils.Constants
import com.dart.campushelper.utils.Constants.Companion.DEFAULT_PADDING
import com.dart.campushelper.utils.parseHtml
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScheduleBottomSheet(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {
    BasicBottomSheet(
        isBottomSheetShow = uiState.isShowTeacherScheduleSheet,
        title = R.string.teacher_schedule,
        onDismissRequest = {
            viewModel.setIsShowTeacherScheduleSheet(false)
        }
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Constants.DEFAULT_PADDING)
            ) {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                NoBorderTextField(
                    text = uiState.searchTeacherName,
                    placeholderText = stringResource(R.string.input_teacher_name),
                    onValueChange = { viewModel.setSearchTeacherName(it) },
                    focusRequester = focusSearchBarRequester
                )
            }
            HorizontalDivider()
            LoadOnlineDataLayout(
                dataSource = uiState.teacherSchedule,
                autoLoadingArgs = arrayOf(uiState.searchTeacherName),
                loadData = {
                    viewModel.loadTeacherSchedule()
                },
                contentWhenDataSourceIsEmpty = {
                    Text(
                        text = stringResource(R.string.no_teacher_schedule_found),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 5.dp).padding(horizontal = DEFAULT_PADDING),
                    )
                },
                contentWhenDataSourceIsNotEmpty = {
                    LazyColumn {
                        items(it) {
                            ListItem(
                                headlineContent = { Text(parseHtml(it.courseNameHtml.toString())) },
                                supportingContent = { Text(it.sksjdd.toString()) },
                                trailingContent = { Text(parseHtml(it.teacherNameHtml.toString())) }
                            )
                        }
                    }
                }
            )
        }
    }
}