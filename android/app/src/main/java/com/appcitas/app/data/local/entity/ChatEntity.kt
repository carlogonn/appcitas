package com.appcitas.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "other_user_id")
    val otherUserId: String,

    @ColumnInfo(name = "other_username")
    val otherUsername: String,

    @ColumnInfo(name = "other_photo_url")
    val otherPhotoUrl: String? = null,

    @ColumnInfo(name = "last_message")
    val lastMessage: String? = null,

    @ColumnInfo(name = "last_message_at")
    val lastMessageAt: String? = null,

    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String
)
