package com.appcitas.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,

    val email: String,

    val username: String,

    val phone: String? = null,

    @ColumnInfo(name = "birth_date")
    val birthDate: String,

    val gender: String? = null,

    val bio: String? = null,

    @ColumnInfo(name = "profile_photo_url")
    val profilePhotoUrl: String? = null,

    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,

    @ColumnInfo(name = "show_distance")
    val showDistance: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: String,

    @ColumnInfo(name = "is_current_user")
    val isCurrentUser: Boolean = false
)
