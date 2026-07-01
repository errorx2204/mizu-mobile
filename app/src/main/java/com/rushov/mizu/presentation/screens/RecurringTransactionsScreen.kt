package com.rushov.mizu.presentation.screens
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.local.DataStoreManager
import com.rushov.mizu.data.local.RecurringTransactionData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun RecurringTransactionsScreen(userId: Int = 1) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()
    
    var recurringList by remember { mutableStateOf<List<RecurringTransactionData>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load from DataStore on first launch
    LaunchedEffect(key1 = true) {
        isLoading = true
        val savedJson = dataStore.recurringTransactions.first()
        if (savedJson.isNotBlank()) {
            recurringList = RecurringTransactionData.listFromJson(savedJson)
        } else {
            // First time - add demo data
            val demoData = listOf(
                RecurringTransactionData(1, "Rent", 15000.0, "Housing", "expense", "monthly", "2025-07-01"),
                RecurringTransactionData(2, "Salary", 50000.0, "Income", "income", "monthly", "2025-07-01"),
                RecurringTransactionData(3, "Netflix", 199.0, "Entertainment", "expense", "monthly", "2025-07-15"),
                RecurringTransactionData(4, "Gym", 1200.0, "Health", "expense", "monthly", "2025-07-05"),
            )
            recurringList = demoData
            dataStore.saveRecurringTransactions(RecurringTransactionData.listToJson(demoData))
        }
        isLoading = false
    }
    
    // Save to DataStore whenever list changes (except initial load)
    var isInitialLoad by remember { mutableStateOf(true) }
    LaunchedEffect(recurringList) {
        if (!isInitialLoad && !isLoading) {
            dataStore.saveRecurringTransactions(RecurringTransactionData.listToJson(recurringList))
        }
        isInitialLoad = false
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
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (recurringList.isEmpty()) {
            Text(
                text = "No recurring transactions set up",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 32.dp)
            )
        } else {
            LazyColumn {
                items(recurringList, key = { it.id }) { item ->
                    RecurringItemCard(
                        item = item,
                        onDelete = {
                            recurringList = recurringList.filter { it.id != item.id }
                        }
                    )
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
fun RecurringItemCard(
    item: RecurringTransactionData,
    onDelete: () -> Unit
) {
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
fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onAdd: (RecurringTransactionData) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var frequency by remember { mutableStateOf("monthly") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTitleDropdown by remember { mutableStateOf(false) }
    
    val categories = com.rushov.mizu.presentation.utils.CategoriesHelper.getCategories(type)
    val titles = categories[category] ?: emptyList()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring Transaction") },
        text = {
            Column {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "expense",
                        onClick = {
                            type = "expense"
                            category = ""
                            title = ""
                        },
                        label = { Text("Expense") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "income",
                        onClick = {
                            type = "income"
                            category = ""
                            title = ""
                        },
                        label = { Text("Income") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Category Dropdown
                Text("Category", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { },
                        label = { Text("Select Category") },
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true },
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                Text(if (showCategoryDropdown) "^" else "v", fontSize = 12.sp)
                            }
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.keys.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    title = ""
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Title
                if (category.isNotEmpty()) {
                    Text("Title", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Select or Type Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (titles.isNotEmpty()) {
                                    IconButton(onClick = { showTitleDropdown = !showTitleDropdown }) {
                                        Text(if (showTitleDropdown) "^" else "v", fontSize = 12.sp)
                                    }
                                }
                            }
                        )
                        
                        if (titles.isNotEmpty()) {
                            DropdownMenu(
                                expanded = showTitleDropdown,
                                onDismissRequest = { showTitleDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                titles.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t) },
                                        onClick = {
                                            title = t
                                            showTitleDropdown = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("-- Custom --", fontWeight = FontWeight.Bold) },
                                    onClick = { showTitleDropdown = false }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Frequency
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
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amount.toDoubleOrNull() ?: 0.0
                if (title.isBlank() || amount.isBlank() || category.isBlank()) return@Button
                onAdd(RecurringTransactionData(
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
