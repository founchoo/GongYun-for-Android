package com.dart.campushelper.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.dart.campushelper.ui.MainActivity
import dagger.hilt.EntryPoints
import kotlinx.coroutines.launch

class AppWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            AppWidgetContent(viewModel = EntryPoints.get(context, EntryPoint::class.java).vm())
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AppWidgetContent(viewModel: AppWidgetViewModel) {

    val uiState by viewModel.uiState.collectAsState()

    MainActivity.scope.launch {
        viewModel.observeStartLocalDate()
    }

    MainActivity.scope.launch {
        // viewModel.getTodaySchedule()
    }

    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .appWidgetBackgroundCornerRadius()
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "当前第 ${uiState.currentWeek} 周 / 星期 ${uiState.dayOfWeek}",
                    modifier = GlanceModifier
                        .defaultWeight()
                        .padding(8.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GlanceTheme.colors.primary
                    )
                )
                Button(text = "刷新", onClick = {
                    MainActivity.scope.launch {
                        // viewModel.getTodaySchedule()
                    }
                })
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            if (uiState.courses.isEmpty()) {
                Text(
                    text = "今天暂无课程",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GlanceTheme.colors.primary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = GlanceModifier
                        .padding(8.dp)
                        .fillMaxSize()
                )
            } else {
                LazyColumn() {
                    itemsIndexed(
                        items = uiState.courses
                    ) { _, course ->
                        val startNode = course.nodeNo!!
                        Row(modifier = GlanceModifier.fillMaxWidth().padding(8.dp)) {
                            Text(
                                "$startNode-${startNode + 1}",
                                modifier = GlanceModifier.defaultWeight().padding(
                                    vertical = 2.dp
                                ),
                                style = TextStyle(textAlign = TextAlign.Start, color = GlanceTheme.colors.primary)
                            )
                            Text(
                                course.courseName ?: "<空闲>",
                                modifier = GlanceModifier.defaultWeight().padding(
                                    vertical = 2.dp
                                ),
                                style = TextStyle(textAlign = TextAlign.Center, color = GlanceTheme.colors.primary)
                            )
                            Text(
                                course.classroomName ?: "",
                                modifier = GlanceModifier.defaultWeight().padding(
                                    vertical = 2.dp
                                ),
                                style = TextStyle(textAlign = TextAlign.End, color = GlanceTheme.colors.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}
