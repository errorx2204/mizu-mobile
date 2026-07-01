package com.rushov.mizu.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.remote.InsightItem
import com.rushov.mizu.data.remote.RetrofitClient
import kotlinx.coroutines.launch

@Composable
fun InsightsScreen(userId: Int = 1) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var insights by remember { mutableStateOf<List<InsightItem>>(emptyList()) }
    var summary by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val response = RetrofitClient.api.getInsights(userId)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    insights = body.insights
                    summary = mapOf(
                        "total_spent" to body.summary.total_spent,
                        "total_budget" to body.summary.total_budget,
                        "categories_tracked" to body.summary.categories_tracked,
                        "insights_count" to body.summary.insights_count
                    )
                    isLoading = false
                } else {
                    error = "Failed to load insights"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "AI Insights",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Spending Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(
                        icon = Icons.Default.AttachMoney,
                        label = "Total Spent",
                        value = "$${summary["total_spent"] ?: 0.0}",
                        color = Color(0xFFE91E63)
                    )
                    SummaryItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Budget",
                        value = "$${summary["total_budget"] ?: 0.0}",
                        color = Color(0xFF4CAF50)
                    )
                    SummaryItem(
                        icon = Icons.Default.Category,
                        label = "Categories",
                        value = "${summary["categories_tracked"] ?: 0}",
                        color = Color(0xFF2196F3)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val totalSpent = (summary["total_spent"] as? Double) ?: 0.0
                val totalBudget = (summary["total_budget"] as? Double) ?: 0.0
                val remaining = totalBudget - totalSpent

                Text(
                    text = if (remaining >= 0) {
                        "$${"%.2f".format(remaining)} remaining this month"
                    } else {
                        "$${"%.2f".format(kotlin.math.abs(remaining))} over budget!"
                    },
                    fontSize = 14.sp,
                    color = if (remaining >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (insights.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "No insights",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Insights Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add more transactions and budgets to get AI-powered insights!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            Text(
                text = "${insights.size} Insights",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            insights.forEach { insight ->
                InsightCard(insight = insight)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun InsightCard(insight: InsightItem) {
    val (cardColor, icon) = when (insight.type) {
        "warning" -> Pair(
            when (insight.severity) {
                "high" -> Color(0xFFFFEBEE)
                else -> Color(0xFFFFF3E0)
            },
            when (insight.severity) {
                "high" -> Icons.Default.Warning
                else -> Icons.Default.Info
            }
        )
        "positive" -> Pair(Color(0xFFE8F5E9), Icons.Default.CheckCircle)
        "trend" -> Pair(Color(0xFFE3F2FD), Icons.Default.TrendingUp)
        "suggestion" -> Pair(Color(0xFFF3E5F5), Icons.Default.Lightbulb)
        else -> Pair(MaterialTheme.colorScheme.surface, Icons.Default.Info)
    }

    val iconColor = when (insight.type) {
        "warning" -> when (insight.severity) {
            "high" -> Color(0xFFE91E63)
            else -> Color(0xFFFF9800)
        }
        "positive" -> Color(0xFF4CAF50)
        "trend" -> Color(0xFF2196F3)
        "suggestion" -> Color(0xFF9C27B0)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = insight.type,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = insight.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.category.uppercase(),
                    fontSize = 11.sp,
                    color = iconColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
