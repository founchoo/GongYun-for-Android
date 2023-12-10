package com.dart.campushelper.ui.schedule

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dart.campushelper.R
import com.dart.campushelper.model.Course
import com.dart.campushelper.ui.component.HorizontalPagerTabRow
import com.dart.campushelper.ui.component.LoadOnlineDataLayout
import com.dart.campushelper.viewmodel.ScheduleUiState
import com.dart.campushelper.viewmodel.ScheduleViewModel
import java.time.format.DateTimeFormatter
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleTable(uiState: ScheduleUiState, viewModel: ScheduleViewModel, pagerState: PagerState) {
    val nodeColumnWeight = 0.65F
    val unimportantAlpha = 0.3f

    val minHeight = max(
        (LocalConfiguration.current.screenHeightDp - 64 - 80) / uiState.nodeHeaders.toList().size,
        100
    )

    LaunchedEffect(uiState.currentWeek) {
        uiState.currentWeek?.let { pagerState.scrollToPage(it - 1) }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setBrowsedWeek(pagerState.currentPage + 1)
    }

    LoadOnlineDataLayout(
        dataSource = uiState.courses,
        loadData = {
            viewModel.loadSchedule(uiState.browsedSemester)
        },
        autoLoadingArgs = arrayOf(uiState.browsedSemester),
        contentWhenDataSourceIsEmpty = {
            Text(
                text = stringResource(R.string.no_course),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        contentWhenDataSourceIsNotEmpty = {
            HorizontalPagerTabRow(
                pagerState = pagerState,
                isHeaderVisible = false,
                tabs = (1..20).map { it },
                dataSource = uiState.courses.data,
            ) { data, tab ->
                val browsedWeek = tab
                val coursesOnCell =
                    mutableMapOf<Pair<Int, Int>, List<Course>>()
                data.forEach { course ->
                    if ((!uiState.isOtherCourseDisplay && course.weekNoList.contains(
                            browsedWeek
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
                Column(Modifier.padding(horizontal = 5.dp)) {
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
                                    text = stringResource(
                                        R.string.year_indicator,
                                        uiState.startLocalDate?.plusDays(
                                            (browsedWeek - 1) * 7L
                                        )?.format(
                                            DateTimeFormatter.ofPattern("yyyy")
                                        )?.takeLast(2) ?: ""
                                    ),
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
                            listOf(
                                stringResource(R.string.monday),
                                stringResource(R.string.tuesday),
                                stringResource(R.string.wednesday),
                                stringResource(R.string.thursday),
                                stringResource(R.string.friday),
                                stringResource(R.string.saturday),
                                stringResource(R.string.sunday),
                            ).forEachIndexed { index, week ->
                                Box(
                                    modifier = Modifier
                                        .weight(1F)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.align(Alignment.Center)
                                    ) {
                                        val color =
                                            if (uiState.dayOfWeek - 1 == index && browsedWeek == uiState.currentWeek && uiState.browsedSemester == uiState.semesters.last()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
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
                                                text = uiState.startLocalDate?.plusDays(
                                                    index + 7L * (browsedWeek - 1)
                                                )
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
                    Column(
                        Modifier.verticalScroll(rememberScrollState())
                    ) {
                        uiState.nodeHeaders.forEach { node ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .requiredHeight(minHeight.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(nodeColumnWeight)
                                        .requiredHeight(minHeight.dp),
                                    verticalArrangement = Arrangement.SpaceAround,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    (node * 2 - 1..node * 2).forEach {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            val color =
                                                if (uiState.currentNode == it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                    alpha = unimportantAlpha
                                                )
                                            Text(
                                                color = color,
                                                text = it.toString(),
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                            if (uiState.isTimeDisplay) {
                                                Text(
                                                    color = color,
                                                    text = "${uiState.nodeStartHeaders[it - 1]}\n${uiState.nodeEndHeaders[it - 1]}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    lineHeight = 12.sp,
                                                )
                                            }
                                        }
                                    }
                                }
                                (1..7).forEach { dayOfWeek ->
                                    var expanded by remember { mutableStateOf(false) }
                                    // 筛选出当前遍历的星期号和节次的课程
                                    val courses = coursesOnCell[Pair(dayOfWeek, node)]
                                    val currentWeekCourse = courses?.find {
                                        it.weekNoList.contains(browsedWeek)
                                    }
                                    val alpha = if (currentWeekCourse != null)
                                        1f else if (courses.isNullOrEmpty())
                                        0f else unimportantAlpha
                                    val foreground =
                                        MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
                                    val background =
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = alpha)
                                    val isCurrentTimeCourse =
                                        (uiState.currentNode + 1) / 2 == node && uiState.dayOfWeek == dayOfWeek
                                    Box(
                                        modifier = Modifier
                                            .weight(1F)
                                            .fillMaxWidth()
                                            .requiredHeight(minHeight.dp)
                                            .padding(3.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(background)
                                            .combinedClickable(
                                                interactionSource = remember {
                                                    MutableInteractionSource()
                                                },
                                                indication = rememberRipple(bounded = true),
                                                onClick = {
                                                    if (courses?.isNotEmpty() == true) {
                                                        viewModel.setContentInCourseDetailDialog(
                                                            courses.toList()
                                                        )
                                                        viewModel.setIsCourseDetailDialogOpen(
                                                            true
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    viewModel.setDayOfWeekOnHoldingCourse(
                                                        dayOfWeek
                                                    )
                                                    viewModel.setNodeNoOnHoldingCourse(
                                                        node
                                                    )
                                                    expanded = true
                                                },
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
                                                    currentWeekCourse
                                                        ?: courses.first()
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
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.occupied_room)) },
                                                onClick = {
                                                    expanded = false
                                                    viewModel.setIsShowNonEmptyClassroomSheet(
                                                        true
                                                    )
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.free_room)) },
                                                onClick = {
                                                    expanded = false
                                                    viewModel.setIsShowEmptyClassroomSheet(
                                                        true
                                                    )
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}