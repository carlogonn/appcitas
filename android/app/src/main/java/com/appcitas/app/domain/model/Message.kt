package com.appcitas.app.domain.model

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("id")
    val id: String,

    @SerializedName("sender_id")
    val senderId: String,

    @SerializedName("receiver_id")
    val receiverId: String? = null,

    @SerializedName("channel_id")
    val channelId: String? = null,

    @SerializedName("encrypted_content")
    val encryptedContent: String,

    @SerializedName("content_type")
    val contentType: String = "text",

    @SerializedName("is_read")
    val isRead: Boolean = false,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("sender_username")
    val senderUsername: String? = null,

    @SerializedName("sender_photo_url")
    val senderPhotoUrl: String? = null
)

data class Chat(
    val id: String,
    val otherUser: User,
    val lastMessage: Message? = null,
    val unreadCount: Int = 0
)

data class CommunityChannel(
    val id: String,
    val name: String,
    val description: String? = null
)

data class CommunityMessage(
    val id: String,
    val channelId: String,
    val userId: String,
    val username: String,
    val content: String,
    val distance: Double? = null,
    val createdAt: String
)
