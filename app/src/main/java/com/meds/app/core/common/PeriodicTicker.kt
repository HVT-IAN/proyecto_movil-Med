package com.meds.app.core.common

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/**
 * Flujo que emite cada [intervalMillis] mientras su colección siga activa.
 *
 * Se usa para tareas periódicas de la capa ViewModel sin crear corrutinas
 * sueltas: al cancelarse la recolección, el flujo termina.
 */
fun periodicTicker(
    intervalMillis: Long,
    initialDelayMillis: Long = intervalMillis
): Flow<Unit> {
    require(intervalMillis > 0L) { "El intervalo debe ser mayor que cero." }
    return flow {
        if (initialDelayMillis > 0L) delay(initialDelayMillis)
        while (currentCoroutineContext().isActive) {
            emit(Unit)
            delay(intervalMillis)
        }
    }
}
