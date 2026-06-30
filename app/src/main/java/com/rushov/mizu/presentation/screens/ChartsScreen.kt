package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.data.remote.TransactionResponse
import kotlinx.coroutines.launch

@Composable
fun ChartsScreen(userId: Int = 1) {
    val scope = rememberCoroutineScope()
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = userId) {
        scope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.api.getTransactions(userId)
                if (response.isSuccessful) transactions = response.body() ?: emptyList()
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    val expenses = transactions.filter { it.type == "expense" }
    val income = transactions.filter { it.type == "income" }
    val totalExpense = expenses.sumOf { it.amount }
    val totalIncome = income.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    val categorySpending = expenses.groupBy { it.category }
        .mapValues { (_, txs) -> txs.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Analytics",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryCard("Income", totalIncome, Color(0xFF4CAF50), Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard("Expense", totalExpense, Color(0xFFE91E63), Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard("Balance", balance, Color(0xFF2196F3), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Spending by Category",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (categorySpending.isEmpty()) {
                Text(
                    text = "No expense data yet",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                val maxSpending = categorySpending.maxOfOrNull { it.second } ?: 1.0
                val colors = listOf(
                    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF3F51B5),
                    Color(0xFF03A9F4), Color(0xFF4CAF50), Color(0xFFFF9800)
                )

                categorySpending.forEachIndexed { index, (category, amount) ->
                    val percentage = amount / maxSpending
                    val color = colors[index % colors.size]
                    CategoryBar(
                        category = category,
                        amount = amount,
                        percentage = percentage.toFloat(),
                        color = color,
                        total = totalExpense
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}


@Composable
fun CategoryBar(category: String, amount: Double, percentage: Float, color: Color, total: Double) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = category, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "Rs. ${String.format("%.2f", amount)} (${String.format("%.1f", (amount/total)*100)}%)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
            )
        }
    }
}
