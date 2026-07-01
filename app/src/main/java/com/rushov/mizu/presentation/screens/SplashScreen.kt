package com.rushov.mizu.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rushov.mizu.data.local.DataStoreManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMainApp: () -> Unit
) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }

    // Logo animations
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, delayMillis = 200),
        label = "logo_alpha"
    )

    // Text staggered animations
    val textAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, delayMillis = 600),
        label = "text_alpha"
    )

    val taglineAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, delayMillis = 900),
        label = "tagline_alpha"
    )

    // Loading dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val dotOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Restart
        ),
        label = "dot_offset"
    )

    // Water drop shimmer
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    LaunchedEffect(key1 = true) {
        delay(2500)
        val loggedIn = dataStore.isLoggedIn.first()
        if (loggedIn) {
            onNavigateToMainApp()
        } else {
            onNavigateToOnboarding()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Water Drop Logo
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(logoScale)
                .alpha(logoAlpha),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 2.5f

                // Drop body with shimmer
                val shimmerColor = Color(
                    red = 1f,
                    green = 0.41f + (shimmerProgress * 0.2f),
                    blue = 0.71f + (shimmerProgress * 0.15f),
                    alpha = 1f
                )

                // Main circle
                drawCircle(
                    color = shimmerColor,
                    radius = radius,
                    center = Offset(centerX, centerY + radius * 0.1f)
                )

                // Drop point
                val trianglePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(centerX, centerY - radius * 1.3f)
                    lineTo(centerX - radius * 0.5f, centerY - radius * 0.3f)
                    lineTo(centerX + radius * 0.5f, centerY - radius * 0.3f)
                    close()
                }
                drawPath(trianglePath, color = shimmerColor)

                // Shine highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = radius * 0.25f,
                    center = Offset(centerX - radius * 0.25f, centerY - radius * 0.15f)
                )

                // Small bright dot
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = radius * 0.08f,
                    center = Offset(centerX - radius * 0.35f, centerY - radius * 0.35f)
                )

                // Outline stroke
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = radius,
                    center = Offset(centerX, centerY + radius * 0.1f),
                    style = Stroke(width = 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // MIZU text with scale
        Text(
            text = "MIZU",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .scale(logoScale)
                .alpha(logoAlpha)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tagline with stagger
        Text(
            text = "Money is Water",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.alpha(taglineAlpha)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Animated loading dots
        Row(
            modifier = Modifier.alpha(textAlpha),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                val dotAlpha = if (index <= dotOffset.toInt()) 1f else 0.3f
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(dotAlpha)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
}
