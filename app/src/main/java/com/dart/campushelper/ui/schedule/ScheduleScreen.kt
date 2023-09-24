package com.dart.campushelper.ui.schedule

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dart.campushelper.model.Course
import com.dart.campushelper.utils.NoLoginPlaceholder
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.material3.placeholder
import io.github.fornewid.placeholder.material3.shimmer

@SuppressLint("UnrememberedMutableState")
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLogin) {
        Column(
            modifier = Modifier
                .padding(5.dp, 0.dp, 3.dp, 3.dp)
        ) {
            // Week header, e.g. 周一 周二 ...
            Row {
                Box(
                    modifier = Modifier
                        .weight(0.5F)
                )
                Row(
                    modifier = Modifier
                        .weight(7F)
                        .align(Alignment.Top)
                ) {
                    uiState.weekHeaders.forEachIndexed { index, week ->
                        Box(
                            modifier = Modifier
                                .weight(1F)
                        ) {
                            Text(
                                text = week,
                                color = if (uiState.dayOfWeek - 1 == index) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(0.dp, 0.dp, 0.dp, 5.dp)
                            )
                        }
                    }
                }
            }

            Row {
                // 节点数
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.5F),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    uiState.nodeHeaders.forEachIndexed { index, node ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                color = if (uiState.currentNode - 1 == index) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                text = node.toString(),
                                textAlign = TextAlign.Center
                            )
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
                                .fillMaxHeight()
                                .weight(1F)
                        ) {
                            val coursesOnWeek = uiState.courses.filter {
                                it.key.first == week
                            }

                            for (node in 1..5) {

                                val coursesOnBothWeekAndNode = coursesOnWeek.filter {
                                    it.key.second == node
                                }.values

                                var currentCourse: Course? = null
                                for (coursesOnBothWeekAndNodeItem in coursesOnBothWeekAndNode) {
                                    if (coursesOnBothWeekAndNodeItem.weekNoList.contains(
                                            uiState.currentWeek
                                        )
                                    ) {
                                        currentCourse = coursesOnBothWeekAndNodeItem
                                    }
                                }
                                var futureCourse: Course? = null
                                if (currentCourse == null && coursesOnBothWeekAndNode.isNotEmpty()) {
                                    futureCourse = coursesOnBothWeekAndNode.first()
                                }

                                val foreground = MaterialTheme.colorScheme.secondary.copy(
                                    alpha = if (coursesOnBothWeekAndNode.isEmpty()) 0f else if (currentCourse != null) 1f else 0.3f
                                )
                                val background = MaterialTheme.colorScheme.secondaryContainer.copy(
                                    alpha = if (coursesOnBothWeekAndNode.isEmpty()) 0f else if (currentCourse != null) 1f else 0.3f
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1F)
                                        .fillMaxWidth()
                                        .padding(3.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(background)
                                        .clickable(
                                            enabled = coursesOnBothWeekAndNode.isNotEmpty(),
                                            onClick = {
                                                viewModel.setContentInCourseDetailDialog(
                                                    coursesOnBothWeekAndNode.toList()
                                                )
                                                viewModel.showCourseDetailDialog()
                                            }
                                        )
                                        .placeholder(
                                            visible = uiState.isTimetableLoading,
                                            highlight = PlaceholderHighlight.shimmer()
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clip(CutCornerShape(topStart = 15.dp))
                                            .background(
                                                if (coursesOnBothWeekAndNode.count() > 1) MaterialTheme.colorScheme.primary else Color.Transparent
                                            )
                                            .align(Alignment.BottomEnd)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = currentCourse?.courseName
                                                ?: (futureCourse?.courseName ?: ""),
                                            style = MaterialTheme.typography.bodySmall.merge(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            textAlign = TextAlign.Center,
                                            color = foreground,
                                        )
                                        Text(
                                            text = currentCourse?.classroomName
                                                ?: (futureCourse?.classroomName ?: ""),
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
    } else {
        NoLoginPlaceholder()
    }

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
