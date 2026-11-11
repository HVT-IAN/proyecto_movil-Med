package com.meds.app.domain.repository

import com.meds.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/** Contrato de autenticación usado por la capa de presentación. */
interface AuthRepository {
    val currentUserId: String?

    fun authStateFlow(): Flow<AuthUser?>

    suspend fun login(email: String, password: String): Result<AuthUser>

    suspend fun register(email: String, password: String): Result<AuthUser>

    suspend fun continueAnonymously(): Result<AuthUser>

    suspend fun linkAnonymousToEmail(email: String, password: String): Result<AuthUser>

    suspend fun sendPasswordReset(email: String): Result<Unit>

    fun signOut(): Result<Unit>
}
