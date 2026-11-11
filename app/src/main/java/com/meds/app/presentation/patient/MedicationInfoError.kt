package com.meds.app.presentation.patient

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Traduce errores de la consulta CIMA a mensajes para la pantalla. */
internal fun Throwable.toMedicationInfoMessage(): String {
    return when (this) {
        is IllegalArgumentException -> message ?: "Revisa el texto de búsqueda."
        is HttpException -> when (code()) {
            404 -> "No se encontraron medicamentos con ese nombre."
            429 -> "CIMA recibió demasiadas consultas. Espera un momento."
            in 500..599 -> "El servicio de CIMA no está disponible. Intenta más tarde."
            else -> "No se pudo consultar CIMA. Código ${code()}."
        }

        is SocketTimeoutException ->
            "La consulta tardó demasiado. Revisa tu conexión e intenta de nuevo."

        is JsonParseException ->
            "La respuesta de CIMA no se pudo interpretar. Intenta de nuevo."

        is IOException ->
            "Sin conexión. Revisa tu red e intenta de nuevo."

        else -> "No se pudo consultar la información del medicamento."
    }
}
