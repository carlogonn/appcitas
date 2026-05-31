package com.appcitas.app.data.repository

import com.appcitas.app.data.local.dao.UserDao
import com.appcitas.app.data.remote.services.UserService
import com.appcitas.app.domain.model.User
import com.appcitas.app.domain.model.UserLocation
import com.appcitas.app.domain.model.UserPhoto
import com.appcitas.app.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userService: UserService,
    private val userDao: UserDao
) : UserRepository {

    override suspend fun getUser(id: String): Result<User> {
        return try {
            val response = userService.getUserById(id)
            if (response.isSuccessful) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = userService.getCurrentUser()
            if (response.isSuccessful) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(user: User): Result<User> {
        return try {
            val response = userService.updateProfile(
                com.appcitas.app.data.remote.services.UpdateProfileRequest(
                    username = user.username,
                    bio = user.bio,
                    phone = user.phone,
                    showDistance = user.showDistance
                )
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLocation(location: UserLocation): Result<Unit> {
        return try {
            val response = userService.updateLocation(
                com.appcitas.app.data.remote.services.LocationRequest(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLocation(userId: String): Result<UserLocation> {
        return try {
            val response = userService.getUserLocation(userId)
            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(
                    UserLocation(
                        userId = userId,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        lastUpdated = dto.lastUpdated
                    )
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadPhoto(photo: ByteArray, isPrimary: Boolean): Result<UserPhoto> {
        // TODO: Implement photo upload with multipart
        return Result.failure(Exception("Not implemented yet"))
    }

    override suspend fun deletePhoto(photoId: String): Result<Unit> {
        return try {
            val response = userService.deletePhoto(photoId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reorderPhotos(photoIds: List<String>): Result<Unit> {
        return try {
            val response = userService.reorderPhotos(photoIds)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNearbyUsers(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<User>> {
        return try {
            val response = userService.getNearbyUsers(latitude, longitude, radiusKm)
            if (response.isSuccessful) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val response = userService.searchUsers(query)
            if (response.isSuccessful) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun blockUser(userId: String): Result<Unit> {
        return try {
            val response = userService.blockUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unblockUser(userId: String): Result<Unit> {
        return try {
            val response = userService.unblockUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBlockedUsers(): Result<List<User>> {
        return try {
            val response = userService.getBlockedUsers()
            if (response.isSuccessful) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reportUser(
        userId: String,
        reason: String,
        description: String?
    ): Result<Unit> {
        return try {
            val response = userService.reportUser(
                userId,
                com.appcitas.app.data.remote.services.ReportRequest(reason, description)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.appcitas.app.data.remote.dto.UserDto.toDomain(): User {
        return User(
            id = id,
            email = email,
            username = username,
            phone = phone,
            birthDate = birthDate,
            gender = gender,
            bio = bio,
            profilePhotoUrl = profilePhotoUrl,
            isVerified = isVerified,
            showDistance = showDistance,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
