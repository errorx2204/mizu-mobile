package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.data.remote.TransactionResponse
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(userId: Int = 1) {
    val scope = rememberCoroutineScope()
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
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

    LaunchedEffect(key1 = userId) {
        loadData()
    }

    val expenses = transactions.filter { it.type == "expense" }
    val income = transactions.filter { it.type == "income" }
    val totalExpense = expenses.sumOf { it.amount }
    val totalIncome = income.sumOf { it.amount }
    val balance = totalIncome - totalExpense
    val recentTransactions = transactions.take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dashboard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            IconButton(onClick = { loadData() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Balance",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rs. ${String.format("%.2f", balance)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryCard("Income", totalIncome, Color(0xFF4CAF50), Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                SummaryCard("Expense", totalExpense, Color(0xFFE91E63), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recent Transactions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (recentTransactions.isEmpty()) {
                Text(
                    text = "No transactions yet",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                LazyColumn {
                    items(recentTransactions) { transaction ->
                        TransactionListItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rs. ${String.format("%.2f", amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
