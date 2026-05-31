package com.appcitas.app.data.remote.services

import com.appcitas.app.data.remote.dto.ChatDto
import com.appcitas.app.data.remote.dto.MessageDto
import com.appcitas.app.data.remote.dto.CommunityChannelDto
import com.appcitas.app.data.remote.dto.CommunityMessageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {

    @GET("chat")
    suspend fun getChats(): Response<List<ChatDto>>

    @GET("chat/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<List<MessageDto>>

    @POST("chat/{receiverId}/messages")
    suspend fun sendMessage(
        @Path("receiverId") receiverId: String,
        @Body request: SendMessageRequest
    ): Response<MessageDto>

    @POST("chat/{chatId}/read")
    suspend fun markAsRead(@Path("chatId") chatId: String): Response<Unit>

    @GET("chat/community/channels")
    suspend fun getCommunityChannels(): Response<List<CommunityChannelDto>>

    @GET("chat/community/{channelId}/messages")
    suspend fun getCommunityMessages(
        @Path("channelId") channelId: String,
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<List<CommunityMessageDto>>

    @POST("chat/community/{channelId}/messages")
    suspend fun sendCommunityMessage(
        @Path("channelId") channelId: String,
        @Body request: SendMessageRequest
    ): Response<CommunityMessageDto>

    @POST("chat/encryption/init")
    suspend fun initEncryption(@Body publicKey: String): Response<Unit>

    @GET("chat/encryption/public-key/{userId}")
    suspend fun getPublicKey(@Path("userId") userId: String): Response<String>
}

data class SendMessageRequest(
    val content: String,
    val contentType: String = "text",
    val encryptedContent: String? = null
)

data class ChatDto(
    val id: String,
    val otherUser: ChatUserDto,
    val lastMessage: MessageDto? = null,
    val unreadCount: Int = 0,
    val createdAt: String,
    val updatedAt: String
)

data class ChatUserDto(
    val id: String,
    val username: String,
    val profilePhotoUrl: String? = null,
    val isVerified: Boolean = false
)

data class MessageDto(
    val id: String,
    val senderId: String,
    val receiverId: String? = null,
    val channelId: String? = null,
    val encryptedContent: String,
    val contentType: String = "text",
    val isRead: Boolean = false,
    val createdAt: String,
    val senderUsername: String? = null,
    val senderPhotoUrl: String? = null
)

data class CommunityChannelDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val memberCount: Int = 0
)

data class CommunityMessageDto(
    val id: String,
    val channelId: String,
    val userId: String,
    val username: String,
    val content: String,
    val distance: Double? = null,
    val createdAt: String,
    val senderPhotoUrl: String? = null
)
