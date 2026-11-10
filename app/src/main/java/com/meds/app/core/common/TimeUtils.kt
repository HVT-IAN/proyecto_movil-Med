package com.meds.app.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val spanishLocale = Locale.forLanguageTag("es")

fun todayKey(): String = LocalDate.now().toString()

fun formatTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return "--:--"
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun formatDateKey(dateKey: String): String {
    if (dateKey.isBlank()) return ""
    return runCatching {
        LocalDate.parse(dateKey)
            .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", spanishLocale))
            .replaceFirstChar { character ->
                if (character.isLowerCase()) character.titlecase(spanishLocale) else character.toString()
            }
    }.getOrDefault(dateKey)
}

fun relativeTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val difference = epochMillis - System.currentTimeMillis()
    val minutes = (difference / 60_000L).toInt()
    return when {
        abs(minutes) <= 1 -> "ahora"
        minutes in 2..59 -> "en $minutes min"
        minutes in -59..-2 -> "hace ${-minutes} min"
        minutes in 60..1_439 -> {
            val hours = minutes / 60
            val rest = minutes % 60
            if (rest == 0) "en $hours h" else "en $hours h $rest min"
        }
        minutes <= -60 && minutes > -1_440 -> {
            val hours = (-minutes) / 60
            val rest = (-minutes) % 60
            if (rest == 0) "hace $hours h" else "hace $hours h $rest min"
        }
        else -> formatTime(epochMillis)
    }
}

fun formatTimeOfDay(time: String): String {
    return runCatching {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: return time
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        String.format(Locale.US, "%02d:%02d", hour, minute)
    }.getOrDefault(time)
}
