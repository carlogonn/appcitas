package com.appcitas.app.data.remote.services

import com.appcitas.app.data.remote.dto.UserDto
import com.appcitas.app.data.remote.dto.UpdateProfileRequest
import com.appcitas.app.data.remote.dto.LocationRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): Response<UserDto>

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserDto>

    @POST("users/me/location")
    suspend fun updateLocation(@Body request: LocationRequest): Response<Unit>

    @GET("users/{id}/location")
    suspend fun getUserLocation(@Path("id") userId: String): Response<LocationDto>

    @Multipart
    @POST("users/me/photos")
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part,
        @Part("is_primary") isPrimary: Boolean = false
    ): Response<PhotoDto>

    @DELETE("users/me/photos/{photoId}")
    suspend fun deletePhoto(@Path("photoId") photoId: String): Response<Unit>

    @PATCH("users/me/photos/reorder")
    suspend fun reorderPhotos(@Body photoIds: List<String>): Response<Unit>

    @GET("users/nearby")
    suspend fun getNearbyUsers(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radiusKm: Double = 50.0
    ): Response<List<UserDto>>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<UserDto>>

    @POST("users/{id}/block")
    suspend fun blockUser(@Path("id") userId: String): Response<Unit>

    @DELETE("users/{id}/block")
    suspend fun unblockUser(@Path("id") userId: String): Response<Unit>

    @GET("users/me/blocked")
    suspend fun getBlockedUsers(): Response<List<UserDto>>

    @POST("users/{id}/report")
    suspend fun reportUser(
        @Path("id") userId: String,
        @Body request: ReportRequest
    ): Response<Unit>
}

data class UpdateProfileRequest(
    val username: String? = null,
    val bio: String? = null,
    val phone: String? = null,
    val showDistance: Boolean? = null
)

data class LocationRequest(
    val latitude: Double,
    val longitude: Double
)

data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val lastUpdated: String
)

data class PhotoDto(
    val id: String,
    val photoUrl: String,
    val isPrimary: Boolean,
    val orderIndex: Int
)

data class ReportRequest(
    val reason: String,
    val description: String? = null
)
