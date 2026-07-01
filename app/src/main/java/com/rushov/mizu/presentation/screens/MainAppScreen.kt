package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.local.DataStoreManager
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.presentation.utils.CategoriesHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    onLogout: () -> Unit,
    onChangePin: () -> Unit = {}
) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    var userId by remember { mutableStateOf(1) }
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        userId = dataStore.userId.first()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MIZU") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Transactions") },
                    label = { Text("Trans") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    label = { Text("Add") },
                    selected = selectedTab == 2,
                    onClick = { showAddDialog = true }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Charts") },
                    label = { Text("Charts") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Wallet, contentDescription = "Budget") },
                    label = { Text("Budget") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Repeat, contentDescription = "Recurring") },
                    label = { Text("Recurring") },
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == 6,
                    onClick = { selectedTab = 6 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(userId = userId)
                1 -> TransactionScreen(userId = userId)
                3 -> ChartsScreen(userId = userId)
                4 -> BudgetScreen(userId = userId)
                5 -> RecurringTransactionsScreen(userId = userId)
                6 -> ProfileScreen(
                    userId = userId,
                    onLogout = onLogout,
                    onChangePin = onChangePin
                )
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            userId = userId,
            onDismiss = { showAddDialog = false },
            onTransactionAdded = {
                showAddDialog = false
                selectedTab = 1
            }
        )
    }
}

@Composable
fun AddTransactionDialog(userId: Int, onDismiss: () -> Unit, onTransactionAdded: () -> Unit) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("expense") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTitleDropdown by remember { mutableStateOf(false) }

    val categories = CategoriesHelper.getCategories(type)
    val titles = categories[category] ?: emptyList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column {
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

                Spacer(modifier = Modifier.height(12.dp))

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

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || amount.isBlank() || category.isBlank()) {
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
                            val response = RetrofitClient.api.createTransaction(
                                userId,
                                com.rushov.mizu.data.remote.TransactionRequest(title, amt, category, type)
                            )
                            if (response.isSuccessful) {
                                onTransactionAdded()
                            } else {
                                error = "Failed to add"
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
