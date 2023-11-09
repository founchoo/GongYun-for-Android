package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
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
import com.dart.campushelper.model.Course
import com.dart.campushelper.ui.rememberTune
import com.dart.campushelper.utils.PreferenceHeader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.abs

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
                Column {
                    val coursesOnBrowsedWeek =
                        uiState.contentInCourseDetailDialog.filter { it.weekNoList.contains(uiState.browsedWeek) }
                    val coursesOnNonBrowsedWeek =
                        uiState.contentInCourseDetailDialog.filter { !it.weekNoList.contains(uiState.browsedWeek) }
                    if (coursesOnBrowsedWeek.isNotEmpty()) {
                        PreferenceHeader(text = "当前浏览周课程")
                        Column {
                            coursesOnBrowsedWeek.forEach { course ->
                                ListItem(
                                    headlineContent = { Text("${course.courseName}") },
                                    supportingContent = { Text("${course.classroomName} / 第 ${course.zc} 周") },
                                    trailingContent = { Text("${course.teacherName}") }
                                )
                            }
                        }
                    }
                    if (coursesOnNonBrowsedWeek.isNotEmpty()) {
                        PreferenceHeader(text = "非当前浏览周课程")
                        Column {
                            coursesOnNonBrowsedWeek.forEach { course ->
                                ListItem(
                                    headlineContent = { Text("${course.courseName}") },
                                    supportingContent = { Text("${course.classroomName} / 第 ${course.zc} 周") },
                                    trailingContent = { Text("${course.teacherName}") }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddContent(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {

    val refreshScope = rememberCoroutineScope()

    fun refresh() = refreshScope.launch {
        viewModel.getCourses()
    }

    val refreshState = rememberPullRefreshState(uiState.isTimetableLoading, ::refresh)
    val nodeColumnWeight = 0.65F
    val unimportantAlpha = 0.3f

    val coursesOnCell =
        mutableMapOf<Pair<Int, Int>, List<Course>>()
    uiState.courses.forEach { course ->
        if ((!uiState.isOtherCourseDisplay && course.weekNoList.contains(
                uiState.browsedWeek
            )) || uiState.isOtherCourseDisplay
        ) {
            val key = Pair(course.weekDayNo!!, course.nodeNo!!)
            if (coursesOnCell.containsKey(key)) {
                coursesOnCell[key] =
                    coursesOnCell[key]!! + course
            } else {
                coursesOnCell[key] = listOf(course)
            }
        }
    }

    Column(Modifier.padding(5.dp)) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 年份标识
            Box(
                modifier = Modifier
                    .weight(nodeColumnWeight)
            ) {
                if (uiState.isYearDisplay) {
                    Text(
                        text = "${
                            uiState.startLocalDate?.plusDays((uiState.browsedWeek - 1) * 7L)
                                ?.format(
                                    DateTimeFormatter.ofPattern("yyyy")
                                )?.takeLast(2) ?: ""
                        }年",
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
            // 星期表头
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
                                if (uiState.dayOfWeek - 1 == index && uiState.browsedWeek == uiState.currentWeek && uiState.browsedSemester == uiState.semesters.last()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                                    .copy(
                                    alpha = unimportantAlpha
                                )
                            Text(
                                text = week,
                                color = color,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (uiState.isDateDisplay) {
                                Text(
                                    text = uiState.startLocalDate?.plusDays(index + 7L * (uiState.browsedWeek - 1))
                                        ?.format(DateTimeFormatter.ofPattern("M-d"))
                                        ?: "",
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
                                        if (uiState.currentNode - 1 == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                            alpha = unimportantAlpha
                                        )
                                    Text(
                                        color = color,
                                        text = node.toString(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
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
                                        val courses = coursesOnCell[Pair(week, node)]
                                        val currentWeekCourse =
                                            courses?.find { it.weekNoList.contains(uiState.browsedWeek) }
                                        val alpha =
                                            if (currentWeekCourse != null)
                                                1f
                                            else if (courses.isNullOrEmpty())
                                                0f
                                            else
                                                unimportantAlpha
                                        val foreground =
                                            MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
                                        val background =
                                            MaterialTheme.colorScheme.secondaryContainer.copy(
                                                alpha = alpha
                                            )
                                        Box(
                                            modifier = Modifier
                                                .weight(1F)
                                                .fillMaxWidth()
                                                .padding(3.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(background)
                                                .clickable(
                                                    enabled = courses?.isNotEmpty() ?: false,
                                                    onClick = {
                                                        viewModel.setContentInCourseDetailDialog(
                                                            courses?.toList() ?: emptyList()
                                                        )
                                                        viewModel.showCourseDetailDialog()
                                                    }
                                                )
                                        ) {
                                            if (!courses.isNullOrEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(15.dp)
                                                        .clip(CutCornerShape(topStart = 15.dp))
                                                        .background(
                                                            if (courses.count() > 1)
                                                                foreground.copy(
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
                                                    val displayedCourse =
                                                        currentWeekCourse ?: courses.first()
                                                    Text(
                                                        text = displayedCourse.courseName
                                                            ?: "",
                                                        style = MaterialTheme.typography.bodySmall.merge(
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        textAlign = TextAlign.Center,
                                                        color = foreground,
                                                    )
                                                    Text(
                                                        text = displayedCourse.classroomName
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

    val scope = CoroutineScope(Dispatchers.IO)
    var showWeekSliderDialog by remember { mutableStateOf(false) }

    IconButton(onClick = {
        showWeekSliderDialog = true
    }) {
        Icon(
            imageVector = rememberTune(),
            contentDescription = null
        )
    }

    var weekSliderPosition by remember { mutableFloatStateOf(uiState.currentWeek.toFloat()) }
    var semesterDropdownMenuExpanded by remember { mutableStateOf(false) }
    var selectedSemester by remember { mutableStateOf(uiState.browsedSemester) }
    val hapticFeedback = LocalHapticFeedback.current

    if (showWeekSliderDialog) {
        AlertDialog(
            confirmButton = {
                TextButton({
                    viewModel.setBrowsedWeek(weekSliderPosition.toInt())
                    if (selectedSemester != uiState.browsedSemester) {
                        scope.launch {
                            viewModel.getCourses(selectedSemester)
                        }
                    }
                    viewModel.setBrowsedSemester(selectedSemester)
                    showWeekSliderDialog = false
                }) {
                    Text("应用")
                }
            },
            dismissButton = {
                TextButton({
                    weekSliderPosition = uiState.currentWeek.toFloat()
                    viewModel.setBrowsedWeek(uiState.currentWeek)
                    selectedSemester = uiState.semesters.last()
                    if (selectedSemester != uiState.browsedSemester) {
                        scope.launch {
                            viewModel.getCourses(selectedSemester)
                        }
                    }
                    viewModel.setBrowsedSemester(uiState.semesters.last().toString())
                    showWeekSliderDialog = false
                }) {
                    Text("重置")
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
                    ) {
                        Text(text = "切换周数")
                        Text(
                            text = "第 ${weekSliderPosition.toInt()} 周",
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
                                    modifier = if (semester == selectedSemester) {
                                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                    } else {
                                        Modifier
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
