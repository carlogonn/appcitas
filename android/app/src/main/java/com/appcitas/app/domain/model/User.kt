package com.appcitas.app.domain.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("birth_date")
    val birthDate: String,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("profile_photo_url")
    val profilePhotoUrl: String? = null,

    @SerializedName("is_verified")
    val isVerified: Boolean = false,

    @SerializedName("show_distance")
    val showDistance: Boolean = false,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class UserPhoto(
    val id: String,
    val userId: String,
    val photoUrl: String,
    val isPrimary: Boolean = false,
    val orderIndex: Int = 0
)

data class UserLocation(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val lastUpdated: String
)
