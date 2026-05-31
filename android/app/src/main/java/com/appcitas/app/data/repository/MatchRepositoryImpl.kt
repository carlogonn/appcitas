package com.appcitas.app.data.repository

import com.appcitas.app.data.remote.services.MatchService
import com.appcitas.app.data.remote.services.SwipeRequest
import com.appcitas.app.domain.model.Match
import com.appcitas.app.domain.model.User
import com.appcitas.app.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchService: MatchService
) : MatchRepository {

    override suspend fun getMatches(): Result<List<Match>> {
        return try {
            val response = matchService.getMatches()
            if (response.isSuccessful) {
                Result.success(
                    response.body()!!.map { dto ->
                        Match(
                            id = dto.id,
                            user1Id = dto.user1Id,
                            user2Id = dto.user2Id,
                            user = dto.user?.toDomain(),
                            createdAt = dto.createdAt
                        )
                    }
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun swipe(userId: String, isLike: Boolean): Result<Boolean> {
        return try {
            val response = matchService.swipe(SwipeRequest(userId, isLike))
            if (response.isSuccessful) {
                Result.success(response.body()!!.isMatch)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiscoverUsers(page: Int, limit: Int): Result<List<User>> {
        return try {
            val response = matchService.getDiscoverUsers(page, limit)
            if (response.isSuccessful) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMatch(matchId: String): Result<Match> {
        return try {
            val response = matchService.getMatch(matchId)
            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(
                    Match(
                        id = dto.id,
                        user1Id = dto.user1Id,
                        user2Id = dto.user2Id,
                        user = dto.user?.toDomain(),
                        createdAt = dto.createdAt
                    )
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeNewMatches(): Flow<Match> = flow {
        // TODO: Implement WebSocket observation
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
