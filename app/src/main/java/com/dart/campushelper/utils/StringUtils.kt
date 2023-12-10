package com.dart.campushelper.utils

import com.dart.campushelper.CampusHelperApplication.Companion.context
import com.dart.campushelper.R
import org.jsoup.Jsoup

// 解析HTML
fun parseHtml(html: String): String {
    val htmlDoc = Jsoup.parse(html)
    return htmlDoc.text()
}

fun String.replaceWithStars(isScreenshotMode: Boolean): String {
    return if (isScreenshotMode) {
        this.map { if (it.isDigit()) '*' else it }.joinToString("")
    } else {
        this
    }
}
