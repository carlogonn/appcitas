package com.appcitas.app.data.remote.services

import com.appcitas.app.data.remote.dto.VerificationDto
import com.appcitas.app.data.remote.dto.VerificationResultDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface VerificationService {

    @GET("verification/status")
    suspend fun getVerificationStatus(): Response<VerificationDto?>

    @Multipart
    @POST("verification/submit")
    suspend fun submitVerification(
        @Part profilePhoto: MultipartBody.Part,
        @Part selfie: MultipartBody.Part,
        @Part livenessVideo: MultipartBody.Part
    ): Response<VerificationResultDto>

    @GET("verification/{verificationId}")
    suspend fun getVerificationStatusById(
        @Path("verificationId") verificationId: String
    ): Response<VerificationDto>
}

data class VerificationDto(
    val id: String,
    val userId: String,
    val status: String,
    val aiScore: Double? = null,
    val rejectionReason: String? = null,
    val createdAt: String,
    val reviewedAt: String? = null
)

data class VerificationResultDto(
    val status: String,
    val faceDetected: Boolean,
    val similarityScore: Double,
    val livenessScore: Double,
    val antiSpoofingScore: Double,
    val rejectionReason: String? = null
)
