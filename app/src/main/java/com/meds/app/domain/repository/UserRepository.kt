package com.meds.app.domain.repository

import com.meds.app.domain.model.UserProfile

/** Contrato del perfil del usuario y sus dispositivos. */
interface UserRepository {
    suspend fun saveRole(uid: String, role: String)

    suspend fun saveProfile(uid: String, role: String, name: String)

    suspend fun saveDeviceToken(uid: String, token: String)

    suspend fun deleteDeviceToken(uid: String, token: String)

    suspend fun loadProfile(uid: String): UserProfile?
}
