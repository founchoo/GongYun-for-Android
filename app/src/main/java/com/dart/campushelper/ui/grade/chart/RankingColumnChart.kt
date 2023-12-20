package com.dart.campushelper.ui.grade.chart

import androidx.compose.runtime.Composable
import com.dart.campushelper.App
import com.dart.campushelper.R
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.ui.component.rememberMarker
import com.dart.campushelper.utils.replaceWithStars
import com.dart.campushelper.viewmodel.GradeUiState
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import java.text.DecimalFormat

@Composable
fun RankingColumnChart(uiState: GradeUiState) {
    ProvideChartStyle(m3ChartStyle()) {
        if (uiState.entryModelForRankingColumnChart != null) {
            Chart(
                chart = columnChart(),
                model = uiState.entryModelForRankingColumnChart,
                bottomAxis = rememberBottomAxis(
                    guideline = null,
                    valueFormatter = { value, _ ->
                        SubRankingType.values()[value.toInt()].toString()
                    }
                ),
                startAxis = rememberStartAxis(
                    guideline = null,
                    valueFormatter = { value, _ ->
                        "${
                            DecimalFormat("#").format((1 - value) * 100)
                                .toString()
                                .replaceWithStars(uiState.isScreenshotMode)
                        }%"
                    },
                    horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                ),
                marker = rememberMarker(
                    formatter = { raw ->
                        "${App.context.getString(R.string.rank_at)} ${
                            DecimalFormat("#.##").format((1 - raw) * 100)
                                .toString()
                                .replaceWithStars(uiState.isScreenshotMode)
                        }%"
                    }
                ),
            )
        }
    }
}