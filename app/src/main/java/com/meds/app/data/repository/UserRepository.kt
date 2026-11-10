package com.meds.app.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val role: String?,
    val name: String?
)

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun saveRole(uid: String, role: String) {
        val reference = firestore.collection("users").document(uid)
        val exists = reference.get().await().exists()
        val data = mutableMapOf<String, Any>("role" to role)
        if (!exists) {
            data["createdAt"] = FieldValue.serverTimestamp()
        }
        reference.set(data, SetOptions.merge()).await()
    }

    suspend fun saveProfile(uid: String, role: String, name: String) {
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

    suspend fun saveDeviceToken(uid: String, token: String) {
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

    suspend fun deleteDeviceToken(uid: String, token: String) {
        if (uid.isBlank() || token.isBlank()) return
        firestore.collection("users").document(uid)
            .collection("devices").document(token)
            .delete()
            .await()
    }

    suspend fun loadProfile(uid: String): UserProfile? {
        val snapshot = firestore.collection("users").document(uid).get().await()
        if (!snapshot.exists()) return null
        return UserProfile(
            role = snapshot.getString("role"),
            name = snapshot.getString("name")
        )
    }
}
