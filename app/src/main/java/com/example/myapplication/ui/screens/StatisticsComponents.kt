package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.clipRect

@Composable
fun ModernStatContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun EmptyStatsBox(height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无数据",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SimpleBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxCount = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    // Animation State
    var isVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEachIndexed { index, (label, count) ->
            // Staggered animation for each bar (optional, but nice)
            val animatedHeightRatio by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isVisible) (if (count == 0) 0f else (count.toFloat() / maxCount)) else 0f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 600,
                    delayMillis = index * 50, // Stagger effect
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                ),
                label = "barHeight"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .weight(1f, fill = false) // Fix height
                        .heightIn(min = 0.dp) // Flexible
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.BottomCenter)
                    ) {
                        // Background track (Optional, maybe too busy)
                         Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        )
                        
                        // Actual Value Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(animatedHeightRatio)
                                .background(
                                    color = if (count > 0) primaryColor else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Label
                Text(
                    text = label.substring(3), // dd
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurfaceColor
                )
            }
        }
    }
}

@Composable
fun CategoryDistributionChart(
    data: List<Pair<String, Int>>
) {
    val maxCount = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    
    // Animation State
    var isVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        data.forEachIndexed { index, (category, count) ->
            val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (isVisible) (count.toFloat() / maxCount) else 0f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 800,
                    delayMillis = index * 100, // Stagger
                    easing = androidx.compose.animation.core.EaseOutQuart
                ),
                label = "progress"
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun AttributeLineChart(
    data: List<Pair<String, Float>>,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOf { it.second }
    val minVal = data.minOf { it.second }
    val range = (maxVal - minVal).coerceAtLeast(1f) // Avoid div by zero

    // Animation for path drawing
    var animationProgress by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        androidx.compose.animation.core.animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.EaseOutQuart)
        ) { value, _ ->
            animationProgress = value
        }
    }

    Column(modifier = modifier) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp) // Space for dots
        ) {
            val width = size.width
            val height = size.height
            
            if (width == 0f || height == 0f) return@Canvas

            val spacing = width / (data.size - 1).coerceAtLeast(1)

            val path = androidx.compose.ui.graphics.Path()
            val fillPath = androidx.compose.ui.graphics.Path() // For gradient below line

            // Build path
            data.forEachIndexed { index, (label, value) ->
                val x = index * spacing
                // Invert Y because canvas 0,0 is top-left
                // Map value [min, max] to [height, 0]
                // We leave some padding top/bottom
                val normalizedValue = (value - minVal) / range
                // Use 10% padding on top and bottom
                val y = height - (normalizedValue * height * 0.8f) - (height * 0.1f)

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height) // Start from bottom-left
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                
                // Draw dots (only if fully animated or passed specific threshold)
                if (animationProgress > index.toFloat() / data.size) {
                    drawCircle(
                        color = color,
                        radius = 4.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
            }
            // Close fill path
            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw Gradient Fill and Line with Clip
            clipRect(right = width * animationProgress) {
                drawPath(
                    path = fillPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.3f),
                            color.copy(alpha = 0.0f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )
                
                drawPath(
                    path = path,
                    color = color,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
        
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
             if (data.size > 1) {
                Text(
                    text = data.first().first.takeLast(5), // mm-dd
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (data.size > 2) {
                     Text(
                        text = "...", // Simplified middle
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                Text(
                    text = data.last().first.takeLast(5),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (data.isNotEmpty()) {
                Text(
                    text = data.first().first,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
