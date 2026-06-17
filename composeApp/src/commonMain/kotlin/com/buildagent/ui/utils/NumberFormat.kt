package com.buildagent.ui.utils

import kotlin.math.abs
import kotlin.math.round

fun Double.fmt2dp(): String {
    val rounded = round(this * 100.0) / 100.0
    val whole = rounded.toLong()
    val frac = abs(round((rounded - whole) * 100)).toLong().toString().padStart(2, '0')
    return "$whole.$frac"
}

fun Double.fmt1dp(): String {
    val rounded = round(this * 10.0) / 10.0
    val whole = rounded.toLong()
    val frac = abs(round((rounded - whole) * 10)).toLong().toString()
    return "$whole.$frac"
}

fun Double.fmt0dp(): String = round(this).toLong().toString()

fun formatBytes(bytes: Long?): String {
    if (bytes == null) return "—"
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${(bytes / 1024.0).fmt1dp()} KB"
    return "${(bytes / (1024.0 * 1024)).fmt1dp()} MB"
}
