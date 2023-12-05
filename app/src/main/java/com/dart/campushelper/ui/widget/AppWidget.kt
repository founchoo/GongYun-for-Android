package com.dart.campushelper.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.dart.campushelper.R
import com.dart.campushelper.utils.convertDayOfWeekToChinese
import com.dart.campushelper.viewmodel.AppWidgetViewModel
import dagger.hilt.EntryPoints
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

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
    val scope = rememberCoroutineScope()
    scope.launch {
        viewModel.getTodaySchedule()
    }

    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .appWidgetBackgroundCornerRadius(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${
                    stringResource(R.string.week_indicator, uiState.currentWeek)
                } / ${convertDayOfWeekToChinese(uiState.dayOfWeek)} / ${
                    LocalDate.now().format(
                        DateTimeFormatter.ofPattern("MM-dd")
                    )
                }",
                modifier = GlanceModifier
                    .height(36.dp),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onBackground,
                )
            )

            if (uiState.courses.isEmpty()) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.outline_local_cafe_black_24dp),
                        contentDescription = null,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .size(48.dp)
                    )
                    Text(
                        text = "今天暂无课程",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onBackground,
                            textAlign = TextAlign.Center
                        ),
                        modifier = GlanceModifier
                            .padding(8.dp)
                            .fillMaxSize()
                    )
                }
            } else {
                Column(
                    modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    uiState.courses.forEach { course ->
                        val startNode = course.nodeNo!!
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(8.dp).defaultWeight()
                        ) {
                            Text(
                                "$startNode-${startNode + 1}",
                                modifier = GlanceModifier.defaultWeight().padding(
                                    vertical = 2.dp
                                ),
                                style = TextStyle(
                                    textAlign = TextAlign.Start,
                                    color = GlanceTheme.colors.primary
                                )
                            )
                            Text(
                                course.courseName ?: "<空闲>",
                                modifier = GlanceModifier.defaultWeight().padding(
                                    vertical = 2.dp
                                ),
                                style = TextStyle(
                                    textAlign = TextAlign.Center,
                                    color = GlanceTheme.colors.onBackground,
                                )
                            )
                            Text(
                                course.classroomName ?: "",
                                modifier = GlanceModifier.defaultWeight().padding(
                                    vertical = 2.dp
                                ),
                                style = TextStyle(
                                    textAlign = TextAlign.End,
                                    color = GlanceTheme.colors.onBackground,
                                )
                            )
                        }
                    }
                }
            }

            Button(
                text = "刷新",
                onClick = {
                    scope.launch {
                        viewModel.getTodaySchedule()
                    }
                },
                modifier = GlanceModifier.height(36.dp)
            )
        }
    }
}
