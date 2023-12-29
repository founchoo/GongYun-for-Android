package com.dart.campushelper.ui.grade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.ui.component.isScrollingUp
import com.dart.campushelper.ui.grade.bottomsheet.FilterGradeBottomSheet
import com.dart.campushelper.ui.grade.bottomsheet.StatisticBottomSheet
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel

@Composable
fun GradeScreen(
    uiState: GradeUiState,
    viewModel: GradeViewModel
) {

    val listState = rememberLazyListState()
    val isScrollingUp = listState.isScrollingUp()

    viewModel.setFabVisibility(isScrollingUp)

    LoadOnlineDataLayout(
        dataSource = uiState.grades,
        loadData = { viewModel.getGrades() },
        contentWhenDataSourceIsEmpty = {
            Text(
                text = if (uiState.searchKeyword.isEmpty()) stringResource(R.string.no_grades) else stringResource(
                    R.string.no_query_grades
                ),
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        },
        contentWhenDataSourceIsNotEmpty = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceAround,
                state = listState,
            ) {
                items(it) { grade ->
                    GradeItem(grade, uiState, viewModel)
                }
            }
        },
    )

    GradeDetailDialog(
        uiState = uiState,
        grade = uiState.contentForGradeDetailDialog,
        onDismissRequest = {
            viewModel.setIsGradeDetailDialogOpen(false)
        }
    )

    FilterGradeBottomSheet(uiState, viewModel)

    StatisticBottomSheet(uiState, viewModel)
}
