package com.meds.app.domain.model

/** Identidad mínima de la sesión, sin depender de tipos de Firebase. */
data class AuthUser(
    val uid: String,
    val email: String?,
    val isAnonymous: Boolean
)
