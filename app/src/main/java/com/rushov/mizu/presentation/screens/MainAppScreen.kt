package com.rushov.mizu.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class NavItem(
    val icon: ImageVector,
    val label: String,
    val index: Int
)

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

    val navItems = listOf(
        NavItem(Icons.Default.Home, "Home", 0),
        NavItem(Icons.Default.Search, "Trans", 1),
        NavItem(Icons.Default.Add, "Add", 2),
        NavItem(Icons.Default.Analytics, "Charts", 3),
        NavItem(Icons.Default.Psychology, "AI", 4),
        NavItem(Icons.Default.Wallet, "Budget", 5),
        NavItem(Icons.Default.Repeat, "Recurring", 6),
        NavItem(Icons.Default.Person, "Profile", 7)
    )

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
            AnimatedBottomNav(
                items = navItems,
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    if (index == 2) {
                        showAddDialog = true
                    } else {
                        selectedTab = index
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(userId = userId)
                1 -> TransactionScreen(userId = userId)
                3 -> ChartsScreen(userId = userId)
                4 -> InsightsScreen(userId = userId)
                5 -> BudgetScreen(userId = userId)
                6 -> RecurringTransactionsScreen(userId = userId)
                7 -> ProfileScreen(
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
fun AnimatedBottomNav(
    items: List<NavItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.index == selectedTab
                
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "icon_scale_${item.index}"
                )
                
                val iconAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.6f,
                    animationSpec = tween(300),
                    label = "icon_alpha_${item.index}"
                )

                // Use Box to avoid RowScope AnimatedVisibility conflict
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(item.index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        // Selected background indicator
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        )
                                )
                            }
                            
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .scale(scale)
                                    .alpha(iconAlpha)
                                    .size(24.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
                            exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(150))
                        ) {
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
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
