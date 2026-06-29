package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.window.Dialog
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.data.remote.TransactionRequest
import com.rushov.mizu.data.remote.TransactionResponse
import com.rushov.mizu.presentation.components.MizuButton
import com.rushov.mizu.presentation.components.MizuTextField
import kotlinx.coroutines.launch

val categories = listOf(
    "Food" to "F",
    "Transport" to "T",
    "Shopping" to "S",
    "Entertainment" to "E",
    "Bills" to "B",
    "Health" to "H",
    "Education" to "Ed",
    "Salary" to "Sa",
    "Investment" to "I",
    "Other" to "O"
)

val categoryColors = mapOf(
    "Food" to Color(0xFFFF6B6B),
    "Transport" to Color(0xFF4ECDC4),
    "Shopping" to Color(0xFF45B7D1),
    "Entertainment" to Color(0xFF96CEB4),
    "Bills" to Color(0xFFFFEAA7),
    "Health" to Color(0xFFDDA0DD),
    "Education" to Color(0xFF98D8C8),
    "Salary" to Color(0xFF4CAF50),
    "Investment" to Color(0xFF2196F3),
    "Other" to Color(0xFF95A5A6)
)

val titles = mapOf(
    "Food" to listOf("Lunch", "Dinner", "Groceries", "Snacks", "Coffee"),
    "Transport" to listOf("Uber", "Bus", "Train", "Fuel", "Parking"),
    "Shopping" to listOf("Clothes", "Electronics", "Household", "Gifts"),
    "Entertainment" to listOf("Movies", "Games", "Music", "Events"),
    "Bills" to listOf("Electricity", "Water", "Internet", "Phone", "Rent"),
    "Health" to listOf("Doctor", "Medicine", "Gym", "Insurance"),
    "Education" to listOf("Books", "Course", "Tuition", "Stationery"),
    "Salary" to listOf("Monthly Salary", "Bonus", "Freelance"),
    "Investment" to listOf("Stocks", "Crypto", "Savings", "FD"),
    "Other" to listOf("Miscellaneous", "Donation", "Loan Payment")
)

@Composable
fun TransactionsScreen(userId: Int = 1) {
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun loadTransactions() {
        scope.launch {
            isLoading = true
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

    LaunchedEffect(key1 = true) {
        loadTransactions()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Transactions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            SummaryCard(transactions)

            Spacer(modifier = Modifier.height(16.dp))

            if (deleteMessage.isNotEmpty()) {
                Text(
                    text = deleteMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LaunchedEffect(deleteMessage) {
                    kotlinx.coroutines.delay(2000)
                    deleteMessage = ""
                }
            }

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (transactions.isEmpty()) {
                Text(
                    text = "No transactions yet",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = transactions,
                        key = { it.id }
                    ) { transaction ->
                        SwipeableTransactionItem(
                            transaction = transaction,
                            onDelete = {
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.api.deleteTransaction(transaction.id)
                                        if (response.isSuccessful) {
                                            deleteMessage = "Transaction deleted!"
                                            loadTransactions()
                                        }
                                    } catch (e: Exception) {
                                        deleteMessage = "Failed to delete"
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Refresh Button
            FloatingActionButton(
                onClick = { loadTransactions() },
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ) {
                Text("R", fontSize = 18.sp, color = Color.White)
            }

            // Add Button
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            userId = userId,
            onDismiss = { showAddDialog = false },
            onAdded = {
                loadTransactions()
            }
        )
    }
}

@Composable
fun SwipeableTransactionItem(
    transaction: TransactionResponse,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE91E63))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Delete",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        TransactionItem(transaction)
    }
}

@Composable
fun SummaryCard(transactions: List<TransactionResponse>) {
    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
    val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
    val balance = income - expense

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem("Income", income, Color(0xFF4CAF50))
            SummaryItem("Expense", expense, Color(0xFFE91E63))
            SummaryItem("Balance", balance, if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63))
        }
    }
}

@Composable
fun SummaryItem(title: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "?${String.format("%.0f", amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TransactionItem(transaction: TransactionResponse) {
    val isIncome = transaction.type == "income"
    val color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63)
    val sign = if (isIncome) "+" else "-"
    val categoryColor = categoryColors[transaction.category] ?: MaterialTheme.colorScheme.primary
    val title = transaction.title.takeIf { it.isNotBlank() } ?: "Untitled"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = categories.find { it.first == transaction.category }?.second ?: "O",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatDate(transaction.created_at),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
            Text(
                text = "$sign?${String.format("%.0f", transaction.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString.substring(0, 19))
        outputFormat.format(date ?: return "Today")
    } catch (e: Exception) {
        "Today"
    }
}

@Composable
fun AddTransactionDialog(
    userId: Int,
    onDismiss: () -> Unit,
    onAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showTitlePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Add Transaction",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TypeButton("Expense", type == "expense", Color(0xFFE91E63)) { type = "expense" }
                        TypeButton("Income", type == "income", Color(0xFF4CAF50)) { type = "income" }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { showCategoryPicker = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (category.isEmpty()) "Select Category" else "$category ${categories.find { it.first == category }?.second ?: ""}",
                                color = if (category.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                            )
                            Text("?", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (category.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { showTitlePicker = true }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (title.isEmpty()) "Select Title" else title,
                                    color = if (title.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                )
                                Text("?", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    MizuTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = "Amount (?)"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        MizuButton(
                            text = "Add Transaction",
                            onClick = {
                                if (title.isNotEmpty() && amount.isNotEmpty() && category.isNotEmpty()) {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            val response = RetrofitClient.api.createTransaction(
                                                userId = userId,
                                                request = TransactionRequest(
                                                    title = title,
                                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                                    category = category,
                                                    type = type
                                                )
                                            )
                                            if (response.isSuccessful) {
                                                onAdded()
                                                onDismiss()
                                            }
                                        } catch (e: Exception) {
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    MizuButton(text = "Cancel", onClick = onDismiss)
                }
            }
        }
    }

    if (showCategoryPicker) {
        Dialog(onDismissRequest = { showCategoryPicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyColumn {
                        items(categories) { (cat, emoji) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        category = cat
                                        title = ""
                                        showCategoryPicker = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = cat, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTitlePicker && category.isNotEmpty()) {
        Dialog(onDismissRequest = { showTitlePicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Title",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyColumn {
                        items(titles[category] ?: listOf("Other")) { itemTitle ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        title = itemTitle
                                        showTitlePicker = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = itemTitle, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypeButton(text: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}





