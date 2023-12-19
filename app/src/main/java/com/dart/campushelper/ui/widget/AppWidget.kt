package com.dart.campushelper.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.dart.campushelper.R
import com.dart.campushelper.ui.main.MainActivity
import com.dart.campushelper.utils.DayOfWeek
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

    @Composable
    fun AppWidgetContent(viewModel: AppWidgetViewModel) {

        val uiState by viewModel.uiState.collectAsState()

        val scope = rememberCoroutineScope()

        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .appWidgetBackground()
                    .background(GlanceTheme.colors.background)
                    .appWidgetBackgroundCornerRadius(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.currentWeek != null) {
                    Row(GlanceModifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text(
                            text = "${
                                LocalContext.current.getString(
                                    R.string.week_indicator,
                                    uiState.currentWeek!!.toString()
                                )
                            } / ${DayOfWeek.instance.convertDayOfWeekToText(uiState.dayOfWeek)} / ${
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
                        Spacer(GlanceModifier.defaultWeight())
                        Button(
                            text = LocalContext.current.getString(R.string.refresh),
                            onClick = {
                                scope.launch {
                                    viewModel.getTodaySchedule()
                                }
                            },
                            modifier = GlanceModifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = GlanceTheme.colors.surface,
                                contentColor = GlanceTheme.colors.primary
                            )
                        )
                    }
                }

                if (uiState.courses == null) {
                    Text(
                        modifier = GlanceModifier.fillMaxSize().defaultWeight().padding(16.dp).clickable(actionStartActivity(
                            Intent(LocalContext.current, MainActivity::class.java)
                        )),
                        text = LocalContext.current.getString(R.string.unlogin_message),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onBackground,
                            textAlign = TextAlign.Center
                        ),
                    )
                } else if (uiState.courses!!.isEmpty()) {
                    Text(
                        modifier = GlanceModifier.fillMaxSize().defaultWeight().padding(16.dp),
                        text = LocalContext.current.getString(R.string.no_schedule_today),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onBackground,
                            textAlign = TextAlign.Center
                        ),
                    )
                } else {
                    LazyColumn(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(uiState.courses!!) { course ->
                            val startNode = course.bigNodeNo!!
                            Row(
                                modifier = GlanceModifier.fillMaxWidth().padding(8.dp).height(36.dp)
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

                if (uiState.lastUpdateDateTime != null) {
                    Row(GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = LocalContext.current.getString(
                                R.string.last_update_time,
                                uiState.lastUpdateDateTime!!.format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                )
                            ),
                            modifier = GlanceModifier
                                .height(36.dp),
                            style = TextStyle(
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onBackground,
                            )
                        )
                        Spacer(GlanceModifier.defaultWeight())
                    }
                }
            }
        }
    }
}
