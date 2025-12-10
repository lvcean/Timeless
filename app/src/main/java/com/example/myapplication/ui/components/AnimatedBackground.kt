package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * 动态渐变背景
 * 创建流动的渐变效果
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFFF8FDF9), // 极淡绿
        Color(0xFFD8F3DC), // 淡薄荷绿
        Color(0xFFE8F5E9)  // 淡绿
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, offset * 1000f),
                    end = Offset(1000f, (1f - offset) * 1000f)
                )
            )
    )
}

/**
 * 浮动气泡背景装饰
 */
@Composable
fun FloatingBubbles(
    modifier: Modifier = Modifier,
    bubbleCount: Int = 8
) {
    Box(modifier = modifier.fillMaxSize()) {
        repeat(bubbleCount) { index ->
            FloatingBubble(
                delay = index * 200,
                size = (40 + index * 15).dp,
                offsetX = (index * 100).dp,
                offsetY = (index * 80).dp
            )
        }
    }
}

@Composable
private fun FloatingBubble(
    delay: Int,
    size: androidx.compose.ui.unit.Dp,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_$delay")
    
    val animatedY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000 + delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y"
    )
    
    val animatedX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000 + delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000 + delay,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .offset(
                x = offsetX + (sin(animatedX * Math.PI).toFloat() * 30).dp,
                y = offsetY + (animatedY * 100).dp
            )
            .size(size)
            .scale(scale)
            .alpha(0.15f)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
    )
}

/**
 * 脉冲圆环效果
 */
@Composable
fun PulseRing(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .background(
                color = color,
                shape = CircleShape
            )
    )
}
