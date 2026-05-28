package com.ravia.app.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Date.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - time
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Hace un momento"
        diff < TimeUnit.HOURS.toMillis(1) -> "Hace ${TimeUnit.MILLISECONDS.toMinutes(diff)} min"
        diff < TimeUnit.DAYS.toMillis(1) -> "Hace ${TimeUnit.MILLISECONDS.toHours(diff)} h"
        diff < TimeUnit.DAYS.toMillis(7) -> "Hace ${TimeUnit.MILLISECONDS.toDays(diff)} días"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX")).format(this)
    }
}

fun Date.toFormattedDate(): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "MX")).format(this)

fun Date.toShortDate(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX")).format(this)

fun Double.toKmString(): String = when {
    this < 1.0 -> "${(this * 1000).toInt()} m"
    else -> String.format("%.1f km", this)
}

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPassword(): Boolean = length >= 6

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } }
