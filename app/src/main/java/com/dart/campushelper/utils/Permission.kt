package com.dart.campushelper.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dart.campushelper.App
import com.dart.campushelper.ui.main.MainActivity

class Permission {

    companion object {
        const val PERMISSION_REQUEST_CODE = 0

        fun requestNotificationPermission(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return if (ContextCompat.checkSelfPermission(
                        App.context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    true
                } else {
                    MainActivity.mainActivity.requestPermissions(
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        PERMISSION_REQUEST_CODE
                    )
                    false
                }
            } else {
                return NotificationManagerCompat.from(App.context).areNotificationsEnabled()
            }
        }
    }
}