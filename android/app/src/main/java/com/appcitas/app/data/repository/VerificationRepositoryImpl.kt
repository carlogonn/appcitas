package com.appcitas.app.data.repository

import com.appcitas.app.data.remote.services.VerificationService
import com.appcitas.app.domain.model.Verification
import com.appcitas.app.domain.model.VerificationResult
import com.appcitas.app.domain.model.VerificationStatus
import com.appcitas.app.domain.repository.VerificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationRepositoryImpl @Inject constructor(
    private val verificationService: VerificationService
) : VerificationRepository {

    override suspend fun getVerificationStatus(): Result<Verification?> {
        return try {
            val response = verificationService.getVerificationStatus()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status != "none") {
                    Result.success(
                        Verification(
                            id = body.id,
                            userId = "", // Will be filled by server
                            status = VerificationStatus.valueOf(body.status.uppercase()),
                            aiScore = body.aiScore,
                            rejectionReason = body.rejectionReason,
                            createdAt = body.createdAt,
                            reviewedAt = body.reviewedAt
                        )
                    )
                } else {
                    Result.success(null)
                }
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitVerification(
        profilePhoto: ByteArray,
        selfie: ByteArray,
        livenessVideo: ByteArray?
    ): Result<VerificationResult> {
        return try {
            val profilePhotoPart = createMultipartBody(profilePhoto, "profile_photo.jpg", "profile_photo")
            val selfiePart = createMultipartBody(selfie, "selfie.jpg", "selfie")

            val livenessVideoPart = if (livenessVideo != null) {
                createMultipartBody(livenessVideo, "liveness.mp4", "liveness_video")
            } else {
                null
            }

            val response = verificationService.submitVerification(
                profilePhotoPart,
                selfiePart,
                livenessVideoPart
            )

            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(
                    VerificationResult(
                        status = dto.status,
                        faceDetected = dto.faceDetected,
                        similarityScore = dto.similarityScore,
                        livenessScore = dto.livenessScore,
                        antiSpoofingScore = dto.antiSpoofingScore,
                        rejectionReason = dto.rejectionReason
                    )
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkVerificationStatus(verificationId: String): Result<Verification> {
        return try {
            val response = verificationService.getVerificationStatusById(verificationId)
            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(
                    Verification(
                        id = dto.id,
                        userId = dto.userId,
                        status = VerificationStatus.valueOf(dto.status.uppercase()),
                        aiScore = dto.aiScore,
                        rejectionReason = dto.rejectionReason,
                        createdAt = dto.createdAt,
                        reviewedAt = dto.reviewedAt
                    )
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeVerificationStatus(): Flow<Verification?> = flow {
        // TODO: Implement polling or WebSocket observation
    }

    private fun createMultipartBody(data: ByteArray, fileName: String, fieldName: String): MultipartBody.Part {
        val requestBody = data.toRequestBody("image/*".toMediaType())
        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }
}
