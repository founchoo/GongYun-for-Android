package com.dart.campushelper.ui.grade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.model.Grade
import com.dart.campushelper.utils.AcademicYearAndSemester
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel

@Composable
fun GradeItem(grade: Grade, uiState: GradeUiState, viewModel: GradeViewModel) {
    return ListItem(
        modifier = Modifier
            .clickable {
                viewModel.setIsGradeDetailDialogOpen(true)
                viewModel.setContentForGradeDetailDialog(grade)
            }
            .padding(horizontal = 10.dp),
        headlineContent = {
            Text(
                text = grade.name,
            )
        },
        supportingContent = {
            Text(
                text = "${stringResource(R.string.grade_label)} ${
                    grade.score.toString()
                        .replaceWithStars(uiState.isScreenshotMode)
                }"
            )
        },
        trailingContent = {
            Text(
                text = "${
                    grade.yearAndSemester?.let {
                        uiState.startYearAndSemester?.let { start ->
                            AcademicYearAndSemester.getReadableString(
                                start,
                                it
                            )
                        }
                    }
                }"
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    )
}
