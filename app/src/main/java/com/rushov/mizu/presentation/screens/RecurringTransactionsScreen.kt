package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class RecurringTransaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val frequency: String,
    val nextDate: String
)

@Composable
fun RecurringTransactionsScreen(userId: Int = 1) {
    val scope = rememberCoroutineScope()
    
    var recurringList by remember { mutableStateOf<List<RecurringTransaction>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        recurringList = listOf(
            RecurringTransaction(1, "Rent", 15000.0, "Housing", "expense", "monthly", "2025-07-01"),
            RecurringTransaction(2, "Salary", 50000.0, "Income", "income", "monthly", "2025-07-01"),
            RecurringTransaction(3, "Netflix", 199.0, "Entertainment", "expense", "monthly", "2025-07-15"),
            RecurringTransaction(4, "Gym", 1200.0, "Health", "expense", "monthly", "2025-07-05"),
        )
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
                text = "Recurring Transactions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring", tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (recurringList.isEmpty()) {
            Text(
                text = "No recurring transactions set up",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 32.dp)
            )
        } else {
            LazyColumn {
                items(recurringList) { item ->
                    RecurringItemCard(item) {
                        recurringList = recurringList.filter { it.id != item.id }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddRecurringDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newItem ->
                recurringList = recurringList + newItem
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RecurringItemCard(item: RecurringTransaction, onDelete: () -> Unit) {
    val isIncome = item.type == "income"
    val amountColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63)
    val sign = if (isIncome) "+" else "-"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
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
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.category + " - " + item.frequency,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Next: " + item.nextDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = sign + String.format("%.2f", item.amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE91E63))
            }
        }
    }
}

@Composable
fun AddRecurringDialog(onDismiss: () -> Unit, onAdd: (RecurringTransaction) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var frequency by remember { mutableStateOf("monthly") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring Transaction") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                Spacer(modifier = Modifier.height(8.dp))
                Text("Frequency:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row {
                    listOf("daily", "weekly", "monthly", "yearly").forEach { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { frequency = freq },
                            label = { Text(freq) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                Row {
                    FilterChip(
                        selected = type == "expense",
                        onClick = { type = "expense" },
                        label = { Text("Expense") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = type == "income",
                        onClick = { type = "income" },
                        label = { Text("Income") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                onAdd(RecurringTransaction(
                    id = System.currentTimeMillis().toInt(),
                    title = title,
                    amount = amt,
                    category = category,
                    type = type,
                    frequency = frequency,
                    nextDate = "2025-07-01"
                ))
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
