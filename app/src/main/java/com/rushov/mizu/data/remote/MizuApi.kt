package com.rushov.mizu.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
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
    val token_type: String,
    val user_id: Int,
    val name: String,
    val email: String
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

data class BudgetRequest(
    val category: String,
    val amount: Double
)

data class BudgetResponse(
    val id: Int,
    val user_id: Int,
    val category: String,
    val amount: Double
)

data class InsightItem(
    val type: String,
    val title: String,
    val message: String,
    val category: String,
    val severity: String
)

data class InsightSummary(
    val total_spent: Double,
    val total_budget: Double,
    val categories_tracked: Int,
    val insights_count: Int
)

data class InsightsResponse(
    val insights: List<InsightItem>,
    val summary: InsightSummary
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
    suspend fun deleteTransaction(
        @Path("transaction_id") transactionId: Int
    ): Response<Unit>

    @POST("budgets/")
    suspend fun createBudget(
        @Query("user_id") userId: Int,
        @Body request: BudgetRequest
    ): Response<BudgetResponse>

    @GET("budgets/")
    suspend fun getBudgets(
        @Query("user_id") userId: Int
    ): Response<List<BudgetResponse>>

    @PUT("budgets/{budget_id}")
    suspend fun updateBudget(
        @Path("budget_id") budgetId: Int,
        @Body request: BudgetRequest
    ): Response<BudgetResponse>

    @DELETE("budgets/{budget_id}")
    suspend fun deleteBudget(
        @Path("budget_id") budgetId: Int
    ): Response<Unit>

    @GET("insights/{user_id}")
    suspend fun getInsights(
        @Path("user_id") userId: Int
    ): Response<InsightsResponse>
}
