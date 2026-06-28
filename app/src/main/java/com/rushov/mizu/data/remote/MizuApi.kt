package com.rushov.mizu.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val email: String,
    val name: String,
    val id: Int,
    val created_at: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)

interface MizuApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>
}
