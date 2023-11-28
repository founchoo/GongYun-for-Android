package com.dart.campushelper.ui.grade

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.dart.campushelper.model.Grade
import com.dart.campushelper.model.HostRankingType
import com.dart.campushelper.model.SubRankingType
import com.dart.campushelper.ui.rememberCheck
import com.dart.campushelper.ui.rememberCheckIndeterminateSmall
import com.dart.campushelper.ui.rememberFilterAlt
import com.dart.campushelper.ui.rememberGlyphs
import com.dart.campushelper.ui.rememberGroups
import com.dart.campushelper.ui.rememberLeaderboard
import com.dart.campushelper.ui.rememberShowChart
import com.dart.campushelper.ui.rememberSort
import com.dart.campushelper.ui.rememberTimeline
import com.dart.campushelper.utils.Constants.Companion.DEFAULT_PADDING
import com.dart.campushelper.utils.isScrollingUp
import com.dart.campushelper.utils.rememberMarker
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
var openChartSheet by mutableStateOf(false)

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

    val scope = rememberCoroutineScope()

    bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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

    // Sheet content
    if (openFilterSheet) {
        ModalBottomSheet(
            windowInsets = WindowInsets.navigationBars,
            onDismissRequest = {
                openFilterSheet = false
            },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DEFAULT_PADDING, 0.dp, DEFAULT_PADDING, DEFAULT_PADDING),
            ) {
                Text("筛选成绩", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(15.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.searchKeyword,
                    onValueChange = { viewModel.changeSearchKeyword(it) },
                    label = { Text("课程名称") },
                    singleLine = true,
                    placeholder = { Text("留空则不对课程名称进行筛选") },
                )
                Spacer(Modifier.height(20.dp))
                // Semester filter section
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            imageVector = rememberTimeline(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("学年学期")
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(-5.dp)
                    ) {
                        for (i in uiState.semesters) {
                            FilterChip(
                                selected = uiState.semestersSelected[i] ?: false,
                                onClick = {
                                    viewModel.changeSemesterSelected(
                                        i,
                                        !(uiState.semestersSelected[i] ?: false)
                                    )
                                },
                                label = { Text(i) },
                                leadingIcon = if (uiState.semestersSelected[i] == true) {
                                    {
                                        Icon(
                                            imageVector = rememberCheck(),
                                            contentDescription = "Localized Description",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    {
                                        Icon(
                                            imageVector = rememberCheckIndeterminateSmall(),
                                            contentDescription = "Localized Description",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                // Course sort section
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            imageVector = rememberSort(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("课程性质")
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(-5.dp)
                    ) {
                        for (i in uiState.courseSorts) {
                            FilterChip(
                                selected = uiState.courseSortsSelected[i] ?: false,
                                onClick = {
                                    viewModel.changeCourseSortSelected(
                                        i,
                                        !(uiState.courseSortsSelected[i] ?: false)
                                    )
                                },
                                label = { Text(i) },
                                leadingIcon = if (uiState.courseSortsSelected[i] == true) {
                                    {
                                        Icon(
                                            imageVector = rememberCheck(),
                                            contentDescription = "Localized Description",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    {
                                        Icon(
                                            imageVector = rememberCheckIndeterminateSmall(),
                                            contentDescription = "Localized Description",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(30.dp))
                // Button section
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(onClick = {
                        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                openFilterSheet = false
                            }
                        }
                    }) {
                        Text("取消")
                    }
                    // Apply button
                    Button(
                        // Note: If you provide logic outside of onDismissRequest to remove the sheet,
                        // you must additionally handle intended state cleanup, if any.
                        onClick = {
                            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                                if (!bottomSheetState.isVisible) {
                                    openFilterSheet = false
                                }
                            }
                            viewModel.filterGrades(uiState.searchKeyword)
                        },
                    ) {
                        Text("应用筛选")
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
                            "成绩 ${
                                grade.score.toString().replaceWithStars(uiState.isScreenshotMode)
                            } / 绩点 ${
                                grade.gradePoint.toString()
                                    .replaceWithStars(uiState.isScreenshotMode)
                            }"
                        )
                    },
                    supportingContent = {
                        Text(
                            (grade.detail ?: "").replaceWithStars(uiState.isScreenshotMode) ?: ""
                        )
                    },
                    trailingContent = {
                        Text(
                            "${grade.semesterYearAndNo}\n" +
                                    "${grade.courseType}\n" +
                                    "学分 ${grade.credit}"
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
                Text("关闭")
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
                    Text("筛选成绩")
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun AddContent(uiState: GradeUiState, viewModel: GradeViewModel) {

    val listState = rememberLazyListState()
    var isScrollingUp by mutableStateOf(listState.isScrollingUp())
    val scope = rememberCoroutineScope()
    fun refresh() = scope.launch {
        viewModel.getGrades()
        viewModel.getStudentRankingInfo()
    }

    val pullRefreshState = rememberPullRefreshState(uiState.isGradesLoading, ::refresh)
    val tabs = listOf("课程列表", "数据统计")
    val pagerState = rememberPagerState(0) { tabs.size }

    fabVisibility = isScrollingUp && pagerState.currentPage == 0

    Column {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(page = index)
                        }
                    },
                    text = { Text(tab) }
                )
            }
        }
        HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) {
            when (it) {
                0 -> {
                    // Grades info section.
                    Box(
                        modifier = Modifier
                            .pullRefresh(pullRefreshState),
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
                            itemsIndexed(uiState.grades) { _, grade ->
                                ListItem(
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
                                            text = "成绩 ${
                                                grade.score.toString()
                                                    .replaceWithStars(uiState.isScreenshotMode)
                                            }"
                                        )
                                    },
                                    trailingContent = {
                                        Text(
                                            text = "${grade.semesterYearAndNo}"
                                        )
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                    )
                                )
                            }
                        }
                        PullRefreshIndicator(
                            uiState.isGradesLoading,
                            pullRefreshState,
                            Modifier.align(Alignment.TopCenter)
                        )
                    }
                }

                1 -> {
                    // Grade gpa and ranking info section.
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                    ) {
                        item {}
                        item {
                            ElevatedCard(
                                modifier = Modifier
                                    .placeholder(
                                        visible = uiState.isGradesLoading,
                                        highlight = PlaceholderHighlight.shimmer(),
                                    )
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = rememberGlyphs(),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "已筛选课程成绩",
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.W900
                                    )
                                    Column {
                                        Text(
                                            text = "平均学分绩点 ${
                                                DecimalFormat("#.####").format(
                                                    uiState.gradePointAverage
                                                ).toString()
                                                    .replaceWithStars(uiState.isScreenshotMode)
                                            }",
                                            textAlign = TextAlign.Center,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                        )
                                        Text(
                                            text = "算数平均分 ${
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
                            }
                        }
                        item {
                            if (uiState.rankingAvailable) {
                                ElevatedCard(
                                    modifier = Modifier
                                        .placeholder(
                                            visible = uiState.isRankingInfoLoading,
                                            highlight = PlaceholderHighlight.shimmer()
                                        )
                                        .fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        Icon(
                                            imageVector = rememberGroups(),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = "排名信息",
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.W900
                                        )
                                        ProvideChartStyle(m3ChartStyle()) {
                                            if (uiState.rankingData != null) {
                                                Chart(
                                                    chart = columnChart(),
                                                    model = uiState.rankingData,
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
                                                            "位列前 ${
                                                                DecimalFormat("#.##").format((1 - raw) * 100)
                                                                    .toString()
                                                                    .replaceWithStars(uiState.isScreenshotMode)
                                                            }%"
                                                        }
                                                    ),
                                                )
                                            }
                                        }
                                        HostRankingType.values()
                                            .forEach { hostRankingType ->
                                                ListItem(
                                                    leadingContent = {
                                                        Box(
                                                            Modifier
                                                                .size(10.dp)
                                                                .background(
                                                                    color = when (hostRankingType) {
                                                                        HostRankingType.GPA -> MaterialTheme.colorScheme.primary
                                                                        HostRankingType.SCORE -> MaterialTheme.colorScheme.secondary
                                                                    },
                                                                    shape = MaterialTheme.shapes.small
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
                                }
                            } else {
                                ElevatedCard(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .placeholder(
                                            visible = uiState.isGradesLoading,
                                            highlight = PlaceholderHighlight.shimmer()
                                        ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = rememberGroups(),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "排名信息不可用",
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.W900,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            maxLines = 2
                                        )
                                        Text(
                                            text = "当筛选课程性质或课程名称时排名信息不可用",
                                            textAlign = TextAlign.Center,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.Start,
                                ) {
                                    Icon(
                                        imageVector = rememberLeaderboard(),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Text(
                                        text = "已筛选课程分数统计",
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.W900
                                    )
                                    ProvideChartStyle(m3ChartStyle()) {
                                        Chart(
                                            chart = columnChart(),
                                            model = entryModelOf(
                                                uiState.gradeDistribution.mapIndexed { index, value ->
                                                    entryOf(index, value)
                                                }
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
                                                    } 门课位于此区间"
                                                }
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                        item {}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateActionsForGrade(viewModel: GradeViewModel, uiState: GradeUiState) {

    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text("绩点曲线")
            }
        },
        state = rememberTooltipState()
    ) {
        IconButton(
            onClick = {
                scope.launch {
                    viewModel.loadLineChart()
                }
                openChartSheet = true
            },
        ) {
            Icon(
                imageVector = rememberShowChart(),
                contentDescription = "绩点曲线",
            )
        }
    }

    if (openChartSheet) {
        ModalBottomSheet(
            windowInsets = WindowInsets.navigationBars,
            onDismissRequest = {
                openChartSheet = false
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DEFAULT_PADDING),
                ) {
                    Text(
                        "绩点曲线", style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = DEFAULT_PADDING)
                    )
                    if (uiState.isLineChartLoading) {
                        Spacer(Modifier.height(5.dp))
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    ProvideChartStyle(m3ChartStyle()) {
                        Chart(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .requiredWidth(uiState.overallScoreData.size * 60.dp)
                                .align(Alignment.CenterHorizontally),
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
                                    when (value) {
                                        1f -> "大一上"
                                        2f -> "大一下"
                                        3f -> "大二上"
                                        4f -> "大二下"
                                        5f -> "大三上"
                                        6f -> "大三下"
                                        7f -> "大四上"
                                        8f -> "大四下"
                                        else -> ""
                                    }
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
            },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
        )
    }
}