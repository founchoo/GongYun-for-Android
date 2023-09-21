package com.dart.campushelper.widget

import android.content.Context
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.dart.campushelper.receiver.ScheduleGlanceWidgetReceiver
import com.dart.campushelper.receiver.ScheduleRefreshCallback
import com.dart.campushelper.ui.Root.Companion.scheduleViewModel

class ScheduleGlanceWidget : GlanceAppWidget() {
    @Composable
    @Preview
    fun Content() {

        val prefs = currentState<Preferences>()
        val currentWeek = prefs[ScheduleGlanceWidgetReceiver.currentWeek]
        val dayOfWeek = prefs[ScheduleGlanceWidgetReceiver.dayOfWeek]

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
                        text = currentWeek.toString(),
                        modifier = GlanceModifier
                            .padding(8.dp),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GlanceTheme.colors.primary
                        )
                    )
                    Button(text = "刷新", onClick = actionRunCallback<ScheduleRefreshCallback>())
                }

                LazyColumn() {
                    if (dayOfWeek != null) {
                        itemsIndexed(
                            items = scheduleViewModel.courses.value.filter {
                                it.key.first == dayOfWeek.toInt() && it.value.weekNoList.contains(currentWeek?.toInt())
                            }.values.toList()
                        ) { index, course ->
                            val startNode = index * 2 + 1
                            Row(modifier = GlanceModifier.fillMaxWidth().padding(8.dp)) {
                                Text(
                                    "$startNode-${startNode + 1}",
                                    modifier = GlanceModifier.defaultWeight().padding(
                                        vertical = 2.dp
                                    ),
                                    style = TextStyle(textAlign = TextAlign.Start)
                                )
                                Text(
                                    course.courseName ?: "<空闲>",
                                    modifier = GlanceModifier.defaultWeight().padding(
                                        vertical = 2.dp
                                    ),
                                    style = TextStyle(textAlign = TextAlign.Center)
                                )
                                Text(
                                    course.classroomName ?: "",
                                    modifier = GlanceModifier.defaultWeight().padding(
                                        vertical = 2.dp
                                    ),
                                    style = TextStyle(textAlign = TextAlign.End)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        TODO("Not yet implemented")
    }
}
