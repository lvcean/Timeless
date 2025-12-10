package com.example.myapplication.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 弹性按压动画 Modifier
 * 使用 Spring 动画实现自然的按压反馈
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce_scale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    val press = androidx.compose.foundation.interaction.PressInteraction.Press(it)
                    interactionSource.emit(press)
                    tryAwaitRelease()
                    interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                },
                onTap = { onClick() }
            )
        }
}

/**
 * 轻微的悬浮效果（用于卡片）
 */
fun Modifier.hoverEffect(
    elevationChange: Float = 4f
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) elevationChange else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "hover_elevation"
    )
    
    this.graphicsLayer {
        translationY = -elevation
        shadowElevation = elevation
    }
}
