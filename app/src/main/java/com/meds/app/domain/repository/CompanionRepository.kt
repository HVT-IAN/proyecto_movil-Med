package com.meds.app.domain.repository

import com.meds.app.domain.model.CompanionDay
import com.meds.app.domain.model.CompanionLink
import com.meds.app.domain.model.LinkedPatient
import com.meds.app.domain.model.Nudge
import com.meds.app.domain.model.PairingInvite
import kotlinx.coroutines.flow.Flow

/** Contrato de vínculos, códigos de invitación y recordatorios. */
interface CompanionRepository {
    fun observeCompanions(patientUid: String): Flow<List<CompanionLink>>

    fun observeLinkedPatients(companionUid: String): Flow<List<LinkedPatient>>

    fun observeNudges(patientUid: String): Flow<List<Nudge>>

    fun observePatientDay(patientUid: String, dateKey: String): Flow<CompanionDay>

    suspend fun createInvite(
        patientUid: String,
        patientName: String
    ): Result<PairingInvite>

    suspend fun invalidateInvite(code: String): Result<Unit>

    suspend fun linkWithCode(
        rawCode: String,
        companionName: String
    ): Result<String>

    suspend fun revokeCompanion(
        patientUid: String,
        companionUid: String
    ): Result<Unit>

    suspend fun sendNudge(
        patientUid: String,
        fromName: String,
        message: String
    ): Result<Unit>

    suspend fun markNudgeRead(
        patientUid: String,
        nudgeId: String
    ): Result<Unit>

    suspend fun unlinkPatient(
        companionUid: String,
        patientUid: String
    ): Result<Unit>
}
