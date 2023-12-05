package com.dart.campushelper.ui.grade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dart.campushelper.CampusHelperApplication
import com.dart.campushelper.R
import com.dart.campushelper.ui.component.rememberMarker
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.viewmodel.GradeUiState
import com.dart.campushelper.viewmodel.GradeViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.DecimalFormat

@Composable
fun GradeDistributionColumnChart(uiState: GradeUiState, viewModel: GradeViewModel) {
    val entryModelForGradeDistributionChart = remember(uiState.gradeDistribution) {
        uiState.gradeDistribution.mapIndexed { index, value ->
            entryOf(index, value)
        }
    }

    return ProvideChartStyle(m3ChartStyle()) {
        Chart(
            chart = columnChart(),
            model = entryModelOf(
                entryModelForGradeDistributionChart
            ),
            bottomAxis = rememberBottomAxis(
                guideline = null,
                valueFormatter = { value, _ ->
                    viewModel.parseScoreRangeIndex(value.toInt())
                }
            ),
            startAxis = rememberStartAxis(
                guideline = null,
                valueFormatter = { value, _ ->
                    DecimalFormat("#").format(value).toString()
                        .replaceWithStars(uiState.isScreenshotMode)
                },
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            ),
            marker = rememberMarker(
                formatter = { raw ->
                    "${
                        DecimalFormat("#").format(raw).toString()
                            .replaceWithStars(uiState.isScreenshotMode)
                    } ${CampusHelperApplication.context.getString(R.string.courses_belong_to_interval)}"
                }
            ),
        )
    }
}