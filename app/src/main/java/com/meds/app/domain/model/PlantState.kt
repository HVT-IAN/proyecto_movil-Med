package com.meds.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PlantState(
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    val freezeAvailable: Int = 1,
    val lastTakenDate: String = "",
    val totalTaken: Int = 0
) {
    val stage: Int
        get() = level.coerceIn(1, MAX_LEVEL)

    val isMaxLevel: Boolean
        get() = stage >= MAX_LEVEL

    val stageXp: Int
        get() = if (isMaxLevel) XP_PER_LEVEL else xp % XP_PER_LEVEL

    val stageProgress: Float
        get() = if (isMaxLevel) 1f else stageXp / XP_PER_LEVEL.toFloat()

    /**
     * Calcula el nuevo estado de la planta tras registrar una toma en [today].
     * Aplica la regla de racha diaria, consumo de escudo por 1 día de ausencia
     * y subida de etapa cada [XP_PER_LEVEL] puntos.
     */
    fun registerDoseTaken(today: LocalDate): PlantState {
        val lastDate = runCatching { LocalDate.parse(lastTakenDate) }.getOrNull()
        val gapDays = lastDate?.let { ChronoUnit.DAYS.between(it, today).toInt() }
        var newFreeze = freezeAvailable
        val newStreak = when {
            gapDays == null -> 1
            gapDays <= 0 -> streak.coerceAtLeast(1)
            gapDays == 1 -> streak + 1
            gapDays == 2 && freezeAvailable > 0 -> {
                newFreeze = freezeAvailable - 1
                streak.coerceAtLeast(1) + 1
            }
            else -> 1
        }
        val newXp = xp + XP_PER_DOSE
        val newLevel = ((newXp / XP_PER_LEVEL) + 1).coerceAtMost(MAX_LEVEL)

        return copy(
            level = newLevel,
            xp = newXp,
            streak = newStreak,
            freezeAvailable = newFreeze,
            lastTakenDate = today.toString(),
            totalTaken = totalTaken + 1
        )
    }

    companion object {
        const val XP_PER_DOSE = 10
        const val XP_PER_LEVEL = 50
        const val MAX_LEVEL = 5
    }
}
