package com.dart.campushelper.ui.grade

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dart.campushelper.model.Grade
import com.dart.campushelper.ui.rememberFilterAlt
import com.dart.campushelper.ui.rememberGlyphs
import com.dart.campushelper.ui.rememberGroups
import com.dart.campushelper.ui.rememberSort
import com.dart.campushelper.ui.rememberTimeline
import com.dart.campushelper.utils.NoLoginPlaceholder
import com.dart.campushelper.utils.fadingEdge
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.placeholder
import io.github.fornewid.placeholder.material3.shimmer
import kotlinx.coroutines.launch
import java.text.DecimalFormat

var openBottomSheet by mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class)
lateinit var bottomSheetState: SheetState

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

    val listState = rememberLazyListState()
    fabVisibility = derivedStateOf {
        !listState.isScrollInProgress
    }.value

    if (uiState.showLoginPlaceholder) {
        NoLoginPlaceholder()
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(Modifier.width(10.dp))
            // Grade gpa and ranking info section.
            Box(
                modifier = Modifier
                    .fadingEdge(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.1f to Color.Red,
                            0.9f to Color.Red,
                            1f to Color.Transparent
                        )
                    )
            ) {
                LazyRow {
                    item {
                        Spacer(Modifier.width(25.dp))
                    }
                    item {
                        Card(
                            modifier = Modifier.placeholder(
                                visible = uiState.isGradesLoading,
                                highlight = PlaceholderHighlight.shimmer()
                            )
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
                                            )
                                        }",
                                        textAlign = TextAlign.Center,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                    )
                                    Text(
                                        text = "算数平均分 ${
                                            DecimalFormat("#.####").format(
                                                uiState.averageScore
                                            )
                                        }",
                                        textAlign = TextAlign.Center,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.width(10.dp))
                    }
                    item {
                        if (uiState.rankingAvailable) {
                            Card(
                                modifier = Modifier.placeholder(
                                    visible = uiState.isRankingInfoLoading,
                                    highlight = PlaceholderHighlight.shimmer()
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = rememberGroups(),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "排名信息",
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.W900
                                    )
                                    Column {
                                        Text(
                                            text = "平均学分绩点排名 " +
                                                    "年级 ${uiState.rankingInfo.byGPAByInstitute.run { "${this.first}/${this.second}" }} " +
                                                    "专业 ${uiState.rankingInfo.byGPAByMajor.run { "${this.first}/${this.second}" }} " +
                                                    "班级 ${uiState.rankingInfo.byGPAByClass.run { "${this.first}/${this.second}" }} ",
                                            textAlign = TextAlign.Center,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                        )
                                        Text(
                                            text = "算术平均分排名 " +
                                                    "年级 ${uiState.rankingInfo.byScoreByInstitute.run { "${this.first}/${this.second}" }} " +
                                                    "专业 ${uiState.rankingInfo.byScoreByMajor.run { "${this.first}/${this.second}" }} " +
                                                    "班级 ${uiState.rankingInfo.byScoreByClass.run { "${this.first}/${this.second}" }} ",
                                            textAlign = TextAlign.Center,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                        )
                                    }
                                }
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
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
                                    Column {
                                        Text(
                                            text = "当筛选课程性质或课程",
                                            textAlign = TextAlign.Center,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "名称时排名信息不可用",
                                            textAlign = TextAlign.Center,
                                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.width(25.dp))
                    }
                }
            }
            // Grades info section.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(25.dp, 0.dp, 25.dp, 10.dp)
                    .placeholder(
                        visible = uiState.isGradesLoading,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                verticalArrangement = Arrangement.Top,
                state = listState,
            ) {
                itemsIndexed(uiState.grades.toList()) { _, grade ->
                    Row(
                        modifier = Modifier
                            .clickable {
                                viewModel.showGradeDetailDialog()
                                viewModel.setContentForGradeDetailDialog(grade)
                            }
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(4f)) {
                            Text(
                                text = grade.name,
                                textAlign = TextAlign.Left,
                                style = MaterialTheme.typography.titleMedium,
                                softWrap = true,
                            )
                            Text(
                                text = "学分 ${grade.credit}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = grade.score.toString(),
                            textAlign = TextAlign.Right,
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .fillMaxWidth()
                                .weight(1f),
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    if (uiState.isGradeDetailDialogOpen) {
        GradeDetailDialog(
            grade = uiState.contentForGradeDetailDialog,
            onDismissRequest = {
                viewModel.hideGradeDetailDialog()
            }
        )
    }

    // Sheet content
    if (openBottomSheet) {

        ModalBottomSheet(
            onDismissRequest = {
                openBottomSheet = false
            },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp, 0.dp, 15.dp, 15.dp),
            ) {
                Text("筛选成绩", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(15.dp))
                OutlinedTextField(
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
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Localized Description",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
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
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = "Localized Description",
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                } else {
                                    null
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
                                openBottomSheet = false
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
                                    openBottomSheet = false
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
    grade: Grade,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        title = {
            Text(text = "成绩详情")
        },
        text = {
            Text(
                text = "课程名称: ${grade.name}\n" +
                        "课程代码: ${grade.kcid}\n" +
                        "课程性质: ${grade.courseSort}\n" +
                        "学分: ${grade.credit}\n" +
                        "学年学期: ${grade.xnxq}\n" +
                        "成绩: ${grade.score}\n" +
                        "绩点: ${grade.gp}"
            )
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
        FloatingActionButton(
            onClick = {
                openBottomSheet = true
            }
        ) {
            Icon(
                imageVector = rememberFilterAlt(),
                contentDescription = null,
            )
        }
    }
}
