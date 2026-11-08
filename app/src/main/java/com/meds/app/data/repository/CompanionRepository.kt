package com.meds.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.meds.app.data.model.CompanionDay
import com.meds.app.data.model.CompanionLink
import com.meds.app.data.model.LinkedPatient
import com.meds.app.data.model.Nudge
import com.meds.app.data.model.PairingInvite
import com.meds.app.data.model.PlantState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.Date
import java.util.Locale

class CompanionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val auth = FirebaseAuth.getInstance()
    private val random = SecureRandom()

    private fun uidOrNull(): String? = auth.currentUser?.uid

    private fun uid(): String = uidOrNull() ?: error("No hay una sesión activa.")

    private fun userRef(uid: String): DocumentReference =
        firestore.collection("users").document(uid)

    fun observeCompanions(patientUid: String): Flow<List<CompanionLink>> {
        if (patientUid.isBlank()) return flowOf(emptyList())
        return queryFlow(userRef(patientUid).collection("companions"))
            .map { snapshot ->
                snapshot.toCompanionLinks().sortedByDescending { it.linkedAt }
            }
    }

    fun observeLinkedPatients(companionUid: String): Flow<List<LinkedPatient>> {
        if (companionUid.isBlank()) return flowOf(emptyList())
        return queryFlow(userRef(companionUid).collection("linkedPatients"))
            .map { snapshot ->
                snapshot.toLinkedPatients().sortedByDescending { it.linkedAt }
            }
    }

    fun observeNudges(patientUid: String): Flow<List<Nudge>> {
        if (patientUid.isBlank()) return flowOf(emptyList())
        return queryFlow(userRef(patientUid).collection("nudges"))
            .map { snapshot ->
                snapshot.toNudges().sortedByDescending { it.createdAt }
            }
    }

    fun observePatientDay(patientUid: String, dateKey: String): Flow<CompanionDay> {
        if (patientUid.isBlank()) return flowOf(CompanionDay())
        val nameFlow = documentFlow(userRef(patientUid))
            .map { it.getString("name").orEmpty() }
        val medicationsFlow = queryFlow(userRef(patientUid).collection("medications"))
            .map { it.toMedications() }
        val schedulesFlow = queryFlow(userRef(patientUid).collection("schedules"))
            .map { it.toSchedules() }
        val dosesFlow = queryFlow(
            userRef(patientUid).collection("doseLogs").whereEqualTo("dateKey", dateKey)
        ).map { it.toDoseLogs().sortedBy { dose -> dose.scheduledAt } }
        val plantFlow = documentFlow(
            userRef(patientUid).collection("plant").document("state")
        ).map { it.toPlantState() }

        return combine(
            nameFlow,
            medicationsFlow,
            schedulesFlow,
            dosesFlow,
            plantFlow
        ) { name, medications, schedules, doses, plant ->
            CompanionDay(
                patientName = name,
                medications = medications.sortedBy { it.name.lowercase() },
                schedules = schedules,
                doses = doses,
                plant = plant
            )
        }
    }

    suspend fun createInvite(
        patientUid: String,
        patientName: String
    ): Result<PairingInvite> = runCatching {
        val now = System.currentTimeMillis()
        val expiresAt = now + INVITE_MINUTES * 60_000L

        repeat(5) {
            val code = generateCode()
            val inviteRef = firestore.collection("pairingInvites").document(code)
            if (!inviteRef.get().await().exists()) {
                inviteRef.set(
                    mapOf(
                        "patientUid" to patientUid,
                        "patientName" to patientName.trim(),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "expiresAt" to Timestamp(Date(expiresAt)),
                        "used" to false
                    )
                ).await()
                return@runCatching PairingInvite(
                    code = code,
                    patientUid = patientUid,
                    patientName = patientName.trim(),
                    createdAt = now,
                    expiresAt = expiresAt,
                    used = false
                )
            }
        }
        error("No se pudo generar un código. Intenta de nuevo.")
    }

    suspend fun linkWithCode(
        rawCode: String,
        companionName: String
    ): Result<String> = runCatching {
        val companionUid = uid()
        val code = rawCode.trim().uppercase(Locale.US)
        require(code.length == 8 && code.all { it.isLetterOrDigit() }) {
            "El código debe tener 8 caracteres."
        }

        val inviteRef = firestore.collection("pairingInvites").document(code)
        val inviteSnapshot = inviteRef.get().await()
        require(inviteSnapshot.exists()) { "El código no existe o ya expiró." }
        val invite = inviteSnapshot.toPairingInvite()
            ?: error("El código no es válido.")
        require(!invite.used) { "Este código ya fue utilizado." }
        require(invite.expiresAt > System.currentTimeMillis()) {
            "Este código ya expiró. Pide uno nuevo."
        }
        require(invite.patientUid.isNotBlank()) { "El código no es válido." }
        require(invite.patientUid != companionUid) {
            "No puedes vincularte a tu propio código."
        }

        val resolvedName = companionName.trim().ifBlank {
            userRef(companionUid).get().await().getString("name").orEmpty()
        }.ifBlank { "Acompañante" }

        val linkedAt = Timestamp.now()
        val batch = firestore.batch()
        batch.set(
            userRef(invite.patientUid).collection("companions").document(companionUid),
            mapOf(
                "name" to resolvedName,
                "linkedAt" to linkedAt,
                "inviteCode" to code
            ),
            SetOptions.merge()
        )
        batch.set(
            userRef(companionUid).collection("linkedPatients").document(invite.patientUid),
            mapOf(
                "name" to invite.patientName.ifBlank { "Paciente" },
                "linkedAt" to linkedAt,
                "inviteCode" to code
            ),
            SetOptions.merge()
        )
        batch.update(inviteRef, "used", true)
        batch.commit().await()
        invite.patientUid
    }

    suspend fun revokeCompanion(
        patientUid: String,
        companionUid: String
    ): Result<Unit> = runCatching {
        userRef(patientUid).collection("companions").document(companionUid)
            .delete()
            .await()
    }

    suspend fun sendNudge(
        patientUid: String,
        fromName: String,
        message: String
    ): Result<Unit> = runCatching {
        val companionUid = uid()
        val cleanMessage = message.trim().take(160)
        require(cleanMessage.isNotBlank()) { "Escribe un mensaje breve." }
        userRef(patientUid).collection("nudges").document().set(
            mapOf(
                "fromUid" to companionUid,
                "fromName" to fromName.trim().ifBlank { "Tu acompañante" },
                "message" to cleanMessage,
                "createdAt" to Timestamp.now(),
                "read" to false
            )
        ).await()
    }

    suspend fun markNudgeRead(
        patientUid: String,
        nudgeId: String
    ): Result<Unit> = runCatching {
        userRef(patientUid).collection("nudges").document(nudgeId)
            .update("read", true)
            .await()
    }

    suspend fun unlinkPatient(
        companionUid: String,
        patientUid: String
    ): Result<Unit> = runCatching {
        val batch = firestore.batch()
        batch.delete(userRef(companionUid).collection("linkedPatients").document(patientUid))
        batch.delete(userRef(patientUid).collection("companions").document(companionUid))
        batch.commit().await()
    }

    private fun generateCode(): String {
        return (1..CODE_LENGTH)
            .map { CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)] }
            .joinToString("")
    }

    private fun queryFlow(query: Query): Flow<QuerySnapshot> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                trySend(snapshot)
            }
        }
        awaitClose { registration.remove() }
    }

    private fun documentFlow(reference: DocumentReference): Flow<DocumentSnapshot> = callbackFlow {
        val registration = reference.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                trySend(snapshot)
            }
        }
        awaitClose { registration.remove() }
    }

    private companion object {
        const val INVITE_MINUTES = 10L
        const val CODE_LENGTH = 8
        const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
