package com.meds.app.core.common

/**
 * Error de permisos de una fuente de datos remota.
 *
 * La capa de datos traduce las excepciones del proveedor a este tipo para que
 * la presentación no dependa de Firebase ni de Retrofit.
 */
class AccessDeniedException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
