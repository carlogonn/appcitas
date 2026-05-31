package com.appcitas.app.domain.model

import com.google.gson.annotations.SerializedName

data class Match(
    @SerializedName("id")
    val id: String,

    @SerializedName("user1_id")
    val user1Id: String,

    @SerializedName("user2_id")
    val user2Id: String,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("created_at")
    val createdAt: String
)

data class Swipe(
    val swiperId: String,
    val swipedId: String,
    val isLike: Boolean
)

data class Block(
    val id: String,
    val blockerId: String,
    val blockedId: String,
    val createdAt: String
)

data class Report(
    val id: String,
    val reporterId: String,
    val reportedId: String,
    val reason: ReportReason,
    val description: String? = null,
    val status: String = "pending",
    val createdAt: String
)

enum class ReportReason {
    @SerializedName("spam")
    SPAM,

    @SerializedName("inappropriate")
    INAPPROPRIATE,

    @SerializedName("harassment")
    HARASSMENT,

    @SerializedName("fake_profile")
    FAKE_PROFILE
}
