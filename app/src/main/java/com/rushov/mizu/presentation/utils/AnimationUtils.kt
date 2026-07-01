package com.rushov.mizu.presentation.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

// Fade in animation for screens
@Composable
fun FadeInAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) + 
                slideInVertically(
                    animationSpec = tween(400),
                    initialOffsetY = { it / 4 }
                ),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        content()
    }
}

// Staggered list item animation
@Composable
fun StaggeredListItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val visible = remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 80L)
        visible.value = true
    }
    
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 2 }
                ),
        modifier = modifier
    ) {
        content()
    }
}

// Press animation for cards/buttons
@Composable
fun PressAnimation(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(150),
        label = "press_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(150),
        label = "press_alpha"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        content()
    }
}

// Shimmer loading effect
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
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
    
    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = 0.5f + (translateAnim % 500) / 1000f
            }
            .background(Color.LightGray.copy(alpha = 0.3f))
    )
}

// Number count-up animation
@Composable
fun AnimatedNumber(
    target: Number,
    suffix: String = "",
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge
) {
    val targetFloat = target.toFloat()
    val animatedValue by animateFloatAsState(
        targetValue = targetFloat,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "number_animation"
    )
    
    androidx.compose.material3.Text(
        text = "${"%.2f".format(animatedValue)}$suffix",
        modifier = modifier,
        style = style
    )
}

// Pulse animation for attention
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}
