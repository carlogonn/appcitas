package com.appcitas.app.domain.repository

import com.appcitas.app.domain.model.User
import com.appcitas.app.domain.model.UserLocation
import com.appcitas.app.domain.model.UserPhoto
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUser(id: String): Result<User>
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun updateLocation(location: UserLocation): Result<Unit>
    suspend fun getLocation(userId: String): Result<UserLocation>
    suspend fun uploadPhoto(photo: ByteArray, isPrimary: Boolean): Result<UserPhoto>
    suspend fun deletePhoto(photoId: String): Result<Unit>
    suspend fun reorderPhotos(photoIds: List<String>): Result<Unit>
    suspend fun getNearbyUsers(latitude: Double, longitude: Double, radiusKm: Double): Result<List<User>>
    suspend fun searchUsers(query: String): Result<List<User>>
    suspend fun blockUser(userId: String): Result<Unit>
    suspend fun unblockUser(userId: String): Result<Unit>
    suspend fun getBlockedUsers(): Result<List<User>>
    suspend fun reportUser(userId: String, reason: String, description: String?): Result<Unit>
}
