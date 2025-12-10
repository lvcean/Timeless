package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Event
import com.example.myapplication.model.EventCategory
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Modern Event Card
 * - No Border
 * - Soft Shadow (Elevation)
 * - Large Corner Radius
 * - Prominent Icon
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EventCard(
    event: Event,
    onAddClick: () -> Unit,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "scale"
    )

    var showToolbar by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .scale(scale)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                    onLongClick = { 
                        showToolbar = true
                    }
                ),
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)), // 轻微描边
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.75f) // 75% 透明乳白色
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp, // 移除投影以增强平面透视感，或者保留极淡的投影
                pressedElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ... Icon Container code ...
                val iconModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = "event-icon-${event.id}"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    }
                } else Modifier
    
                Box(
                    modifier = Modifier
                        .then(iconModifier)
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(event.backgroundColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                val vector = IconLibrary.IconMap[event.icon]
                if (vector != null) {
                    Icon(
                        imageVector = vector,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = event.backgroundColor
                    )
                } else {
                    Text(text = event.icon, fontSize = 28.sp)
                }
                }
                
                // Text Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.lastRecordTime ?: "从未记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                
                // Action Button
                IconButton(
                    onClick = onAddClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = event.backgroundColor
                    ),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Record",
                        tint = Color.White
                    )
                }
            }
        }

        // Floating Toolbar Popup
        if (showToolbar) {
             androidx.compose.ui.window.Popup(
                 alignment = Alignment.TopCenter,
                 offset = androidx.compose.ui.unit.IntOffset(0, -120),
                 onDismissRequest = { showToolbar = false }
             ) {
                 FloatingToolbar(
                     onEdit = { 
                         showToolbar = false
                         onEditClick()
                     },
                     onDelete = {
                         showToolbar = false
                         onDeleteClick()
                     }
                 )
             }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
fun EventCardPreview() {
    MyApplicationTheme {
        Column(Modifier.padding(16.dp)) {
            EventCard(
                event = Event(
                    id = "1",
                    name = "Finance",
                    icon = "💰",
                    backgroundColor = Color(0xFFFFE082),
                    lastRecordTime = "Today 18:45",
                    category = EventCategory.FINANCE
                ),
                onAddClick = {},
                onClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )
            Spacer(Modifier.height(16.dp))
            EventCard(
                event = Event(
                    id = "2",
                    name = "Running",
                    icon = "🏃",
                    backgroundColor = Color(0xFF64B5F6),
                    lastRecordTime = null,
                    category = EventCategory.HEALTH
                ),
                onAddClick = {},
                onClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )
        }
    }
}
