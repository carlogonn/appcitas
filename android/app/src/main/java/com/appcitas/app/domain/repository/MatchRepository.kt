package com.appcitas.app.domain.repository

import com.appcitas.app.domain.model.Match
import com.appcitas.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    suspend fun getMatches(): Result<List<Match>>
    suspend fun swipe(userId: String, isLike: Boolean): Result<Boolean>
    suspend fun getDiscoverUsers(page: Int = 1, limit: Int = 20): Result<List<User>>
    suspend fun getMatch(matchId: String): Result<Match>
    fun observeNewMatches(): Flow<Match>
}
