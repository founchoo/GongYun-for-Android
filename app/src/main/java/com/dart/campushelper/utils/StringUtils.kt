package com.dart.campushelper.utils

import org.jsoup.Jsoup

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

// 解析HTML
fun parseHtml(html: String): String {
    val htmlDoc = Jsoup.parse(html)
    return htmlDoc.text()
}