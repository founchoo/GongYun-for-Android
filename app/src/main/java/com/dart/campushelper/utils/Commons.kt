package com.dart.campushelper.utils

import android.content.Intent
import android.net.Uri
import com.dart.campushelper.App

fun visitWebsite(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.data = Uri.parse(url)
    App.context.startActivity(intent)
}