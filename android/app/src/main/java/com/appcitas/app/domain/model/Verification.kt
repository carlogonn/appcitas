package com.appcitas.app.domain.model

import com.google.gson.annotations.SerializedName

data class Verification(
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("status")
    val status: VerificationStatus,

    @SerializedName("ai_score")
    val aiScore: Double? = null,

    @SerializedName("rejection_reason")
    val rejectionReason: String? = null,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("reviewed_at")
    val reviewedAt: String? = null
)

enum class VerificationStatus {
    @SerializedName("pending")
    PENDING,

    @SerializedName("approved")
    APPROVED,

    @SerializedName("rejected")
    REJECTED
}

data class VerificationResult(
    @SerializedName("status")
    val status: String,

    @SerializedName("face_detected")
    val faceDetected: Boolean,

    @SerializedName("similarity_score")
    val similarityScore: Double,

    @SerializedName("liveness_score")
    val livenessScore: Double,

    @SerializedName("anti_spoofing_score")
    val antiSpoofingScore: Double,

    @SerializedName("rejection_reason")
    val rejectionReason: String? = null
)

data class IcebreakerTopic(
    val id: String,
    val topicText: String,
    val category: String,
    val isActive: Boolean = true
)
