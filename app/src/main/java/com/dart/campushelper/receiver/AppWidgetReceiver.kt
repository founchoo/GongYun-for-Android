package com.dart.campushelper.receiver

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.dart.campushelper.App
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.alarm.SyncScheduleRepository
import com.dart.campushelper.ui.widget.AppWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject
    lateinit var syncScheduleRepository: SyncScheduleRepository

    private val receiver = ComponentName(context, RebootReceiver::class.java)

    override val glanceAppWidget: GlanceAppWidget
        get() = AppWidget()

    override fun onEnabled(context: Context?) {
        syncScheduleRepository.setAlarm()
        // Enable receiver for reboot broadcast
        App.context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onDisabled(context: Context?) {
        syncScheduleRepository.cancelAlarm()
        // Disable receiver for reboot broadcast
        App.context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_APPS_UPDATED -> {
                val awm = AppWidgetManager.getInstance(context)
                val ids = ComponentName(context.packageName, checkNotNull(javaClass.canonicalName))
                onUpdate(
                    context = context,
                    appWidgetManager = awm,
                    appWidgetIds = awm.getAppWidgetIds(ids)
                )
            }

            else -> super.onReceive(context, intent)
        }
    }

    companion object {

        val ACTION_APPS_UPDATED = "${context.packageName}.action.ACTION_APPS_UPDATED"

        @JvmStatic
        fun updateBroadcast(ctx: Context) {
            ctx.sendBroadcast(
                Intent(ctx, AppWidgetReceiver::class.java).also {
                    it.action = ACTION_APPS_UPDATED
                }
            )
        }
    }
}
