package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.presentation.components.MizuBottomNav

@Composable
fun MainAppScreen(onLogout: () -> Unit) {
    var selectedRoute by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            MizuBottomNav(
                selectedRoute = selectedRoute,
                onItemSelected = { route -> selectedRoute = route }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedRoute) {
                "home" -> HomeScreen()
                "transactions" -> TransactionsScreen(userId = 1)
                "budget" -> BudgetScreen(userId = 1)
                "profile" -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
