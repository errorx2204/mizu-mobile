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
import androidx.compose.material3.LinearProgressIndicator
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
import com.rushov.mizu.data.remote.BudgetRequest
import com.rushov.mizu.data.remote.BudgetResponse
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.data.remote.TransactionResponse
import com.rushov.mizu.presentation.components.MizuButton
import com.rushov.mizu.presentation.components.MizuTextField
import kotlinx.coroutines.launch

val expenseCategories = listOf(
    "Food" to Color(0xFFFF6B6B),
    "Transport" to Color(0xFF4ECDC4),
    "Shopping" to Color(0xFF45B7D1),
    "Entertainment" to Color(0xFF96CEB4),
    "Bills" to Color(0xFFFFEAA7),
    "Health" to Color(0xFFDDA0DD),
    "Education" to Color(0xFF98D8C8),
    "Other" to Color(0xFF95A5A6)
)

val incomeCategories = listOf(
    "Salary" to Color(0xFF4CAF50),
    "Investment" to Color(0xFF2196F3),
    "Other" to Color(0xFFFF9800)
)

@Composable
fun BudgetScreen(userId: Int = 1) {
    var budgets by remember { mutableStateOf<List<BudgetResponse>>(emptyList()) }
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("expense") }
    var deleteMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val budgetResponse = RetrofitClient.api.getBudgets(userId)
                val transactionResponse = RetrofitClient.api.getTransactions(userId)
                
                if (budgetResponse.isSuccessful) {
                    budgets = budgetResponse.body() ?: emptyList()
                }
                if (transactionResponse.isSuccessful) {
                    transactions = transactionResponse.body() ?: emptyList()
                }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(key1 = true) {
        loadData()
    }

    val expenseBudgets = budgets.filter { it.category in expenseCategories.map { cat -> cat.first } }
    val incomeBudgets = budgets.filter { it.category in incomeCategories.map { cat -> cat.first } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Budget",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BudgetTabButton(
                text = "Expense Budgets",
                isSelected = selectedTab == "expense",
                color = Color(0xFFE91E63)
            ) { selectedTab = "expense" }
            
            BudgetTabButton(
                text = "Income Goals",
                isSelected = selectedTab == "income",
                color = Color(0xFF4CAF50)
            ) { selectedTab = "income" }
        }

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
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (selectedTab == "expense" && expenseBudgets.isEmpty()) {
            Column(
                modifier = Modifier.padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No expense budgets",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to set your first budget",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        } else if (selectedTab == "income" && incomeBudgets.isEmpty()) {
            Column(
                modifier = Modifier.padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No income goals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to set your first goal",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val displayBudgets = if (selectedTab == "expense") expenseBudgets else incomeBudgets
                
                items(
                    items = displayBudgets,
                    key = { it.id }
                ) { budget ->
                    val spent = transactions
                        .filter { it.type == selectedTab && it.category == budget.category }
                        .sumOf { it.amount }
                    
                    SwipeableBudgetCard(
                        budget = budget,
                        spent = spent,
                        isIncome = selectedTab == "income",
                        onDelete = {
                            scope.launch {
                                try {
                                    val response = RetrofitClient.api.deleteBudget(budget.id)
                                    if (response.isSuccessful) {
                                        deleteMessage = "Budget deleted!"
                                        loadData()
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

        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = { loadData() },
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ) {
                Text("R", fontSize = 18.sp, color = Color.White)
            }

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
        AddBudgetDialog(
            userId = userId,
            isIncome = selectedTab == "income",
            onDismiss = { showAddDialog = false },
            onAdded = { loadData() }
        )
    }
}

@Composable
fun SwipeableBudgetCard(
    budget: BudgetResponse,
    spent: Double,
    isIncome: Boolean,
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
        BudgetProgressCard(budget, spent, isIncome)
    }
}

@Composable
fun BudgetTabButton(text: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BudgetProgressCard(budget: BudgetResponse, spent: Double, isIncome: Boolean) {
    val percentage = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = budget.amount - spent
    
    val color = when {
        isIncome -> when {
            percentage >= 1f -> Color(0xFF4CAF50)
            percentage >= 0.8f -> Color(0xFF2196F3)
            else -> Color(0xFFFF9800)
        }
        else -> when {
            percentage >= 1f -> Color(0xFFE91E63)
            percentage >= 0.8f -> Color(0xFFFF9800)
            else -> Color(0xFF4CAF50)
        }
    }

    val categoryColor = expenseCategories.find { it.first == budget.category }?.second 
        ?: incomeCategories.find { it.first == budget.category }?.second
        ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Text(
                        text = " ${budget.category}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "Rs. ${String.format("%.0f", spent)} / Rs. ${String.format("%.0f", budget.amount)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isIncome) {
                    if (remaining <= 0) "Goal reached! Rs. ${String.format("%.0f", kotlin.math.abs(remaining))} extra!" 
                    else "Rs. ${String.format("%.0f", remaining)} to reach goal"
                } else {
                    if (remaining >= 0) "Rs. ${String.format("%.0f", remaining)} remaining" 
                    else "Rs. ${String.format("%.0f", kotlin.math.abs(remaining))} over budget!"
                },
                fontSize = 12.sp,
                color = if (isIncome) {
                    if (remaining <= 0) Color(0xFF4CAF50) else Color(0xFFFF9800)
                } else {
                    if (remaining >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63)
                }
            )
        }
    }
}

@Composable
fun AddBudgetDialog(
    userId: Int,
    isIncome: Boolean,
    onDismiss: () -> Unit,
    onAdded: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val categories = if (isIncome) incomeCategories else expenseCategories

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
                        text = if (isIncome) "Set Income Goal" else "Set Budget",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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
                                text = if (selectedCategory.isEmpty()) "Select Category" else selectedCategory,
                                color = if (selectedCategory.isEmpty()) 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
                                    else MaterialTheme.colorScheme.onSurface
                            )
                            Text("?", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MizuTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = if (isIncome) "Goal Amount (Rs.)" else "Budget Amount (Rs.)"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        MizuButton(
                            text = if (isIncome) "Set Goal" else "Set Budget",
                            onClick = {
                                if (selectedCategory.isNotEmpty() && amount.isNotEmpty()) {
                                    scope.launch {
                                        isLoading = true
                                        try {
                                            val response = RetrofitClient.api.createBudget(
                                                userId = userId,
                                                request = BudgetRequest(
                                                    category = selectedCategory,
                                                    amount = amount.toDoubleOrNull() ?: 0.0
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
                        items(categories) { (cat, color) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategory = cat
                                        showCategoryPicker = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(
                                    text = " $cat",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
