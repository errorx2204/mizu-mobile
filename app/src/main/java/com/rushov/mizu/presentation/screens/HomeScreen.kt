package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.data.remote.TransactionResponse
import com.rushov.mizu.presentation.components.ExpensePieChart
import com.rushov.mizu.presentation.components.MonthlyBarChart
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(userId: Int = 1) {
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        scope.launch {
            try {
                val response = RetrofitClient.api.getTransactions(userId)
                if (response.isSuccessful) {
                    transactions = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val balance = income - expense

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Balance Card
        BalanceCard(balance, income, expense)

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats
        QuickStatsRow(transactions)

        Spacer(modifier = Modifier.height(16.dp))

        // Charts
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            // Expense Pie Chart
            ExpensePieChart(transactions)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Monthly Bar Chart
            MonthlyBarChart(transactions)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recent Transactions
            RecentTransactionsSection(transactions)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BalanceCard(balance: Double, income: Double, expense: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Balance",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Text(
                text = "?${String.format("%.0f", balance)}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IncomeExpenseItem("Income", income, Color(0xFF4CAF50), "?")
                IncomeExpenseItem("Expense", expense, Color(0xFFE91E63), "?")
            }
        }
    }
}

@Composable
fun IncomeExpenseItem(label: String, amount: Double, color: Color, arrow: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = arrow,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = "?${String.format("%.0f", amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun QuickStatsRow(transactions: List<TransactionResponse>) {
    val totalTransactions = transactions.size
    val expenseCount = transactions.filter { it.type == "expense" }.size
    val incomeCount = transactions.filter { it.type == "income" }.size
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Transactions", totalTransactions.toString(), MaterialTheme.colorScheme.primary)
        StatItem("Expenses", expenseCount.toString(), Color(0xFFE91E63))
        StatItem("Income", incomeCount.toString(), Color(0xFF4CAF50))
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.width(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<TransactionResponse>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "Recent Transactions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (transactions.isEmpty()) {
            Text(
                text = "No transactions yet",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            transactions.take(5).forEach { transaction ->
                RecentTransactionItem(transaction)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RecentTransactionItem(transaction: TransactionResponse) {
    val isIncome = transaction.type == "income"
    val color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63)
    val sign = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transaction.category,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "$sign?${String.format("%.0f", transaction.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
