package com.appcitas.app.data.remote.services

import com.appcitas.app.data.remote.dto.AuthResponse
import com.appcitas.app.data.remote.dto.LoginRequest
import com.appcitas.app.data.remote.dto.RegisterRequest
import com.appcitas.app.data.remote.dto.ForgotPasswordRequest
import com.appcitas.app.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): Response<AuthResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>
}
