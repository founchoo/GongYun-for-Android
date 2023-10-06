package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.dart.campushelper.model.Course
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (course in uiState.contentInCourseDetailDialog) {
                        Text(
                            "课程: ${course.courseName}\n" +
                                    "周数: ${course.zc}\n" +
                                    "教室: ${course.classroomName}\n" +
                                    "教师: ${course.teacherName}\n"
                        )
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
    val nodeColumnWeight = 0.6F

    Column(Modifier.padding(5.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                modifier = Modifier
                    .weight(nodeColumnWeight)
            ) {
                if (uiState.isYearDisplay) {
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
                            )
                            if (uiState.isDateDisplay) {
                                Text(
                                    text = uiState.dateHeaders?.get(index) ?: "",
                                    style = MaterialTheme.typography.bodySmall,
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
                                        textAlign = TextAlign.Center
                                    )
                                    if (uiState.isTimeDisplay) {
                                        Text(
                                            color = color,
                                            text = "${uiState.nodeStartHeaders[index]}\n${uiState.nodeEndHeaders[index]}",
                                            fontSize = 10.sp,
                                            lineHeight = 10.sp,
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
                                    // 筛选出当前遍历的星期的课程
                                    val coursesOnWeek = uiState.courses.filter {
                                        it.key.first == week
                                    }

                                    for (node in 1..5) {
                                        // 筛选出当前遍历的节次的课程
                                        val coursesOnBothWeekAndNode = coursesOnWeek.filter {
                                            it.key.second == node
                                        }.values
                                        // 筛选出符合当前节次和星期的本周的课程
                                        val currentCourse: Course? = coursesOnBothWeekAndNode.find {
                                            it.weekNoList.contains(uiState.browsedWeek)
                                        }
                                        // 筛选出符合当前节次和星期的非本周课程
                                        val nonCurWeekCourses = coursesOnBothWeekAndNode.filter {
                                            it != currentCourse
                                        }
                                        // 点按时弹出的对话框中显示的课程
                                        val coursesToShowWhenClicked =
                                            if (uiState.isOtherCourseDisplay) {
                                                (if (currentCourse == null) emptyList() else listOf(
                                                    currentCourse
                                                )).plus(nonCurWeekCourses)
                                            } else {
                                                if (currentCourse == null) {
                                                    emptyList()
                                                } else {
                                                    listOf(currentCourse)
                                                }
                                            }
                                        val alpha =
                                            if (currentCourse != null) 1f else if (coursesToShowWhenClicked.isEmpty()) 0f else 0.3f

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
                                                    enabled = coursesToShowWhenClicked.isNotEmpty(),
                                                    onClick = {
                                                        viewModel.setContentInCourseDetailDialog(
                                                            coursesToShowWhenClicked.toList()
                                                        )
                                                        viewModel.showCourseDetailDialog()
                                                    }
                                                )
                                        ) {
                                            if (coursesToShowWhenClicked.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(15.dp)
                                                        .clip(CutCornerShape(topStart = 15.dp))
                                                        .background(
                                                            if (coursesToShowWhenClicked.count() > 1) MaterialTheme.colorScheme.primary else Color.Transparent
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
                                                        text = coursesToShowWhenClicked.first().courseName
                                                            ?: "",
                                                        style = MaterialTheme.typography.bodySmall.merge(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        textAlign = TextAlign.Center,
                                                        color = foreground,
                                                    )
                                                    Text(
                                                        text = coursesToShowWhenClicked.first().classroomName
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

@SuppressLint("UnrememberedMutableState")
@Composable
fun CreateActionsForSchedule(viewModel: ScheduleViewModel) {

    var showWeekSliderDialog by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showWeekSliderDialog = true
    }) {
        Icon(
            imageVector = rememberTune(),
            contentDescription = null
        )
    }

    var sliderPosition by remember { mutableFloatStateOf(viewModel.uiState.value.currentWeek.toFloat()) }

    val hapticFeedback = LocalHapticFeedback.current

    if (showWeekSliderDialog) {
        AlertDialog(
            confirmButton = {
                TextButton({
                    viewModel.setBrowsedWeek(sliderPosition.toInt())
                    showWeekSliderDialog = false
                }) {
                    Text("应用")
                }
            },
            dismissButton = {
                TextButton({
                    sliderPosition = viewModel.uiState.value.currentWeek.toFloat()
                    viewModel.resetBrowsedWeek()
                    showWeekSliderDialog = false
                }) {
                    Text("重置")
                }
            },
            onDismissRequest = { showWeekSliderDialog = false },
            title = { Text("切换周数") },
            text = {
                Column {
                    Slider(
                        value = sliderPosition,
                        onValueChange = {
                            if (abs(it - sliderPosition) >= 0.9f) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            sliderPosition = it
                        },
                        steps = 20,
                        valueRange = 1f..20f,
                    )
                    Text(text = "切换到第 ${sliderPosition.toInt()} 周")
                }
            }
        )
    }
}
