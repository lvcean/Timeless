package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 图标和颜色选择器抽屉
 * 包含预设颜色 + 图标网格 + 高级颜色选择器入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconColorPickerSheet(
    selectedIcon: String,
    selectedColor: Color,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var showAdvancedColorPicker by remember { mutableStateOf(false) }
    
    // 预设颜色（8个常用色）
    val presetColors = listOf(
        Color(0xFF2563EB), // 蓝色
        Color(0xFF10B981), // 绿色
        Color(0xFFF59E0B), // 黄色
        Color(0xFFEF4444), // 红色
        Color(0xFF8B5CF6), // 紫色
        Color(0xFFEC4899), // 粉色
        Color(0xFF6B7280), // 灰色
        Color(0xFF14B8A6), // 青色
    )
    
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // 标题
            Text(
                text = "图标",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // 颜色选择行 - 添加横向滚动
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetColors.forEach { color ->
                    ColorDot(
                        color = color,
                        isSelected = selectedColor == color,
                        onClick = { onColorSelected(color) }
                    )
                }
                
                // 调色盘按钮（五彩渐变）
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color.Red,
                                    Color.Yellow,
                                    Color.Green,
                                    Color.Cyan,
                                    Color.Blue,
                                    Color.Magenta,
                                    Color.Red
                                )
                            )
                        )
                        .clickable { showAdvancedColorPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "高级颜色选择",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 图标选择器
            IconPicker(
                selectedIconName = selectedIcon,
                onIconSelected = onIconSelected
            )
        }
    }

    // 高级颜色选择器（第二层抽屉）
    if (showAdvancedColorPicker) {
        val advancedSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        AdvancedColorPickerSheet(
            initialColor = selectedColor,
            sheetState = advancedSheetState,
            onDismiss = { showAdvancedColorPicker = false },
            onColorSelected = { color ->
                onColorSelected(color)
                showAdvancedColorPicker = false
            }
        )
    }
}

@Composable
private fun ColorDot(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, Color.White, CircleShape)
                        .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                } else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp)
            )
        }
    }
}
