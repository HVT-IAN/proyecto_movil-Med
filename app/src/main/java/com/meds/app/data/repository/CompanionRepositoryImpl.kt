package com.meds.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.meds.app.core.common.AccessDeniedException
import com.meds.app.data.remote.firestore.asFlow
import com.meds.app.domain.model.CompanionDay
import com.meds.app.domain.model.CompanionLink
import com.meds.app.domain.model.LinkedPatient
import com.meds.app.domain.model.Nudge
import com.meds.app.domain.model.PairingInvite
import com.meds.app.domain.repository.CompanionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.Date
import java.util.Locale

class CompanionRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : CompanionRepository {
    private val random = SecureRandom()

    private fun uidOrNull(): String? = auth.currentUser?.uid

    private fun uid(): String = uidOrNull() ?: error("No hay una sesión activa.")

    private fun userRef(uid: String): DocumentReference =
        firestore.collection("users").document(uid)

    override fun observeCompanions(patientUid: String): Flow<List<CompanionLink>> {
        if (patientUid.isBlank()) return flowOf(emptyList())
        return userRef(patientUid).collection("companions").asFlow()
            .map { snapshot ->
                snapshot.toCompanionLinks().sortedByDescending { it.linkedAt }
            }
    }

    override fun observeLinkedPatients(companionUid: String): Flow<List<LinkedPatient>> {
        if (companionUid.isBlank()) return flowOf(emptyList())
        return userRef(companionUid).collection("linkedPatients").asFlow()
            .map { snapshot ->
                snapshot.toLinkedPatients().sortedByDescending { it.linkedAt }
            }
    }

    override fun observeNudges(patientUid: String): Flow<List<Nudge>> {
        if (patientUid.isBlank()) return flowOf(emptyList())
        return userRef(patientUid).collection("nudges").asFlow()
            .map { snapshot ->
                snapshot.toNudges().sortedByDescending { it.createdAt }
            }
    }

    override fun observePatientDay(patientUid: String, dateKey: String): Flow<CompanionDay> {
        if (patientUid.isBlank()) return flowOf(CompanionDay())
        val nameFlow = userRef(patientUid).asFlow()
            .map { it.getString("name").orEmpty() }
        val medicationsFlow = userRef(patientUid).collection("medications").asFlow()
            .map { it.toMedications() }
        val schedulesFlow = userRef(patientUid).collection("schedules").asFlow()
            .map { it.toSchedules() }
        val dosesFlow = userRef(patientUid).collection("doseLogs")
            .whereEqualTo("dateKey", dateKey)
            .asFlow()
            .map { it.toDoseLogs().sortedBy { dose -> dose.scheduledAt } }
        val plantFlow = userRef(patientUid).collection("plant").document("state")
            .asFlow()
            .map { it.toPlantState() }

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
            .retryWhen { cause, attempt ->
                // La escritura local del vínculo puede emitirse antes de que
                // Firestore confirme el lote y aplique las reglas.
                if (cause.isPermissionDenied() && attempt < PERMISSION_RETRIES) {
                    delay(700L * (attempt + 1))
                    true
                } else {
                    false
                }
            }
            .catch { throwable -> throw throwable.toAccessError() }
    }

    override suspend fun createInvite(
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

    override suspend fun invalidateInvite(code: String): Result<Unit> = runCatching {
        if (code.isBlank()) return@runCatching
        val inviteRef = firestore.collection("pairingInvites").document(code)
        val snapshot = inviteRef.get().await()
        if (snapshot.exists() && snapshot.getBoolean("used") != true) {
            inviteRef.update("used", true).await()
        }
    }

    override suspend fun linkWithCode(
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

    override suspend fun revokeCompanion(
        patientUid: String,
        companionUid: String
    ): Result<Unit> = runCatching {
        userRef(patientUid).collection("companions").document(companionUid)
            .delete()
            .await()
    }

    override suspend fun sendNudge(
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

    override suspend fun markNudgeRead(
        patientUid: String,
        nudgeId: String
    ): Result<Unit> = runCatching {
        userRef(patientUid).collection("nudges").document(nudgeId)
            .update("read", true)
            .await()
    }

    override suspend fun unlinkPatient(
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

    private fun Throwable.isPermissionDenied(): Boolean =
        (this as? FirebaseFirestoreException)?.code ==
            FirebaseFirestoreException.Code.PERMISSION_DENIED

    private fun Throwable.toAccessError(): Throwable =
        if (isPermissionDenied()) {
            AccessDeniedException("El acceso a este paciente fue retirado.", this)
        } else {
            this
        }

    private companion object {
        const val INVITE_MINUTES = 10L
        const val CODE_LENGTH = 8
        const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        const val PERMISSION_RETRIES = 3L
    }
}
