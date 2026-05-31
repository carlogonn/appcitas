package com.appcitas.app.data.remote.services

import com.appcitas.app.data.remote.dto.MatchDto
import com.appcitas.app.data.remote.dto.UserDto
import com.appcitas.app.data.remote.dto.SwipeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MatchService {

    @GET("matches")
    suspend fun getMatches(): Response<List<MatchDto>>

    @GET("matches/{matchId}")
    suspend fun getMatch(@Path("matchId") matchId: String): Response<MatchDto>

    @POST("matches/swipe")
    suspend fun swipe(@Body request: SwipeRequest): Response<SwipeResponse>

    @GET("discover")
    suspend fun getDiscoverUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<UserDto>>
}

data class SwipeRequest(
    val targetUserId: String,
    val isLike: Boolean
)

data class SwipeResponse(
    val isMatch: Boolean,
    val matchId: String? = null
)

data class MatchDto(
    val id: String,
    val user1Id: String,
    val user2Id: String,
    val user: UserDto? = null,
    val createdAt: String
)
