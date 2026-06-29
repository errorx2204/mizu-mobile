package com.rushov.mizu.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.remote.TransactionResponse

@Composable
fun ExpensePieChart(transactions: List<TransactionResponse>) {
    val expenses = transactions.filter { it.type == "expense" }
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    
    if (categoryTotals.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No expenses yet",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    val total = categoryTotals.sumOf { it.second }
    val colors = listOf(
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF3F51B5),
        Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF4CAF50),
        Color(0xFFFF9800), Color(0xFFFF5722),
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Expense Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryTotals.take(6).forEachIndexed { index, (category, amount) ->
                    val percentage = (amount / total * 100).toInt()
                    val color = colors[index % colors.size]
                    
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
                                    .background(color)
                            )
                            Text(
                                text = " $category",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        Text(
                            text = "?${String.format("%.0f", amount)} ($percentage%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Total Expenses: ?${String.format("%.0f", total)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun MonthlyBarChart(transactions: List<TransactionResponse>) {
    val monthlyData = transactions.groupBy { 
        it.created_at.substring(0, 7)
    }.mapValues { entry ->
        val income = entry.value.filter { it.type == "income" }.sumOf { it.amount }
        val expense = entry.value.filter { it.type == "expense" }.sumOf { it.amount }
        Pair(income, expense)
    }.toList().sortedBy { it.first }.takeLast(6)
    
    if (monthlyData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No monthly data yet",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        return
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Monthly Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Text(" Income", fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
                Spacer(modifier = Modifier.padding(horizontal = 16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE91E63))
                    )
                    Text(" Expense", fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val maxAmount = monthlyData.maxOf { maxOf(it.second.first, it.second.second) }
            
            monthlyData.forEach { (month, amounts) ->
                val income = amounts.first
                val expense = amounts.second
                val monthLabel = month.substring(5)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthLabel,
                        fontSize = 12.sp,
                        modifier = Modifier.width(40.dp)
                    )
                    
                    val incomeWidth = if (maxAmount > 0) {
                        ((income / maxAmount) * 0.4f).toFloat().coerceAtLeast(0.05f)
                    } else 0.05f
                    
                    Box(
                        modifier = Modifier
                            .weight(incomeWidth)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF4CAF50))
                    )
                    
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    
                    val expenseWidth = if (maxAmount > 0) {
                        ((expense / maxAmount) * 0.4f).toFloat().coerceAtLeast(0.05f)
                    } else 0.05f
                    
                    Box(
                        modifier = Modifier
                            .weight(expenseWidth)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE91E63))
                    )
                    
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "?${String.format("%.0f", income)}",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.width(50.dp)
                    )
                    Text(
                        text = "?${String.format("%.0f", expense)}",
                        fontSize = 10.sp,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.width(50.dp)
                    )
                }
            }
        }
    }
}


