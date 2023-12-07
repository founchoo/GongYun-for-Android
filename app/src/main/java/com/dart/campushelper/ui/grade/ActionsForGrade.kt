package com.dart.campushelper.ui.grade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Score
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.ColumnCard
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
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

    BasicBottomSheet(
        isBottomSheetShow = uiState.openSummarySheet,
        title = R.string.data_summary,
        onDismissRequest = { viewModel.setOpenSummarySheet(false) },
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            item {
                ColumnCard(
                    icon = Icons.Outlined.Score,
                    title = stringResource(R.string.course_grade),
                    description = stringResource(R.string.course_grades_desc)
                ) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "${stringResource(R.string.gpa_label)} ${
                            DecimalFormat("#.####").format(
                                uiState.gradePointAverage
                            ).toString()
                                .replaceWithStars(uiState.isScreenshotMode)
                        }",
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                    Text(
                        text = "${stringResource(R.string.arithmetic_mean_score)} ${
                            DecimalFormat("#.####").format(
                                uiState.averageScore
                            ).toString()
                                .replaceWithStars(uiState.isScreenshotMode)
                        }",
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
            }
            item {
                if (uiState.rankingAvailable) {
                    ColumnCard(
                        icon = Icons.Outlined.Groups,
                        title = stringResource(R.string.rank_title),
                        description = stringResource(R.string.rank_desc)
                    ) {
                        RankingInfo(uiState)
                        RankingColumnChart(uiState)
                    }
                } else {
                    ColumnCard(
                        useErrorColor = true,
                        icon = Icons.Outlined.Groups,
                        title = stringResource(R.string.rank_not_available_title),
                    ) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.rank_not_available_desc),
                            textAlign = TextAlign.Center,
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            item {
                ColumnCard(
                    icon = Icons.Outlined.Leaderboard,
                    title = stringResource(R.string.course_score_summary),
                    description = stringResource(R.string.course_score_desc)
                ) {
                    GradeDistributionColumnChart(uiState, viewModel)
                }
            }
            item {
                ColumnCard(
                    icon = Icons.Outlined.Timeline,
                    title = stringResource(R.string.performance_curve),
                ) {
                    GPAChangeLineChart(uiState = uiState)
                }
            }
        }
    }
}
