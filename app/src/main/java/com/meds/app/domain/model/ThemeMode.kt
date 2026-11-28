package com.meds.app.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromName(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

fun resolveDarkTheme(mode: ThemeMode, systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
