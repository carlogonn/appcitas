package com.appcitas.app.domain.repository

import com.appcitas.app.domain.model.Verification
import com.appcitas.app.domain.model.VerificationResult
import kotlinx.coroutines.flow.Flow

interface VerificationRepository {
    suspend fun getVerificationStatus(): Result<Verification?>
    suspend fun submitVerification(
        profilePhoto: ByteArray,
        selfie: ByteArray,
        livenessVideo: ByteArray
    ): Result<VerificationResult>
    suspend fun checkVerificationStatus(verificationId: String): Result<Verification>
    fun observeVerificationStatus(): Flow<Verification?>
}
