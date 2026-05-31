package com.appcitas.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.appcitas.app.data.local.dao.UserDao
import com.appcitas.app.data.local.dao.MessageDao
import com.appcitas.app.data.local.dao.ChatDao
import com.appcitas.app.data.local.entity.UserEntity
import com.appcitas.app.data.local.entity.MessageEntity
import com.appcitas.app.data.local.entity.ChatEntity

@Database(
    entities = [
        UserEntity::class,
        MessageEntity::class,
        ChatEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
}
