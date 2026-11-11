package com.meds.app.data.repository

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.meds.app.domain.model.AuthUser
import com.meds.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Implementación de autenticación con Firebase Auth. */
class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val currentUserId: String?
        get() = auth.currentUser?.uid

    override fun authStateFlow(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAuthUser()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<AuthUser> =
        runCatching {
            val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
                ?: error("Firebase no devolvió un usuario.")
            user.toAuthUser()
        }.onFailure { throwable ->
            Log.e("AuthRepository", "No se pudo iniciar sesión", throwable)
        }

    override suspend fun register(email: String, password: String): Result<AuthUser> =
        runCatching {
            val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
                ?: error("Firebase no devolvió un usuario.")
            user.toAuthUser()
        }.onFailure { throwable ->
            Log.e("AuthRepository", "No se pudo crear la cuenta", throwable)
        }

    override suspend fun continueAnonymously(): Result<AuthUser> = runCatching {
        val user = auth.currentUser ?: auth.signInAnonymously().await().user
            ?: error("Firebase no devolvió un usuario.")
        user.toAuthUser()
    }.onFailure { throwable ->
        Log.e("AuthRepository", "No se pudo iniciar sesión anónima", throwable)
    }

    override suspend fun linkAnonymousToEmail(
        email: String,
        password: String
    ): Result<AuthUser> = runCatching {
        val user = auth.currentUser ?: error("No hay una sesión activa.")
        check(user.isAnonymous) { "La cuenta ya tiene un correo vinculado." }
        val credential = EmailAuthProvider.getCredential(email.trim(), password)
        val linked = user.linkWithCredential(credential).await().user
            ?: error("Firebase no devolvió un usuario.")
        linked.toAuthUser()
    }.onFailure { throwable ->
        Log.e("AuthRepository", "No se pudo vincular la cuenta anónima", throwable)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.onFailure { throwable ->
        Log.e("AuthRepository", "No se pudo enviar el correo de recuperación", throwable)
    }

    override fun signOut(): Result<Unit> = runCatching { auth.signOut() }

    private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
        uid = uid,
        email = email,
        isAnonymous = isAnonymous
    )
}
