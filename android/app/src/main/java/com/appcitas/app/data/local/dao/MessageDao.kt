package com.appcitas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.appcitas.app.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE (sender_id = :userId OR receiver_id = :userId) ORDER BY created_at DESC")
    fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE (sender_id = :user1 AND receiver_id = :user2) OR (sender_id = :user2 AND receiver_id = :user1) ORDER BY created_at ASC")
    fun getMessagesBetweenUsers(user1: String, user2: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE (sender_id = :user1 AND receiver_id = :user2) OR (sender_id = :user2 AND receiver_id = :user1) ORDER BY created_at ASC LIMIT :limit")
    suspend fun getMessagesBetweenUsersLimited(user1: String, user2: String, limit: Int = 50): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE channel_id = :channelId ORDER BY created_at ASC")
    fun getCommunityMessages(channelId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE channel_id = :channelId ORDER BY created_at ASC LIMIT :limit")
    suspend fun getCommunityMessagesLimited(channelId: String, limit: Int = 50): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET is_read = 1 WHERE sender_id = :senderId AND receiver_id = :receiverId AND is_read = 0")
    suspend fun markAsRead(senderId: String, receiverId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE sender_id = :senderId AND receiver_id = :receiverId AND is_read = 0")
    suspend fun getUnreadCount(senderId: String, receiverId: String): Int

    @Query("DELETE FROM messages WHERE sender_id = :senderId AND receiver_id = :receiverId")
    suspend fun deleteMessagesBetweenUsers(senderId: String, receiverId: String)
}
