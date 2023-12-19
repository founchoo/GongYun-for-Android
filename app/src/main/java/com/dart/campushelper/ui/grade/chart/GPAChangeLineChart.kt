package com.dart.campushelper.ui.grade.chart

import androidx.compose.runtime.Composable
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.ui.component.rememberMarker
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.viewmodel.GradeUiState
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.DecimalFormat

@Composable
fun GPAChangeLineChart(uiState: GradeUiState) {
    ProvideChartStyle(m3ChartStyle()) {
        Chart(
            chart = lineChart(),
            model = entryModelOf(
                uiState.overallScoreData
            ),
            startAxis = rememberStartAxis(
                valueFormatter = { value, _ ->
                    DecimalFormat("#.##").format(value).toString()
                        .replaceWithStars(uiState.isScreenshotMode)
                },
            ),
            bottomAxis = rememberBottomAxis(
                guideline = null,
                valueFormatter = { value, _ ->
                    uiState.semesters?.get(value.toInt() - 1)
                        ?.let { "${context.getString(it.yearResId)} ${context.getString(it.semesterResId)}" }
                        ?.replaceWithStars(uiState.isScreenshotMode) ?: ""
                },
            ),
            marker = rememberMarker(
                formatter = { raw ->
                    DecimalFormat("#.####").format(raw).toString()
                        .replaceWithStars(uiState.isScreenshotMode)
                }
            ),
        )
    }
}