package com.dart.campushelper.ui.grade

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dart.campushelper.CampusHelperApplication.Companion.context
import com.dart.campushelper.R
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.ui.component.BasicBottomSheet
import com.dart.campushelper.ui.component.isScrollingUp
import com.dart.campushelper.ui.component.rememberMarker
import com.dart.campushelper.ui.rememberCheck
import com.dart.campushelper.ui.rememberCheckIndeterminateSmall
import com.dart.campushelper.ui.rememberDeselect
import com.dart.campushelper.ui.rememberFilterAlt
import com.dart.campushelper.ui.rememberGlyphs
import com.dart.campushelper.ui.rememberGroups
import com.dart.campushelper.ui.rememberLeaderboard
import com.dart.campushelper.ui.rememberSelectAll
import com.dart.campushelper.ui.rememberShowChart
import com.dart.campushelper.ui.rememberSort
import com.dart.campushelper.ui.rememberTimeline
import com.dart.campushelper.utils.combinationOfGradeAndSemesterToText
import com.dart.campushelper.utils.replaceWithStars
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.placeholder
import io.github.fornewid.placeholder.material3.shimmer
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
lateinit var bottomSheetState: SheetState

var openFilterSheet by mutableStateOf(false)
var openSummarySheet by mutableStateOf(false)

var fabVisibility: Boolean? by mutableStateOf(null)

