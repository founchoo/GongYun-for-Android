package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dart.campushelper.ui.rememberTune
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    AddContent(uiState = uiState, viewModel = viewModel)

    if (uiState.isCourseDetailDialogOpen) {
        AlertDialog(
            confirmButton = {},
            dismissButton = {
                TextButton({
                    viewModel.hideCourseDetailDialog()
                }) {
                    Text("关闭")
                }
            },
            onDismissRequest = { viewModel.hideCourseDetailDialog() },
            title = { Text("课程详情") },
            text = {
                LazyColumn {
                    items(uiState.contentInCourseDetailDialog) { course ->
                        Surface {
                            ListItem(
                                headlineContent = { Text("${course.courseName}") },
                                supportingContent = { Text("${course.classroomName} / 第 ${course.zc} 周") },
                                trailingContent = { Text("${course.teacherName}") }
                            )
                        }
                    }
                }
            }
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun AddContent(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {

    val refreshScope = rememberCoroutineScope()

    fun refresh() = refreshScope.launch {
        viewModel.getCourses()
    }

    val refreshState = rememberPullRefreshState(uiState.isTimetableLoading, ::refresh)
    val nodeColumnWeight = 0.65F

    Column(Modifier.padding(5.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                modifier = Modifier
                    .weight(nodeColumnWeight)
            ) {
                if (uiState.isYearDisplay && uiState.browsedSemester == uiState.semesters.last()) {
                    Text(
                        text = "${uiState.browsedYear}年",
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .weight(7F)
                    .align(Alignment.CenterVertically)
            ) {
                uiState.weekHeaders.forEachIndexed { index, week ->
                    Box(
                        modifier = Modifier
                            .weight(1F)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            val color =
                                if (uiState.dayOfWeek - 1 == index && uiState.browsedWeek == uiState.currentWeek) MaterialTheme.colorScheme.primary else Color.Unspecified
                            Text(
                                text = week,
                                color = color,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (uiState.isDateDisplay && uiState.browsedSemester == uiState.semesters.last()) {
                                Text(
                                    text = uiState.dateHeaders?.get(index) ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color,
                                )
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.pullRefresh(refreshState)) {
            LazyColumn {
                item {
                    Row {
                        // 节点数
                        Column(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .weight(nodeColumnWeight),
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            uiState.nodeHeaders.forEachIndexed { index, node ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    val color =
                                        if (uiState.currentNode - 1 == index) MaterialTheme.colorScheme.primary else Color.Unspecified
                                    Text(
                                        color = color,
                                        text = node.toString(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (uiState.isTimeDisplay) {
                                        Text(
                                            color = color,
                                            text = "${uiState.nodeStartHeaders[index]}\n${uiState.nodeEndHeaders[index]}",
                                            style = MaterialTheme.typography.labelSmall,
                                            lineHeight = 12.sp,
                                        )
                                    }
                                }
                            }
                        }
                        // 课程详情
                        Row(
                            modifier = Modifier
                                .weight(7F)
                        ) {
                            (1..7).forEach { week ->
                                Column(
                                    modifier = Modifier
                                        .fillParentMaxHeight()
                                        .weight(1F)
                                ) {
                                    for (node in 1..5) {
                                        // 筛选出当前遍历的星期号和节次的课程
                                        val coursesOnCell = uiState.courses[Pair(week, node)]
                                        val alpha =
                                            if (coursesOnCell?.find { it.weekNoList.contains(uiState.browsedWeek) } != null)
                                                1f
                                            else if (coursesOnCell.isNullOrEmpty())
                                                0f
                                            else
                                                0.3f
                                        val foreground =
                                            MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
                                        val background =
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = alpha)
                                        Box(
                                            modifier = Modifier
                                                .weight(1F)
                                                .fillMaxWidth()
                                                .padding(3.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(background)
                                                .clickable(
                                                    enabled = coursesOnCell?.isNotEmpty() ?: false,
                                                    onClick = {
                                                        viewModel.setContentInCourseDetailDialog(
                                                            coursesOnCell?.toList() ?: emptyList()
                                                        )
                                                        viewModel.showCourseDetailDialog()
                                                    }
                                                )
                                        ) {
                                            if (coursesOnCell?.isNotEmpty() == true) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(15.dp)
                                                        .clip(CutCornerShape(topStart = 15.dp))
                                                        .background(
                                                            if (coursesOnCell.count() > 1)
                                                                MaterialTheme.colorScheme.primary.copy(
                                                                    alpha = alpha
                                                                )
                                                            else
                                                                Color.Transparent
                                                        )
                                                        .align(Alignment.BottomEnd)
                                                )
                                                Column(
                                                    modifier = Modifier
                                                        .padding(2.dp)
                                                        .align(Alignment.Center),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = coursesOnCell.first().courseName
                                                            ?: "",
                                                        style = MaterialTheme.typography.bodySmall.merge(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        textAlign = TextAlign.Center,
                                                        color = foreground,
                                                    )
                                                    Text(
                                                        text = coursesOnCell.first().classroomName
                                                            ?: "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        textAlign = TextAlign.Center,
                                                        color = foreground,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            PullRefreshIndicator(
                uiState.isTimetableLoading,
                refreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun CreateActionsForSchedule(viewModel: ScheduleViewModel, uiState: ScheduleUiState) {

    var showWeekSliderDialog by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showWeekSliderDialog = true
    }) {
        Icon(
            imageVector = rememberTune(),
            contentDescription = null
        )
    }

    var weekSliderPosition by remember { mutableFloatStateOf(viewModel.uiState.value.currentWeek.toFloat()) }
    var semesterDropdownMenuExpanded by remember { mutableStateOf(false) }
    var selectedSemester by remember { mutableStateOf(uiState.browsedSemester) }
    val hapticFeedback = LocalHapticFeedback.current

    if (showWeekSliderDialog) {
        AlertDialog(
            confirmButton = {
                TextButton({
                    viewModel.setBrowsedWeek(weekSliderPosition.toInt())
                    viewModel.setBrowsedSemester(selectedSemester)
                    showWeekSliderDialog = false
                }) {
                    Text("应用")
                }
            },
            dismissButton = {
                TextButton({
                    weekSliderPosition = uiState.currentWeek.toFloat()
                    viewModel.resetBrowsedWeek()
                    selectedSemester = uiState.semesters.last()
                    viewModel.resetBrowsedSemester()
                    showWeekSliderDialog = false
                }) {
                    Text("回到当前")
                }
            },
            onDismissRequest = {
                weekSliderPosition = uiState.browsedWeek.toFloat()
                selectedSemester = uiState.browsedSemester
                showWeekSliderDialog = false
            },
            title = { Text("切换课表") },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = "切换周数")
                        Text(
                            text = "切换到第 ${weekSliderPosition.toInt()} 周",
                            modifier = Modifier.weight(1F, true),
                            textAlign = TextAlign.Right,
                        )
                    }
                    Slider(
                        value = weekSliderPosition,
                        onValueChange = {
                            if (abs(it - weekSliderPosition) >= 0.9f) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            weekSliderPosition = it
                        },
                        steps = 20,
                        valueRange = 1f..20f,
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = "切换学年学期")
                    Spacer(modifier = Modifier.height(10.dp))
                    ExposedDropdownMenuBox(
                        expanded = semesterDropdownMenuExpanded,
                        onExpandedChange = {
                            semesterDropdownMenuExpanded = !semesterDropdownMenuExpanded
                        }
                    ) {
                        TextField(
                            modifier = Modifier.menuAnchor(),
                            readOnly = true,
                            value = selectedSemester,
                            onValueChange = {},
                            label = { Text("当前选择的学年学期") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterDropdownMenuExpanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        )
                        ExposedDropdownMenu(
                            expanded = semesterDropdownMenuExpanded,
                            onDismissRequest = { semesterDropdownMenuExpanded = false }
                        ) {
                            uiState.semesters.forEach { semester ->
                                DropdownMenuItem(
                                    text = {
                                        Text(semester)
                                    },
                                    onClick = {
                                        selectedSemester = semester
                                        semesterDropdownMenuExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("注意：当选择非当前学期时，星期下方的日期及左上角的年份将不支持显示（即使在设置中已开启）")
                }
            }
        )
    }
}
