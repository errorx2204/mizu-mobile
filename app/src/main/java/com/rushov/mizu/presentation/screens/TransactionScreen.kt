package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.data.remote.TransactionResponse
import kotlinx.coroutines.launch

@Composable
fun TransactionScreen(userId: Int = 1) {
    val scope = rememberCoroutineScope()
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

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

    val filteredTransactions = transactions.filter { transaction ->
        val matchesSearch = searchQuery.isBlank() ||
                transaction.title.contains(searchQuery, ignoreCase = true) ||
                transaction.category.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" ||
                (selectedFilter == "Income" && transaction.type == "income") ||
                (selectedFilter == "Expense" && transaction.type == "expense")
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Transactions",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search transactions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Income", "Expense").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (filteredTransactions.isEmpty()) {
            Text(
                text = if (searchQuery.isNotBlank()) "No matching transactions" else "No transactions yet",
                modifier = Modifier.padding(top = 32.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn {
                items(filteredTransactions) { transaction ->
                    TransactionListItem(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(transaction: TransactionResponse) {
    val isIncome = transaction.type == "income"
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63)
    val sign = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.category,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = transaction.created_at.take(10),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Text(
                text = "$sign${String.format("%.2f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}