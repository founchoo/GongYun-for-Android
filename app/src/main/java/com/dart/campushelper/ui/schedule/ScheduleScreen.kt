package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.dart.campushelper.R
import com.dart.campushelper.model.Course
import com.dart.campushelper.ui.component.BasicBottomSheetWithDataList
import com.dart.campushelper.ui.component.PreferenceHeader
import com.dart.campushelper.ui.component.TooltipIconButton
import com.dart.campushelper.ui.rememberChevronLeft
import com.dart.campushelper.ui.rememberChevronRight
import com.dart.campushelper.ui.rememberEditNote
import com.dart.campushelper.ui.rememberEventUpcoming
import com.dart.campushelper.ui.rememberLightbulb
import com.dart.campushelper.utils.combinationOfGradeAndSemesterToText
import com.dart.campushelper.utils.convertDayOfWeekToChinese
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@SuppressLint("UnrememberedMutableState")
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    ScheduleTable(uiState = uiState, viewModel = viewModel)

    AddContent(uiState = uiState, viewModel = viewModel)

    if (uiState.isCourseDetailDialogOpen) {
        AlertDialog(
            confirmButton = {},
            dismissButton = {
                TextButton({
                    viewModel.setIsCourseDetailDialogOpen(false)
                }) {
                    Text(stringResource(R.string.close))
                }
            },
            onDismissRequest = { viewModel.setIsCourseDetailDialogOpen(false) },
            title = { Text(stringResource(R.string.schedule_switcher_title)) },
            text = {
                Column {
                    val coursesOnBrowsedWeek =
                        uiState.contentInCourseDetailDialog.filter { it.weekNoList.contains(uiState.browsedWeek) }
                    val coursesOnNonBrowsedWeek =
                        uiState.contentInCourseDetailDialog.filter { !it.weekNoList.contains(uiState.browsedWeek) }
                    if (coursesOnBrowsedWeek.isNotEmpty()) {
                        PreferenceHeader(text = stringResource(R.string.current_observed_week_course_title))
                        Column {
                            coursesOnBrowsedWeek.forEach { course ->
                                ListItem(
                                    headlineContent = { Text("${course.courseName}") },
                                    supportingContent = {
                                        Text(
                                            "${course.classroomName} / ${
                                                String.format(
                                                    stringResource(
                                                        R.string.week_indicator
                                                    ), course.zc
                                                )
                                            }"
                                        )
                                    },
                                    trailingContent = { Text("${course.teacherName}") }
                                )
                            }
                        }
                    }
                    if (coursesOnNonBrowsedWeek.isNotEmpty()) {
                        PreferenceHeader(text = stringResource(R.string.non_current_observed_week_course_title))
                        Column {
                            coursesOnNonBrowsedWeek.forEach { course ->
                                ListItem(
                                    headlineContent = { Text("${course.courseName}") },
                                    supportingContent = {
                                        Text(
                                            "${course.classroomName} / ${
                                                String.format(
                                                    stringResource(
                                                        R.string.week_indicator
                                                    ), course.zc
                                                )
                                            }"
                                        )
                                    },
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
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun AddContent(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(
            min(screenHeight, screenWidth) / 2
        ),
        tooltip = {
            RichTooltip(
                title = {
                    Text(
                        stringResource(R.string.check_room_tip_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                action = {
                    TextButton(
                        onClick = {
                            uiState.holdingCourseTooltipState.dismiss()
                            scope.launch {
                                uiState.holdingSemesterTooltipState.show()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.show_next_tip))
                    }
                }) {
                Text(stringResource(R.string.check_room_tip_desc))
            }
        },
        state = uiState.holdingCourseTooltipState
    ) {
    }

    val args = arrayOf(
        uiState.browsedWeek.toString(),
        convertDayOfWeekToChinese(
            uiState.dayOfWeekOnHoldingCourse
        ),
        "${uiState.nodeNoOnHoldingCourse * 2 - 1} - ${uiState.nodeNoOnHoldingCourse * 2}"
    )

    BasicBottomSheetWithDataList(
        isBottomSheetShow = uiState.isShowNonEmptyClassroomSheet,
        title = R.string.occupied_room,
        descriptionWhenItemSourceIsEmpty = String.format(
            stringResource(R.string.occupied_room_without_content_desc),
            *args
        ),
        descriptionWhenItemSourceIsNotEmpty = String.format(
            stringResource(R.string.occupied_room_with_content_desc),
            *args
        ),
        isContentLoading = uiState.isNonEmptyClrLoading,
        itemSource = uiState.nonEmptyClassrooms,
        onDismissRequest = {
            viewModel.setIsShowNonEmptyClassroomSheet(false)
        }
    ) {
        var buildingIndexSelected by remember { mutableIntStateOf(0) }
        SecondaryScrollableTabRow(
            selectedTabIndex = buildingIndexSelected,
        ) {
            uiState.buildingNames.forEachIndexed { index, buildingName ->
                Tab(
                    text = { Text(buildingName) },
                    selected = buildingIndexSelected == index,
                    onClick = { buildingIndexSelected = index },
                )
            }
        }
        LazyColumn {
            items(it.filter {
                (it.classroomName?.split("-")?.get(0)
                    ?: "") == uiState.buildingNames[buildingIndexSelected]
            }) {
                ListItem(
                    headlineContent = { Text(text = "${it.classroomNameHtml ?: ""} | ${it.courseName ?: ""}") },
                    trailingContent = { Text(text = it.teacherName ?: "") },
                    supportingContent = {
                        Text(
                            text = it.className?.replace(
                                ",",
                                " | "
                            ) ?: ""
                        )
                    },
                )
            }
        }
    }

    BasicBottomSheetWithDataList(
        isBottomSheetShow = uiState.isShowEmptyClassroomSheet,
        title = R.string.free_room,
        descriptionWhenItemSourceIsEmpty = String.format(
            stringResource(R.string.empty_room_without_content_desc),
            *args
        ),
        descriptionWhenItemSourceIsNotEmpty = String.format(
            stringResource(R.string.empty_room_with_content_desc),
            *args
        ),
        isContentLoading = uiState.isEmptyClrLoading,
        itemSource = uiState.emptyClassrooms,
        onDismissRequest = {
            viewModel.setIsShowEmptyClassroomSheet(false)
        }
    ) {
        var buildingIndexSelected by remember { mutableIntStateOf(0) }
        SecondaryScrollableTabRow(
            selectedTabIndex = buildingIndexSelected,
        ) {
            uiState.buildingNames.forEachIndexed { index, buildingName ->
                Tab(
                    text = { Text(buildingName) },
                    selected = buildingIndexSelected == index,
                    onClick = { buildingIndexSelected = index },
                )
            }
        }
        LazyColumn {
            items(it.filter {
                it.buildingName == uiState.buildingNames[buildingIndexSelected]
            }) {
                ListItem(
                    headlineContent = { Text(text = it.roomName ?: "") },
                    supportingContent = {
                        Text(
                            text = "${it.buildingName}${
                                it.floor.let { floor ->
                                    if (floor == null) {
                                        ""
                                    } else {
                                        " | $floor 楼"
                                    }
                                }
                            }"
                        )
                    },
                    trailingContent = {
                        Text(
                            text = it.functionalAreaName ?: ""
                        )
                    },
                )
            }
        }
    }

    BasicBottomSheetWithDataList(
        isBottomSheetShow = uiState.isShowScheduleNotesSheet,
        title = R.string.schedule_other_info,
        descriptionWhenItemSourceIsEmpty = R.string.schedule_other_info_empty,
        isContentLoading = uiState.isScheduleNotesLoading,
        itemSource = uiState.scheduleNotes,
        onDismissRequest = {
            viewModel.setIsShowScheduleNotesSheet(false)
        }
    ) {
        LazyColumn {
            items(it) {
                ListItem(
                    headlineContent = { Text(text = it.title) },
                    supportingContent = { Text(text = it.description) },
                )
            }
        }
    }

    BasicBottomSheetWithDataList(
        isBottomSheetShow = uiState.isShowPlannedScheduleSheet,
        title = R.string.planned_schedule_info_title,
        descriptionWhenItemSourceIsEmpty = R.string.planned_schedule_info_empty,
        isContentLoading = uiState.isPlannedScheduleLoading,
        itemSource = uiState.plannedSchedule,
        onDismissRequest = {
            viewModel.setIsShowPlannedScheduleSheet(false)
        }
    ) {
        LazyColumn {
            items(it) {
                ListItem(
                    headlineContent = { Text(text = it.courseName.toString()) },
                    supportingContent = { Text(text = it.hostInstituteName.toString()) },
                    trailingContent = {
                        Text(
                            text = combinationOfGradeAndSemesterToText(
                                (it.grade ?: "0").toInt(),
                                (it.semester ?: "0").toInt()
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ScheduleTable(uiState: ScheduleUiState, viewModel: ScheduleViewModel) {
    val scope = rememberCoroutineScope()
    val refreshState = rememberPullRefreshState(uiState.isTimetableLoading, {
        scope.launch {
            viewModel.getCourses(uiState.browsedSemester)
        }
    })
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
                        text = String.format(
                            stringResource(R.string.year_indicator),
                            uiState.startLocalDate?.plusDays((uiState.browsedWeek - 1) * 7L)
                                ?.format(
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
                        if (uiState.courses.isEmpty()) {
                            Box(
                                Modifier
                                    .weight(1F, true)
                                    .fillMaxSize()
                            ) {
                                Text(
                                    text = stringResource(R.string.no_course),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        } else {
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
                                (1..7).forEach { dayOfWeek ->
                                    Column(
                                        modifier = Modifier
                                            .fillParentMaxHeight()
                                            .weight(1F)
                                    ) {
                                        for (node in 1..5) {
                                            var expanded by remember { mutableStateOf(false) }
                                            // 筛选出当前遍历的星期号和节次的课程
                                            val courses = coursesOnCell[Pair(dayOfWeek, node)]
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
                                            val isCurrentTimeCourse =
                                                (uiState.currentNode + 1) / 2 == node && uiState.dayOfWeek == dayOfWeek
                                            Box(
                                                modifier = Modifier
                                                    .weight(1F)
                                                    .fillMaxWidth()
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
                                                            viewModel.setNodeNoOnHoldingCourse(node)
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
                                                DropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false },
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text(stringResource(R.string.occupied_room)) },
                                                        onClick = {
                                                            expanded = false
                                                            viewModel.viewModelScope.launch {
                                                                viewModel.getNonEmptyClassrooms(
                                                                    dayOfWeek,
                                                                    node
                                                                )
                                                            }
                                                            viewModel.setIsShowNonEmptyClassroomSheet(
                                                                true
                                                            )
                                                        },
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text(stringResource(R.string.free_room)) },
                                                        onClick = {
                                                            expanded = false
                                                            viewModel.viewModelScope.launch {
                                                                viewModel.loadEmptyClassroom(
                                                                    dayOfWeek,
                                                                    node
                                                                )
                                                            }
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

    val scope = rememberCoroutineScope()
    var isMenuExpanded by remember { mutableStateOf(false) }

    TooltipIconButton(
        label = R.string.last_week,
        imageVector = rememberChevronLeft(),
        enabled = uiState.browsedWeek > 1,
        onClick = {
            viewModel.setBrowsedWeek(uiState.browsedWeek - 1)
        },
    )

    TooltipIconButton(
        label = R.string.next_week,
        imageVector = rememberChevronRight(),
        enabled = uiState.browsedWeek < 20,
        onClick = {
            viewModel.setBrowsedWeek(uiState.browsedWeek + 1)
        },
    )

    TooltipIconButton(
        label = R.string.more,
        imageVector = Icons.Outlined.MoreVert,
        onClick = {
            isMenuExpanded = true
        },
    )

    DropdownMenu(
        expanded = isMenuExpanded,
        onDismissRequest = { isMenuExpanded = false },
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(rememberLightbulb(), null)
            },
            text = { Text(stringResource(R.string.help)) },
            onClick = {
                scope.launch {
                    uiState.holdingCourseTooltipState.show()
                }
                isMenuExpanded = false
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(rememberEditNote(), null)
            },
            text = { Text(stringResource(R.string.schedule_other_info)) },
            onClick = {
                isMenuExpanded = false
                viewModel.setIsShowScheduleNotesSheet(true)
                viewModel.loadScheduleNotes()
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(rememberEventUpcoming(), null)
            },
            text = { Text(stringResource(R.string.plan_courses)) },
            onClick = {
                isMenuExpanded = false
                viewModel.setIsShowPlannedScheduleSheet(true)
                viewModel.loadPlannedSchedule()
            },
        )
    }

    var weekSliderPosition by remember { mutableFloatStateOf(uiState.browsedWeek.toFloat()) }
    var semesterDropdownMenuExpanded by remember { mutableStateOf(false) }
    var selectedSemester by remember { mutableStateOf(uiState.browsedSemester) }
    val hapticFeedback = LocalHapticFeedback.current

    if (uiState.isShowWeekSliderDialog) {
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
                    viewModel.setIsShowWeekSliderDialog(false)
                }) {
                    Text(stringResource(R.string.apply))
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
                    viewModel.setIsShowWeekSliderDialog(false)
                }) {
                    Text(stringResource(R.string.reset))
                }
            },
            onDismissRequest = {
                weekSliderPosition = uiState.browsedWeek.toFloat()
                selectedSemester = uiState.browsedSemester
                viewModel.setIsShowWeekSliderDialog(false)
            },
            title = { Text(stringResource(R.string.switch_schedule)) },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                    ) {
                        Text(text = stringResource(R.string.switch_schedule))
                        Text(
                            text = String.format(
                                stringResource(
                                    R.string.week_indicator
                                ), weekSliderPosition.toInt()
                            ),
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
                    Text(text = stringResource(R.string.switch_year_semester))
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
                            label = { Text(stringResource(R.string.selected_year_semester)) },
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
