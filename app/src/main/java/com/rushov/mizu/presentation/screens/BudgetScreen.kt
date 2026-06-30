package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.rushov.mizu.data.remote.BudgetResponse
import com.rushov.mizu.data.remote.TransactionResponse
import com.rushov.mizu.data.remote.BudgetRequest
import kotlinx.coroutines.launch

@Composable
fun BudgetScreen(userId: Int = 1) {
    val scope = rememberCoroutineScope()
    var budgets by remember { mutableStateOf<List<BudgetResponse>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    val spendingByCategory = remember(transactions) {
        transactions
            .filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
    }

    LaunchedEffect(key1 = userId) {
        scope.launch {
            try {
                isLoading = true
                val budgetResponse = RetrofitClient.api.getBudgets(userId)
                val transactionResponse = RetrofitClient.api.getTransactions(userId)
                if (budgetResponse.isSuccessful) budgets = budgetResponse.body() ?: emptyList()
                if (transactionResponse.isSuccessful) transactions = transactionResponse.body() ?: emptyList()
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

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
                text = "Budgets",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Button(onClick = { showAddDialog = true }) {
                Text("+ Add")
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (budgets.isEmpty()) {
            Text(
                text = "No budgets set. Add one to start tracking!",
                modifier = Modifier.padding(top = 32.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn {
                items(budgets) { budget ->
                    val spent = spendingByCategory[budget.category] ?: 0.0
                    val percentage = if (budget.amount > 0) (spent / budget.amount) else 0.0
                    BudgetCard(
                        category = budget.category,
                        budgetAmount = budget.amount,
                        spent = spent,
                        percentage = percentage
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            userId = userId,
            onDismiss = { showAddDialog = false },
            onBudgetAdded = {
                showAddDialog = false
                scope.launch {
                    val response = RetrofitClient.api.getBudgets(userId)
                    if (response.isSuccessful) budgets = response.body() ?: emptyList()
                }
            }
        )
    }
}

@Composable
fun BudgetCard(category: String, budgetAmount: Double, spent: Double, percentage: Double) {
    val progressColor = when {
        percentage >= 1.0 -> Color(0xFFE91E63)
        percentage >= 0.8 -> Color(0xFFFF9800)
        percentage >= 0.5 -> Color(0xFF4CAF50)
        else -> Color(0xFF2196F3)
    }

    val statusText = when {
        percentage >= 1.0 -> "Over Budget!"
        percentage >= 0.8 -> "Warning"
        percentage >= 0.5 -> "On Track"
        else -> "Good"
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = category, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = statusText, fontSize = 12.sp, color = progressColor, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rs. ${String.format("%.2f", spent)} / Rs. ${String.format("%.2f", budgetAmount)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(minOf(percentage.toFloat(), 1.0f))
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(progressColor)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${String.format("%.1f", percentage * 100)}% used",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun AddBudgetDialog(userId: Int, onDismiss: () -> Unit, onBudgetAdded: () -> Unit) {
    val scope = rememberCoroutineScope()
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget") },
        text = {
            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true
                )
                if (error.isNotEmpty()) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (category.isBlank() || amount.isBlank()) {
                        error = "Fill all fields"
                        return@Button
                    }
                    val amt = amount.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        error = "Invalid amount"
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.api.createBudget(
                                userId,
                                BudgetRequest(category, amt)
                            )
                            if (response.isSuccessful) {
                                onBudgetAdded()
                            } else {
                                error = "Failed to add budget"
                            }
                        } catch (e: Exception) {
                            error = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Adding..." else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
