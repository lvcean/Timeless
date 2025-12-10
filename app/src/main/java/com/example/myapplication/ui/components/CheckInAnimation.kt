package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 打卡成功动画组件 (烟花版)
 * 显示一个带有粒子爆炸效果的成功图标，播放完毕后自动回调结束
 */
@Composable
fun CheckInSuccessAnimation(
    onAnimationEnd: () -> Unit = {}
) {
    // 动画进度控制 (0f -> 1f)
    val animationProgress = remember { Animatable(0f) }
    
    // 启动动画
    LaunchedEffect(Unit) {
        // 播放时长 1.5 秒
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
        // 动画结束回调
        onAnimationEnd()
    }
    
    val progress = animationProgress.value

    // 生成随机粒子
    val particles = remember {
        List(50) { // 增加粒子数量到 50
            Particle(
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 500f + 200f, // 增加速度/距离
                size = Random.nextFloat() * 12f + 6f, // 增加粒子大小
                color = when(Random.nextInt(3)) {
                   0 -> Color(0xFF52B788) // 主色
                   1 -> Color(0xFF95D5B2) // 亮色
                   else -> Color(0xFFB7E4C7) // 淡色
                }
            )
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 1. 粒子爆炸效果
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                drawParticle(particle, progress)
            }
        }
        
        // 2. 成功图标已移除 (仅保留烟花效果)
    }
}

/**
 * 粒子数据类
 */
private data class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

/**
 * 绘制单个粒子
 */
private fun DrawScope.drawParticle(particle: Particle, progress: Float) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    // 计算粒子位置 (向外扩散)
    val distance = particle.speed * progress // 增加扩散范围
    val radian = Math.toRadians(particle.angle.toDouble())
    val x = centerX + (distance * cos(radian)).toFloat()
    val y = centerY + (distance * sin(radian)).toFloat()
    
    // 计算透明度 (先显现后消失)
    val alpha = when {
        progress < 0.2f -> progress * 5f // 渐入
        progress > 0.6f -> (1f - progress) * 2.5f // 渐出
        else -> 1f
    }.coerceIn(0f, 1f)
    
    // 绘制粒子
    drawCircle(
        color = particle.color.copy(alpha = alpha),
        radius = particle.size * (1f - progress * 0.2f), // 粒子也会稍微变小
        center = Offset(x, y)
    )
}

/**
 * 简化版本 - 仅显示成功图标动画
 */