@SuppressLint("RestrictedApi", "CoroutineCreationDuringComposition", "UnrememberedMutableState")
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun GradeScreen(
    viewModel: GradeViewModel
) {

    val uiState by viewModel.uiState.collectAsState()

    AddContent(uiState, viewModel)

    if (uiState.isGradeDetailDialogOpen) {
        GradeDetailDialog(
            uiState = uiState,
            grade = uiState.contentForGradeDetailDialog,
            onDismissRequest = {
                viewModel.setIsGradeDetailDialogOpen(false)
            }
        )
    }

    BasicBottomSheet(
        isBottomSheetShow = openFilterSheet,
        title = R.string.filter_grade_title,
        onDismissRequest = { openFilterSheet = false },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
    ) {
        Column {
            // Semester filter section
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = rememberTimeline(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.semester_title))
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            val value = uiState.semestersSelected.containsValue(false)
                            uiState.semesters.forEach { semester ->
                                viewModel.changeSemesterSelected(
                                    semester, value
                                )
                            }
                            viewModel.filterGrades(uiState.searchKeyword)
                        },
                    ) {
                        Icon(
                            imageVector = if (uiState.semestersSelected.containsValue(false)) rememberSelectAll() else rememberDeselect(),
                            if (uiState.semestersSelected.containsValue(false))
                                stringResource(R.string.select_all) else stringResource(R.string.deselect_all),
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(-5.dp)
                ) {
                    for (semester in uiState.semesters) {
                        FilterChip(
                            selected = uiState.semestersSelected[semester] ?: false,
                            onClick = {
                                viewModel.changeSemesterSelected(
                                    semester,
                                    !(uiState.semestersSelected[semester] ?: false)
                                )
                                viewModel.filterGrades(uiState.searchKeyword)
                            },
                            label = { Text(semester) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (uiState.semestersSelected[semester] == true) rememberCheck() else rememberCheckIndeterminateSmall(),
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            // Course type section
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = rememberSort(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.course_type_title))
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            val value = uiState.courseTypesSelected.containsValue(false)
                            uiState.courseTypes.forEach { semester ->
                                viewModel.changeCourseSortSelected(
                                    semester, value
                                )
                            }
                            viewModel.filterGrades(uiState.searchKeyword)
                        },
                    ) {
                        Icon(
                            imageVector = if (uiState.courseTypesSelected.containsValue(false))
                                rememberSelectAll() else rememberDeselect(),
                            contentDescription = if (uiState.courseTypesSelected.containsValue(
                                    false
                                )
                            )
                                stringResource(R.string.select_all) else stringResource(R.string.deselect_all),
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(-5.dp)
                ) {
                    for (courseType in uiState.courseTypes) {
                        FilterChip(
                            selected = uiState.courseTypesSelected[courseType] ?: false,
                            onClick = {
                                viewModel.changeCourseSortSelected(
                                    courseType,
                                    !(uiState.courseTypesSelected[courseType] ?: false)
                                )
                                viewModel.filterGrades(uiState.searchKeyword)
                            },
                            label = { Text(courseType) },
                            leadingIcon = if (uiState.courseTypesSelected[courseType] == true) {
                                {
                                    Icon(
                                        imageVector = rememberCheck(),
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else {
                                {
                                    Icon(
                                        imageVector = rememberCheckIndeterminateSmall(),
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GradeDetailDialog(
    uiState: GradeUiState,
    grade: Grade,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        title = {
            Text(text = grade.name)
        },
        text = {
            Column {
                ListItem(
                    headlineContent = {
                        Text(
                            "${stringResource(R.string.grade_label)} ${
                                grade.score.toString().replaceWithStars(uiState.isScreenshotMode)
                            } / ${stringResource(R.string.gpa_label)} ${
                                grade.gradePoint.toString()
                                    .replaceWithStars(uiState.isScreenshotMode)
                            }"
                        )
                    },
                    supportingContent = {
                        Text(
                            (grade.detail ?: "").replaceWithStars(uiState.isScreenshotMode)
                        )
                    },
                    trailingContent = {
                        Text(
                            "${grade.yearAndSemester}\n" +
                                    "${grade.courseType}\n" +
                                    "${stringResource(R.string.credit_label)} ${grade.credit}"
                        )
                    },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFloatingActionButtonForGrade() {

    val density = LocalDensity.current
    AnimatedVisibility(
        visible = fabVisibility ?: false,
        enter = slideInHorizontally {
            with(density) { 80.dp.roundToPx() }
        },
        exit = slideOutHorizontally {
            with(density) { 80.dp.roundToPx() }
        },
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(stringResource(R.string.filter_grade_title))
                }
            },
            state = rememberTooltipState()
        ) {
            FloatingActionButton(
                onClick = {
                    openFilterSheet = true
                }
            ) {
                Icon(
                    imageVector = rememberFilterAlt(),
                    contentDescription = null,
                )
            }
        }
    }
}

@OptIn(
    ExperimentalMaterialApi::class
)
@SuppressLint("UnrememberedMutableState")
@Composable
fun AddContent(uiState: GradeUiState, viewModel: GradeViewModel) {

    val listState = rememberLazyListState()
    val isScrollingUp by mutableStateOf(listState.isScrollingUp())
    val scope = rememberCoroutineScope()
    fun refresh() = scope.launch {
        viewModel.getGrades()
    }

    val pullRefreshState = rememberPullRefreshState(uiState.isGradesLoading, ::refresh)

    fabVisibility = isScrollingUp

    Box(
        modifier = Modifier
            .pullRefresh(pullRefreshState)
            .semantics { isTraversalGroup = true },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .placeholder(
                    visible = uiState.isGradesLoading,
                    highlight = PlaceholderHighlight.shimmer()
                ),
            verticalArrangement = Arrangement.SpaceAround,
            state = listState,
        ) {
            if (uiState.grades.isEmpty()) {
                item {
                    Text(
                        text = if (uiState.searchKeyword.isEmpty()) stringResource(R.string.no_grades) else stringResource(
                            R.string.no_query_grades
                        ),
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                items(uiState.grades) { grade ->
                    GradeItem(grade, uiState, viewModel)
                }
            }
        }
        PullRefreshIndicator(
            uiState.isGradesLoading,
            pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }
}

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
                    combinationOfGradeAndSemesterToText(value)
                        .replaceWithStars(uiState.isScreenshotMode)
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
                    } ${context.getString(R.string.courses_belong_to_interval)}"
                }
            ),
        )
    }
}

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
                        "${context.getString(R.string.rank_at)} ${
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

@Composable
fun RankingInfo(uiState: GradeUiState) {
    return HostRankingType.values()
        .forEach { hostRankingType ->
            ListItem(
                leadingContent = {
                    Box(
                        Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(
                                color = when (hostRankingType) {
                                    HostRankingType.GPA -> MaterialTheme.colorScheme.primary
                                    HostRankingType.SCORE -> MaterialTheme.colorScheme.secondary
                                },
                            )
                    )
                },
                supportingContent = {
                    Text(hostRankingType.run {
                        SubRankingType.values()
                            .joinToString("，") {
                                uiState.rankingInfo.getRanking(
                                    this,
                                    it
                                ).run {
                                    "$it ${this?.ranking ?: "-"}/${this?.total ?: "-"}"
                                }
                            }
                            .replaceWithStars(uiState.isScreenshotMode)
                    })
                },
                headlineContent = { Text(hostRankingType.toString()) },
            )
        }
}

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
                text = "${grade.yearAndSemester}"
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    )
}

@Composable
fun ElevatedCardWithIcon(
    useErrorColor: Boolean = false,
    isLoadingPlaceholderShow: Boolean = false,
    icon: ImageVector,
    title: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    return ElevatedCard(
        colors = if (useErrorColor) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ) else CardDefaults.elevatedCardColors(),
        modifier = Modifier
            .placeholder(
                visible = isLoadingPlaceholderShow,
                highlight = PlaceholderHighlight.shimmer(),
            )
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (useErrorColor) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.W900,
                color = if (useErrorColor) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionsForGrade(
    viewModel: GradeViewModel,
    uiState: GradeUiState,
) {

    val scope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = !uiState.isSearchBarShow,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(stringResource(R.string.search))
                }
            },
            state = rememberTooltipState()
        ) {
            IconButton(
                onClick = {
                    viewModel.setIsSearchBarShow(true)
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.search),
                )
            }
        }
    }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(stringResource(R.string.data_summary))
            }
        },
        state = rememberTooltipState()
    ) {
        IconButton(
            onClick = {
                scope.launch {
                    viewModel.loadLineChart()
                }
                openSummarySheet = true
            },
        ) {
            Icon(
                imageVector = rememberShowChart(),
                contentDescription = stringResource(R.string.data_summary),
            )
        }
    }

    BasicBottomSheet(
        isBottomSheetShow = openSummarySheet,
        title = R.string.data_summary,
        onDismissRequest = { openSummarySheet = false },
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            item {
                ElevatedCardWithIcon(
                    isLoadingPlaceholderShow = uiState.isGradesLoading,
                    icon = rememberGlyphs(),
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
                    ElevatedCardWithIcon(
                        isLoadingPlaceholderShow = uiState.isRankingInfoLoading,
                        icon = rememberGroups(),
                        title = stringResource(R.string.rank_title),
                        description = stringResource(R.string.rank_desc)
                    ) {
                        RankingInfo(uiState)
                        RankingColumnChart(uiState)
                    }
                } else {
                    ElevatedCardWithIcon(
                        useErrorColor = true,
                        isLoadingPlaceholderShow = uiState.isGradesLoading,
                        icon = rememberGroups(),
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
                ElevatedCardWithIcon(
                    icon = rememberLeaderboard(),
                    title = stringResource(R.string.course_score_summary),
                    description = stringResource(R.string.course_score_desc)
                ) {
                    GradeDistributionColumnChart(uiState, viewModel)
                }
            }
            item {
                ElevatedCardWithIcon(
                    isLoadingPlaceholderShow = uiState.isLineChartLoading,
                    icon = rememberTimeline(),
                    title = stringResource(R.string.performance_curve),
                ) {
                    GPAChangeLineChart(uiState = uiState)
                }
            }
        }
    }
}