package com.meds.app.domain.model

/** Perfil persistido en Firestore para restaurar rol y nombre. */
data class UserProfile(
    val role: String?,
    val name: String?
)
