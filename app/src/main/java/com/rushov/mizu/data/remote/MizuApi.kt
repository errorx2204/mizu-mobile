package com.rushov.mizu.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

data class TransactionRequest(
    val title: String,
    val amount: Double,
    val category: String,
    val type: String
)

data class TransactionResponse(
    val id: Int,
    val user_id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val created_at: String
)

interface MizuApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("transactions/")
    suspend fun createTransaction(
        @Query("user_id") userId: Int,
        @Body request: TransactionRequest
    ): Response<TransactionResponse>

    @GET("transactions/")
    suspend fun getTransactions(
        @Query("user_id") userId: Int
    ): Response<List<TransactionResponse>>

    @DELETE("transactions/{transaction_id}")
    suspend fun deleteTransaction(transactionId: Int): Response<Unit>
}
