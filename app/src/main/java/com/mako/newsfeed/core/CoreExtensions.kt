package com.mako.newsfeed.core

import io.ktor.http.decodeURLPart
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.toArticleReadableDateTime(): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(this)
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm", Locale.getDefault())
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        this
    }
}

fun String.fixUrl(): String {
    val regex = Regex("""\\+u([0-9a-fA-F]{4})""")
    return regex.replace(this) { matchResult ->
        val hexCode = matchResult.groupValues[1]
        hexCode.toInt(16).toChar().toString()
    }
}