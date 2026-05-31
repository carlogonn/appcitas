package com.appcitas.app.domain.repository

import com.appcitas.app.domain.model.Chat
import com.appcitas.app.domain.model.CommunityChannel
import com.appcitas.app.domain.model.CommunityMessage
import com.appcitas.app.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    // 1:1 Chat
    suspend fun getChats(): Result<List<Chat>>
    suspend fun getMessages(chatId: String, limit: Int = 50): Result<List<Message>>
    suspend fun sendMessage(receiverId: String, content: String, contentType: String = "text"): Result<Message>
    suspend fun markAsRead(chatId: String): Result<Unit>
    fun observeMessages(chatId: String): Flow<Message>
    fun observeNewChats(): Flow<Chat>

    // Community Chat
    suspend fun getCommunityChannels(): Result<List<CommunityChannel>>
    suspend fun getCommunityMessages(channelId: String, limit: Int = 50): Result<List<CommunityMessage>>
    suspend fun sendCommunityMessage(channelId: String, content: String): Result<CommunityMessage>
    fun observeCommunityMessages(channelId: String): Flow<CommunityMessage>

    // Encryption
    suspend fun initEncryption(): Result<Unit>
    suspend fun getPublicKey(): Result<String>
}
