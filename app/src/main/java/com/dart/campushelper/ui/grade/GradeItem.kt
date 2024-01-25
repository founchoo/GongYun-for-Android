package com.dart.campushelper.ui.grade

import androidx.compose.foundation.clickable
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
                viewModel.setContentForGradeDetailDialog(grade)
                viewModel.setIsGradeDetailDialogOpen(true)
                viewModel.updateGradeReadStatus(grade)
            },
        headlineContent = {
            BadgedBox(badge = { if (!grade.isRead) Badge() }) {
                Text(grade.name)
            }
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
        }
    )
}
