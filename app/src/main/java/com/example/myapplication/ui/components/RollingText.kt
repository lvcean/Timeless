package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * 滚动数字组件
 * 数字会从 0 滚动到 targetValue
 */
@Composable
fun RollingText(
    targetValue: Int,
    suffix: String = "",
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier
) {
    // 动画状态
    var currentValue by remember { mutableIntStateOf(0) }
    
    // 启动动画
    LaunchedEffect(targetValue) {
        animate(
            initialValue = 0f,
            targetValue = targetValue.toFloat(),
            animationSpec = tween(
                durationMillis = 1000, // 1秒内滚完
                easing = FastOutSlowInEasing
            )
        ) { value, _ ->
            currentValue = value.toInt()
        }
    }

    Text(
        text = "$currentValue$suffix",
        style = style,
        color = color,
        fontWeight = fontWeight,
        modifier = modifier
    )
}
