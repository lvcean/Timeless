package com.example.myapplication.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Event
import com.example.myapplication.ui.components.AddEventBottomSheet
import com.example.myapplication.ui.components.EventCard
import com.example.myapplication.viewmodel.EventViewModel
import java.util.Calendar
import java.util.UUID

/**
 * 首页 - Modern Dashboard Style
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    viewModel: EventViewModel,
    onEventClick: (Event) -> Unit,
    onEditEvent: (Event) -> Unit, // Kept for compatibility but might be unused if we use Sheet for everything
    onGlobalCalendarClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val events by viewModel.events.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var detailedRecordEvent by remember { mutableStateOf<Event?>(null) }
    var showCheckInAnimation by remember { mutableStateOf(false) }  // 打卡成功动画

    val onQuickAdd: (Event) -> Unit = {
        showCheckInAnimation = true  // 触发打卡动画
    }
    
    // Custom Event Sheet State
    var showCustomEventSheet by remember { mutableStateOf(false) }
    var initialEventForSheet by remember { mutableStateOf<Event?>(null) }

    // Helper to open sheet
    fun openCustomSheet(event: Event?) {
        initialEventForSheet = event
        showCustomEventSheet = true
    }
    
    // Collapsing Toolbar Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Greeting Logic
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "早上好 ☀️"
        in 12..18 -> "下午好 🌤️"
        else -> "晚上好 🌙"
    }

    // Hoisted Sheet States
    val addEventSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val customSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val recordSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 全屏动态背景容器
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 底层：动态渐变背景
        com.example.myapplication.ui.components.AnimatedGradientBackground()
        
        // 2. 装饰层：浮动气泡
        com.example.myapplication.ui.components.FloatingBubbles()

        // 3. 内容层：Scaffold
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent, // 关键：设置为透明，让背景透出来
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = "首页",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = onGlobalCalendarClick) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Global Calendar")
                        }
                        IconButton(onClick = onAchievementsClick) {
                            Icon(Icons.Filled.EmojiEvents, contentDescription = "Achievements")
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent, // 关键：标题栏透明
                        scrolledContainerColor = Color.Transparent // 滚动后保持全透明
                    ),
                    scrollBehavior = scrollBehavior
                )
            },
            floatingActionButton = {
                // 呼吸与脉冲动画
                val infiniteTransition = rememberInfiniteTransition(label = "fab")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                
                Box(contentAlignment = Alignment.Center) {
                    FloatingActionButton(
                        onClick = { showBottomSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .scale(scale)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = MaterialTheme.colorScheme.primary
                            )
                    ) {
                        Icon(Icons.Default.Add, "新建事件")
                    }
                }
            }
        ) { innerPadding ->
        
        // Box(modifier = Modifier.fillMaxSize()) {
            // 背景已移至最外层
            
            // 内容层
            if (events.isEmpty()) {
                // 精美的空状态页面
                com.example.myapplication.ui.components.EmptyStateView(
                    title = "还没有事件",
                    subtitle = "创建你的第一个打卡事件\n开始记录美好生活！",
                    actionText = "创建事件",
                    onActionClick = { showBottomSheet = true },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between cards
            ) {
                // Greeting as the first item with floating animation
                item { 
                    val infiniteTransition = rememberInfiniteTransition(label = "greeting")
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "greetingFloat"
                    )
                    
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .offset(y = offsetY.dp)
                    )
                } 
                
                itemsIndexed(events) { index, event ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 50L)
                        isVisible = true
                    }
                    
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isVisible,
                        enter = androidx.compose.animation.slideInVertically { 100 } + androidx.compose.animation.fadeIn()
                    ) {
                        var showDeleteConfirm by remember { mutableStateOf(false) }
                        
                        EventCard(
                            event = event,
                            onAddClick = {
                                if (event.isQuickRecord) {
                                    viewModel.addRecord(event.id)
                                    onQuickAdd(event)
                                } else {
                                    detailedRecordEvent = event
                                }
                            },
                            onClick = { onEventClick(event) },
                            onEditClick = { openCustomSheet(event) },
                            onDeleteClick = { showDeleteConfirm = true },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                        
                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text("删除事件") },
                                text = { Text("确定要删除 \"${event.name}\" 吗？所有记录也将被删除。") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteEvent(event)
                                            showDeleteConfirm = false
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("删除")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) {
                                        Text("取消")
                                    }
                                }
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom spacing for FAB
            }
            }  // else 闭合
        // }  // 原 Box 闭合 (已移除)
    }  // Scaffold 闭合
    } // 最外层 Box 闭合



    // Bottom Sheet Logic
    if (showBottomSheet) {
        AddEventBottomSheet(
            sheetState = addEventSheetState,
            onDismiss = { showBottomSheet = false },
            onEventSelected = { presetEvent ->
                // Open CustomEventSheet with preset data
                val preset = Event(
                    id = UUID.randomUUID().toString(),
                    name = presetEvent.name,
                    icon = presetEvent.icon,
                    backgroundColor = presetEvent.backgroundColor,
                    category = presetEvent.category,
                    attributes = presetEvent.attributes,
                    eventType = presetEvent.eventType,
                    groupName = presetEvent.groupName
                )
                openCustomSheet(preset)
                // Don't close AddEventBottomSheet - keep it open for stacking
            },
            onCustomEvent = {
                // Don't close AddEventBottomSheet - keep it open for stacking
                openCustomSheet(null) // New blank event
            }
        )
    }

    if (showCustomEventSheet) {
        com.example.myapplication.ui.components.CustomEventSheet(
            initialEvent = initialEventForSheet,
            sheetState = customSheetState,
            onDismiss = { 
                showCustomEventSheet = false 
                initialEventForSheet = null
            },
            onConfirm = { newEvent ->
                // Logic to update or add
                // We can check if the ID exists in the current list to decide
                val isExisting = events.any { it.id == newEvent.id }
                if (isExisting) {
                    viewModel.updateEvent(newEvent)
                } else {
                    viewModel.addEvent(newEvent)
                }
                showCustomEventSheet = false
                initialEventForSheet = null
            }
        )
    }






    // 打卡成功动画（烟花效果）
    if (showCheckInAnimation) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            com.example.myapplication.ui.components.CheckInSuccessAnimation(
                onAnimationEnd = { showCheckInAnimation = false }
            )
        }
    }

    if (detailedRecordEvent != null) {
        com.example.myapplication.ui.components.AddRecordSheet(
            event = detailedRecordEvent!!,
            sheetState = recordSheetState,
            onDismiss = { detailedRecordEvent = null },
            onConfirm = { note, timestamp, attributes ->
                viewModel.addRecord(detailedRecordEvent!!.id, note, timestamp, attributes)
                detailedRecordEvent = null
                // Trigger check-in animation
                showCheckInAnimation = true  // 触发打卡动画
            },
            onUpdateAttribute = { updatedAttr ->
                viewModel.updateEventAttribute(detailedRecordEvent!!, updatedAttr)
            }
        )
    }
}
