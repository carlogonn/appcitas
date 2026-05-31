package com.appcitas.app.data.repository

import com.appcitas.app.data.local.dao.UserDao
import com.appcitas.app.data.local.entity.UserEntity
import com.appcitas.app.data.remote.dto.AuthResponse
import com.appcitas.app.data.remote.dto.LoginRequest
import com.appcitas.app.data.remote.dto.RegisterRequest
import com.appcitas.app.data.remote.services.AuthService
import com.appcitas.app.domain.model.User
import com.appcitas.app.domain.repository.AuthRepository
import com.appcitas.app.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val userDao: UserDao,
    private val securityUtils: SecurityUtils
) : AuthRepository {

    override suspend fun register(
        email: String,
        username: String,
        password: String,
        birthDate: String,
        gender: String?
    ): Result<User> {
        return try {
            val response = authService.register(
                RegisterRequest(email, username, password, birthDate, gender)
            )

            if (response.isSuccessful) {
                val authResponse = response.body()!!
                handleAuthResponse(authResponse)
                Result.success(authResponse.user.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authService.login(LoginRequest(email, password))

            if (response.isSuccessful) {
                val authResponse = response.body()!!
                handleAuthResponse(authResponse)
                Result.success(authResponse.user.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            securityUtils.clearAll()
            userDao.deleteCurrentUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            val response = authService.forgotPassword(
                com.appcitas.app.data.remote.dto.ForgotPasswordRequest(email)
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

    override suspend fun refreshToken(): Result<String> {
        return try {
            val refreshToken = securityUtils.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token"))

            val response = authService.refreshToken(refreshToken)
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                securityUtils.saveAccessToken(authResponse.accessToken)
                securityUtils.saveRefreshToken(authResponse.refreshToken)
                Result.success(authResponse.accessToken)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> = flow {
        emit(securityUtils.isLoggedIn())
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            // Try to get from local database first
            val userId = securityUtils.getUserId()
                ?: return Result.failure(Exception("No user logged in"))

            val localUser = userDao.getUserById(userId)
            if (localUser != null) {
                Result.success(
                    User(
                        id = localUser.id,
                        email = localUser.email,
                        username = localUser.username,
                        phone = localUser.phone,
                        birthDate = localUser.birthDate,
                        gender = localUser.gender,
                        bio = localUser.bio,
                        profilePhotoUrl = localUser.profilePhotoUrl,
                        isVerified = localUser.isVerified,
                        showDistance = localUser.showDistance,
                        createdAt = localUser.createdAt,
                        updatedAt = localUser.updatedAt
                    )
                )
            } else {
                // Fetch from API if not in local DB
                val response = authService.getCurrentUser()
                if (response.isSuccessful) {
                    val userDto = response.body()!!
                    // Save to local DB
                    val userEntity = UserEntity(
                        id = userDto.id,
                        email = userDto.email,
                        username = userDto.username,
                        phone = userDto.phone,
                        birthDate = userDto.birthDate,
                        gender = userDto.gender,
                        bio = userDto.bio,
                        profilePhotoUrl = userDto.profilePhotoUrl,
                        isVerified = userDto.isVerified,
                        showDistance = userDto.showDistance,
                        createdAt = userDto.createdAt,
                        updatedAt = userDto.updatedAt,
                        isCurrentUser = true
                    )
                    userDao.insertUser(userEntity)
                    Result.success(userDto.toDomain())
                } else {
                    Result.failure(Exception("Failed to fetch user"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return securityUtils.getUserId()
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        securityUtils.saveAccessToken(accessToken)
        securityUtils.saveRefreshToken(refreshToken)
    }

    override suspend fun clearTokens() {
        securityUtils.clearAll()
    }

    private suspend fun handleAuthResponse(authResponse: AuthResponse) {
        securityUtils.saveAccessToken(authResponse.accessToken)
        securityUtils.saveRefreshToken(authResponse.refreshToken)
        securityUtils.saveUserId(authResponse.user.id)

        val userEntity = UserEntity(
            id = authResponse.user.id,
            email = authResponse.user.email,
            username = authResponse.user.username,
            phone = authResponse.user.phone,
            birthDate = authResponse.user.birthDate,
            gender = authResponse.user.gender,
            bio = authResponse.user.bio,
            profilePhotoUrl = authResponse.user.profilePhotoUrl,
            isVerified = authResponse.user.isVerified,
            showDistance = authResponse.user.showDistance,
            createdAt = authResponse.user.createdAt,
            updatedAt = authResponse.user.updatedAt,
            isCurrentUser = true
        )
        userDao.insertUser(userEntity)
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
