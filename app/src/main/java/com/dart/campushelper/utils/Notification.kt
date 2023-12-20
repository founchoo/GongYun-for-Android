package com.dart.campushelper.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dart.campushelper.App.Companion.context
import com.dart.campushelper.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Notification @Inject constructor() {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val CHANNEL_ID = "host_channel"
        private const val LESSON_REMINDER_NOTIFICATION_ID = 0
    }

    fun show(title: String, content: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setWhen(System.currentTimeMillis())
            // Set visibility in lock screen
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(LESSON_REMINDER_NOTIFICATION_ID, builder.build())
            }
        }
    }

    fun createNotificationChannel() {
        val name = context.getString(R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun deleteNotificationChannel() {
        notificationManager.deleteNotificationChannel(CHANNEL_ID)
    }
}