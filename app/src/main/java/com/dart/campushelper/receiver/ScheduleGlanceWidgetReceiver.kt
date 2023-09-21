package com.dart.campushelper.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.dart.campushelper.widget.ScheduleGlanceWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class ScheduleGlanceWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = ScheduleGlanceWidget()

    private val coroutineScope = MainScope()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.e("TAG", "正在更新")

        super.onUpdate(context, appWidgetManager, appWidgetIds)
        observeData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ScheduleRefreshCallback.UPDATE_ACTION) {
            observeData(context)
        }
    }

    private fun observeData(context: Context) {
        coroutineScope.launch {
            val glanceId =
                GlanceAppWidgetManager(context).getGlanceIds(ScheduleGlanceWidget::class.java).firstOrNull()

            glanceId?.let {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                    pref.toMutablePreferences().apply {
                        this[dayOfWeek] = (
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                LocalDate.now().dayOfWeek.value
                            } else {
                                1
                            }
                            ).toString()
                        //this[currentWeek] = "第 ${scheduleViewModel.week.value} 周"
                    }
                }
                glanceAppWidget.update(context, it)
            }
        }
    }

    companion object {
        val currentWeek = stringPreferencesKey("selected_week")
        val dayOfWeek = stringPreferencesKey("day_of_week")
    }
}

class ScheduleRefreshCallback : ActionCallback {

    companion object {
        const val UPDATE_ACTION = "updateAction"
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, ScheduleGlanceWidgetReceiver::class.java).apply {
            action = UPDATE_ACTION
        }
        context.sendBroadcast(intent)
    }
}
