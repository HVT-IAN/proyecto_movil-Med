package com.meds.app.data.repository

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    val isAnonymous: Boolean
        get() = auth.currentUser?.isAnonymous == true

    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Firebase no devolvió un usuario.")
    }.onFailure { throwable ->
        Log.e("AuthRepository", "No se pudo iniciar sesión", throwable)
    }

    suspend fun register(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("Firebase no devolvió un usuario.")
    }.onFailure { throwable ->
        Log.e("AuthRepository", "No se pudo crear la cuenta", throwable)
    }

    suspend fun continueAnonymously(): Result<FirebaseUser> = runCatching {
        auth.currentUser ?: auth.signInAnonymously().await().user
            ?: error("Firebase no devolvió un usuario.")
    }.onFailure { throwable ->
        Log.e("AuthRepository", "No se pudo iniciar sesión anónima", throwable)
    }

    suspend fun linkAnonymousToEmail(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            val user = auth.currentUser ?: error("No hay una sesión activa.")
            check(user.isAnonymous) { "La cuenta ya tiene un correo vinculado." }
            val credential = EmailAuthProvider.getCredential(email.trim(), password)
            user.linkWithCredential(credential).await().user
                ?: error("Firebase no devolvió un usuario.")
        }.onFailure { throwable ->
            Log.e("AuthRepository", "No se pudo vincular la cuenta anónima", throwable)
        }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.onFailure { throwable ->
        Log.e("AuthRepository", "No se pudo enviar el correo de recuperación", throwable)
    }

    fun signOut(): Result<Unit> = runCatching { auth.signOut() }
}
