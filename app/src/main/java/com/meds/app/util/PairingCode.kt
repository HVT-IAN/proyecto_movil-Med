package com.meds.app.util

import android.net.Uri

fun parsePairingCode(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null

    val code = if (trimmed.startsWith("meds://pair", ignoreCase = true)) {
        runCatching { Uri.parse(trimmed).getQueryParameter("code") }.getOrNull()
    } else {
        trimmed.substringBefore('&')
    }

    return code
        ?.trim()
        ?.uppercase()
        ?.takeIf { value -> value.length == 8 && value.all(Char::isLetterOrDigit) }
}
