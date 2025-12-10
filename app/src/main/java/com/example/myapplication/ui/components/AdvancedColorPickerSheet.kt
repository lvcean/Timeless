package com.example.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/**
 * 高级颜色选择器
 * 包含三个模式：网格、光谱、滑块
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedColorPickerSheet(
    initialColor: Color,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var currentColor by remember { mutableStateOf(initialColor) }
    
    // RGB 分量
    var red by remember { mutableStateOf((initialColor.red * 255).toInt()) }
    var green by remember { mutableStateOf((initialColor.green * 255).toInt()) }
    var blue by remember { mutableStateOf((initialColor.blue * 255).toInt()) }

    // 更新颜色
    LaunchedEffect(red, green, blue) {
        currentColor = Color(red, green, blue)
    }
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val topPadding = screenHeight * 0.05f // 95% screen height

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = topPadding),
        containerColor = Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = true,
            isFocusable = true,
            securePolicy = SecureFlagPolicy.Inherit
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "关闭"
                    )
                }
                Text(
                    text = "颜色",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onColorSelected(currentColor) }) {
                    Text("完成", fontWeight = FontWeight.Bold)
                }
            }

            // Tab 切换
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("网格") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("光谱") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("滑块") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 内容区域
            when (selectedTab) {
                0 -> GridColorPicker(
                    selectedColor = currentColor,
                    onColorSelected = { 
                        currentColor = it
                        red = (it.red * 255).toInt()
                        green = (it.green * 255).toInt()
                        blue = (it.blue * 255).toInt()
                    }
                )
                1 -> SpectrumColorPicker(
                    selectedColor = currentColor,
                    onColorSelected = { 
                        currentColor = it
                        red = (it.red * 255).toInt()
                        green = (it.green * 255).toInt()
                        blue = (it.blue * 255).toInt()
                    }
                )
                2 -> SliderColorPicker(
                    red = red,
                    green = green,
                    blue = blue,
                    onRedChange = { red = it },
                    onGreenChange = { green = it },
                    onBlueChange = { blue = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 颜色预览和 Hex 值
            ColorPreview(currentColor)
        }
    }
}

@Composable
private fun GridColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val materialColors = listOf(
        // Red
        Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFEF9A9A), Color(0xFFE57373),
        Color(0xFFEF5350), Color(0xFFF44336), Color(0xFFE53935), Color(0xFFD32F2F),
        // Pink
        Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFF48FB1), Color(0xFFF06292),
        Color(0xFFEC407A), Color(0xFFE91E63), Color(0xFFD81B60), Color(0xFFC2185B),
        // Purple
        Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFCE93D8), Color(0xFFBA68C8),
        Color(0xFFAB47BC), Color(0xFF9C27B0), Color(0xFF8E24AA), Color(0xFF7B1FA2),
        // Blue
        Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF90CAF9), Color(0xFF64B5F6),
        Color(0xFF42A5F5), Color(0xFF2196F3), Color(0xFF1E88E5), Color(0xFF1976D2),
        // Green
        Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7), Color(0xFF81C784),
        Color(0xFF66BB6A), Color(0xFF4CAF50), Color(0xFF43A047), Color(0xFF388E3C),
        // Yellow
        Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFF59D), Color(0xFFFFF176),
        Color(0xFFFFEE58), Color(0xFFFFEB3B), Color(0xFFFDD835), Color(0xFFFBC02D),
        // Orange
        Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFCC80), Color(0xFFFFB74D),
        Color(0xFFFFA726), Color(0xFFFF9800), Color(0xFFFB8C00), Color(0xFFF57C00),
        // Grey
        Color(0xFFFAFAFA), Color(0xFFF5F5F5), Color(0xFFEEEEEE), Color(0xFFE0E0E0),
        Color(0xFFBDBDBD), Color(0xFF9E9E9E), Color(0xFF757575), Color(0xFF616161),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(materialColors) { color ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (selectedColor == color) {
                            Modifier.border(2.dp, Color.Black, CircleShape)
                        } else Modifier
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun SpectrumColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(1f) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HSV 色轮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val offset = change.position - center
                            val angle = atan2(offset.y, offset.x)
                            hue = ((angle + PI) / (2 * PI) * 360).toFloat()
                            val distance = sqrt(offset.x * offset.x + offset.y * offset.y)
                            saturation = (distance / (size.width / 2f)).coerceIn(0f, 1f)
                            
                            val color = hsvToColor(hue, saturation, value)
                            onColorSelected(color)
                        }
                    }
            ) {
                // 绘制色轮
                for (i in 0..360) {
                    val angleRad = i * PI / 180
                    val color = hsvToColor(i.toFloat(), 1f, 1f)
                    drawArc(
                        color = color,
                        startAngle = i.toFloat() - 0.5f,
                        sweepAngle = 1f,
                        useCenter = true
                    )
                }
                
                // 绘制选中指示器
                val angleRad = (hue - 90) * PI / 180
                val radius = saturation * size.width / 2
                val indicatorX = center.x + (radius * cos(angleRad)).toFloat()
                val indicatorY = center.y + (radius * sin(angleRad)).toFloat()
                
                drawCircle(
                    color = Color.White,
                    radius = 12f,
                    center = Offset(indicatorX, indicatorY),
                    style = Stroke(width = 3f)
                )
            }
        }

        // 亮度滑块
        Text("亮度", fontSize = 14.sp, color = Color.Gray)
        Slider(
            value = value,
            onValueChange = { 
                value = it
                onColorSelected(hsvToColor(hue, saturation, value))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SliderColorPicker(
    red: Int,
    green: Int,
    blue: Int,
    onRedChange: (Int) -> Unit,
    onGreenChange: (Int) -> Unit,
    onBlueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 红色滑块
        ColorSlider(
            label = "红色",
            value = red,
            onValueChange = onRedChange,
            color = Color.Red
        )
        
        // 绿色滑块
        ColorSlider(
            label = "绿色",
            value = green,
            onValueChange = onGreenChange,
            color = Color.Green
        )
        
        // 蓝色滑块
        ColorSlider(
            label = "蓝色",
            value = blue,
            onValueChange = onBlueChange,
            color = Color.Blue
        )
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(50.dp)
        )
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color
            )
        )
        
        Text(
            text = value.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
    }
}

@Composable
private fun ColorPreview(color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        )
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "sRGB Hex 颜色#",
                fontSize = 12.sp,
                color = Color(0xFF007AFF)
            )
            Text(
                text = colorToHex(color),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 辅助函数
private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
    val h = hue / 60f
    val c = value * saturation
    val x = c * (1 - abs(h % 2 - 1))
    val m = value - c
    
    val (r, g, b) = when (h.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    
    return Color(
        ((r + m) * 255).toInt(),
        ((g + m) * 255).toInt(),
        ((b + m) * 255).toInt()
    )
}

private fun colorToHex(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    return String.format("%02X%02X%02X", r, g, b)
}
