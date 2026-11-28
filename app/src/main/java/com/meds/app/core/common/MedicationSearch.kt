package com.meds.app.core.common

import java.text.Normalizer
import java.util.Locale

/** Normaliza texto para búsquedas sin distinguir mayúsculas ni acentos. */
fun normalizeForSearch(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase(Locale.ROOT)
        .trim()

/** Indica si un medicamento coincide con la consulta por nombre, dosis o forma. */
fun medicationMatchesQuery(
    name: String,
    dose: String,
    form: String,
    query: String
): Boolean {
    val normalizedQuery = normalizeForSearch(query)
    if (normalizedQuery.isBlank()) return true
    return listOf(name, dose, form).any { value ->
        normalizeForSearch(value).contains(normalizedQuery)
    }
}
