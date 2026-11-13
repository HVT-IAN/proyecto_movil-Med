package com.meds.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.meds.app.BuildConfig
import com.meds.app.domain.model.UserProfile
import com.meds.app.domain.repository.UserRepository
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/** Implementación del perfil y dispositivos en Cloud Firestore. */
class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    override suspend fun saveRole(uid: String, role: String) {
        val reference = firestore.collection("users").document(uid)
        val exists = reference.get().await().exists()
        val data = mutableMapOf<String, Any>("role" to role)
        if (!exists) {
            data["createdAt"] = FieldValue.serverTimestamp()
        }
        reference.set(data, SetOptions.merge()).await()
    }

    override suspend fun saveProfile(uid: String, role: String, name: String) {
        val reference = firestore.collection("users").document(uid)
        val exists = reference.get().await().exists()
        val data = mutableMapOf<String, Any>(
            "role" to role,
            "name" to name
        )
        if (!exists) {
            data["createdAt"] = FieldValue.serverTimestamp()
        }
        reference.set(data, SetOptions.merge()).await()
    }

    override suspend fun saveDeviceToken(uid: String, token: String) {
        if (uid.isBlank() || token.isBlank()) return
        firestore.collection("users").document(uid)
            .collection("devices").document(token)
            .set(
                mapOf(
                    "token" to token,
                    "platform" to "android",
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    override suspend fun registerDeviceToken(uid: String): Boolean {
        if (uid.isBlank() || BuildConfig.USE_FIREBASE_EMULATOR) return false
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return false
        return runCatching { saveDeviceToken(uid, token) }.isSuccess
    }

    override suspend fun deleteDeviceToken(uid: String, token: String) {
        if (uid.isBlank() || token.isBlank()) return
        firestore.collection("users").document(uid)
            .collection("devices").document(token)
            .delete()
            .await()
    }

    override suspend fun unregisterCurrentDevice(uid: String) {
        if (uid.isBlank()) return
        runCatching {
            withTimeoutOrNull(3_000L) {
                val token = FirebaseMessaging.getInstance().token.await()
                deleteDeviceToken(uid, token)
            }
        }
    }

    override suspend fun loadProfile(uid: String): UserProfile? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        if (!snapshot.exists()) return null
        return UserProfile(
            role = snapshot.getString("role"),
            name = snapshot.getString("name")
        )
    }
}
