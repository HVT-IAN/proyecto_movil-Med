package com.meds.app.data.repository

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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
        runAuthCall("No se pudo iniciar sesión") {
            val user = auth.signInWithEmailAndPassword(email.trim(), password).await().user
                ?: error("Firebase no devolvió un usuario.")
            user.toAuthUser()
        }

    override suspend fun register(email: String, password: String): Result<AuthUser> =
        runAuthCall("No se pudo crear la cuenta") {
            val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
                ?: error("Firebase no devolvió un usuario.")
            user.toAuthUser()
        }

    override suspend fun continueAnonymously(): Result<AuthUser> =
        runAuthCall("No se pudo iniciar sesión anónima") {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
                ?: error("Firebase no devolvió un usuario.")
            user.toAuthUser()
        }

    override suspend fun linkAnonymousToEmail(
        email: String,
        password: String
    ): Result<AuthUser> = runAuthCall(
        logMessage = "No se pudo vincular la cuenta anónima",
        collisionMessage = "Ese correo ya está en uso. Prueba con otro para no perder los datos de esta sesión."
    ) {
        val user = auth.currentUser ?: error("No hay una sesión activa.")
        check(user.isAnonymous) { "La cuenta ya tiene un correo vinculado." }
        val credential = EmailAuthProvider.getCredential(email.trim(), password)
        val linked = user.linkWithCredential(credential).await().user
            ?: error("Firebase no devolvió un usuario.")
        linked.toAuthUser()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runAuthCall("No se pudo enviar el correo de recuperación") {
            auth.sendPasswordResetEmail(email.trim()).await()
            Unit
        }

    override fun signOut(): Result<Unit> = runCatching { auth.signOut() }

    private inline fun <T> runAuthCall(
        logMessage: String,
        collisionMessage: String? = null,
        block: () -> T
    ): Result<T> = runCatching(block).fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            Log.e("AuthRepository", logMessage, throwable)
            val message = if (collisionMessage != null && throwable is FirebaseAuthUserCollisionException) {
                collisionMessage
            } else {
                throwable.toAuthErrorMessage()
            }
            Result.failure(IllegalStateException(message, throwable))
        }
    )

    private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
        uid = uid,
        email = email,
        isAnonymous = isAnonymous
    )
}

private fun Throwable.toAuthErrorMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException ->
        "La contraseña es demasiado débil. Usa al menos 6 caracteres."
    is FirebaseAuthInvalidCredentialsException ->
        "Correo o contraseña incorrectos."
    is FirebaseAuthInvalidUserException ->
        "No encontramos una cuenta con este correo."
    is FirebaseAuthUserCollisionException ->
        "Ese correo ya está registrado. Inicia sesión con él."
    is FirebaseAuthRecentLoginRequiredException ->
        "Por seguridad, vuelve a iniciar sesión e inténtalo de nuevo."
    is FirebaseNetworkException ->
        "No pudimos conectar. Revisa tu conexión a internet."
    is FirebaseTooManyRequestsException ->
        "Demasiados intentos. Espera un momento y vuelve a intentarlo."
    is FirebaseAuthException ->
        if (errorCode == "ERROR_OPERATION_NOT_ALLOWED") {
            "Este método de inicio de sesión no está habilitado. Revisa la configuración de Firebase."
        } else {
            message ?: "No pudimos completar el inicio de sesión."
        }
    else -> message ?: "Ocurrió un error inesperado."
}
