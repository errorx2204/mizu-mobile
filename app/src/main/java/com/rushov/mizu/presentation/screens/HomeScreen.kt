package com.rushov.mizu.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.data.remote.TransactionResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(userId: Int = 1) {
    val scope = rememberCoroutineScope()
    var transactions by remember { mutableStateOf<List<TransactionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            try {
                isLoading = true
                val response = RetrofitClient.api.getTransactions(userId)
                if (response.isSuccessful) transactions = response.body() ?: emptyList()
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(key1 = userId) {
        loadData()
    }

    val expenses = transactions.filter { it.type == "expense" }
    val income = transactions.filter { it.type == "income" }
    val totalExpense = expenses.sumOf { it.amount }
    val totalIncome = income.sumOf { it.amount }
    val balance = totalIncome - totalExpense
    val recentTransactions = transactions.take(5)

    // Fade in animation for the whole screen
    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        screenVisible = true
    }

    AnimatedVisibility(
        visible = screenVisible,
        enter = fadeIn(animationSpec = tween(500)) + 
                slideInVertically(animationSpec = tween(500), initialOffsetY = { it / 6 }),
        modifier = Modifier.fillMaxSize()
    ) {
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
                    text = "Dashboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Animated refresh button
                var isRefreshing by remember { mutableStateOf(false) }
                val rotation by animateFloatAsState(
                    targetValue = if (isRefreshing) 360f else 0f,
                    animationSpec = tween(600, easing = FastOutSlowInEasing),
                    label = "refresh_rotation"
                )
                
                IconButton(onClick = { 
                    isRefreshing = !isRefreshing
                    loadData() 
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.graphicsLayer { rotationZ = rotation }
                    )
                }
            }

            if (isLoading) {
                // Shimmer loading effect
                ShimmerCard(modifier = Modifier.fillMaxWidth().height(120.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    ShimmerCard(modifier = Modifier.weight(1f).height(80.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    ShimmerCard(modifier = Modifier.weight(1f).height(80.dp))
                }
            } else {
                // Balance card with pulse animation if negative
                val isNegative = balance < 0
                val balanceModifier = if (isNegative) {
                    Modifier.pulseAnimation()
                } else {
                    Modifier
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(balanceModifier),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Total Balance",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Animated number count-up
                        AnimatedNumber(
                            target = balance,
                            prefix = "Rs. ",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFE91E63)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AnimatedSummaryCard("Income", totalIncome, Color(0xFF4CAF50), Modifier.weight(1f), delay = 100)
                    Spacer(modifier = Modifier.width(8.dp))
                    AnimatedSummaryCard("Expense", totalExpense, Color(0xFFE91E63), Modifier.weight(1f), delay = 200)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Recent Transactions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (recentTransactions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "No transactions yet",
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(recentTransactions) { index, transaction ->
                            StaggeredTransactionItem(
                                transaction = transaction,
                                index = index
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedSummaryCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier, delay: Int = 0) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + 
                scaleIn(animationSpec = tween(400), initialScale = 0.8f)
    ) {
        SummaryCard(title, amount, color, modifier)
    }
}

@Composable
fun StaggeredTransactionItem(transaction: TransactionResponse, index: Int) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(index * 80L + 300)
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(350)) + 
                slideInHorizontally(
                    animationSpec = tween(350),
                    initialOffsetX = { it / 3 }
                ),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        PressableCard(
            onClick = { /* Transaction detail */ }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = transaction.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${if (transaction.type == "income") "+" else "-"}Rs. ${String.format("%.2f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (transaction.type == "income") Color(0xFF4CAF50) else Color(0xFFE91E63)
                )
            }
        }
    }
}

@Composable
fun PressableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(150),
        label = "press_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 4.dp,
        animationSpec = tween(150),
        label = "press_elevation"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    ) {
        content()
    }
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_translate"
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.5f + (translateAnim % 500) / 1000f
                }
        )
    }
}

@Composable
fun AnimatedNumber(
    target: Double,
    prefix: String = "",
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current
) {
    val animatedValue by animateFloatAsState(
        targetValue = target.toFloat(),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "number_animation"
    )
    
    Text(
        text = "$prefix${String.format("%.2f", animatedValue)}",
        style = style
    )
}

@Composable
fun Modifier.pulseAnimation(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    return this.then(Modifier.graphicsLayer { scaleX = scale; scaleY = scale })
}

@Composable
fun SummaryCard(title: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rs. ${String.format("%.2f", amount)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
