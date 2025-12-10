package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Event
import com.example.myapplication.viewmodel.EventViewModel
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.myapplication.ui.components.bouncyClickable
import com.example.myapplication.ui.components.RollingText
import androidx.compose.animation.*
import androidx.compose.foundation.lazy.itemsIndexed
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class StatsPeriod {
    WEEK, MONTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: EventViewModel = viewModel(),
    onNavigateToEventStats: (String) -> Unit
) {
    val events by viewModel.events.collectAsState()
    var selectedPeriod by remember { mutableStateOf(StatsPeriod.WEEK) }
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }

    val scope = rememberCoroutineScope()

    // 全屏动态背景容器
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 底层：动态渐变背景
        com.example.myapplication.ui.components.AnimatedGradientBackground()
        
        // 2. 装饰层：浮动气泡
        com.example.myapplication.ui.components.FloatingBubbles()

        Scaffold(
            containerColor = Color.Transparent, // 透明背景
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "趋势",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent, // 标题栏透明
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 顶部统计卡片
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "事件总数",
                        value = events.size,
                        suffix = "个",
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "记录次数",
                        value = 0,
                        suffix = "次",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "使用天数",
                        value = 1,
                        suffix = "天",
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "记录天数",
                        value = 1,
                        suffix = "天",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. 周/月切换 (液体流转开关)
            item {
                val selectedIndex = if (selectedPeriod == StatsPeriod.WEEK) 0 else 1
                
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF0F2F5)) // 浅灰轨道
                        .padding(4.dp)
                ) {
                    val tabWidth = maxWidth / 2
                    
                    // 白色滑块 (带弹簧动画)
                    val indicatorOffset by animateDpAsState(
                        targetValue = if (selectedIndex == 0) 0.dp else tabWidth,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "indicator"
                    )

                    // 滑块本体
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffset)
                            .width(tabWidth)
                            .fillMaxHeight()
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                    )

                    // 文字层
                    Row(modifier = Modifier.fillMaxSize()) {
                        // 周按钮
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedPeriod = StatsPeriod.WEEK },
                            contentAlignment = Alignment.Center
                        ) {
                            val textColor by animateColorAsState(
                                targetValue = if (selectedIndex == 0) Color.Black else Color.Gray,
                                animationSpec = tween(300), 
                                label = "text"
                            )
                            Text("周", color = textColor, fontWeight = FontWeight.Bold)
                        }

                        // 月按钮
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedPeriod = StatsPeriod.MONTH },
                            contentAlignment = Alignment.Center
                        ) {
                            val textColor by animateColorAsState(
                                targetValue = if (selectedIndex == 1) Color.Black else Color.Gray,
                                animationSpec = tween(300),
                                label = "text"
                            )
                            Text("月", color = textColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. 日期导航
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentDate = Calendar.getInstance().apply {
                            timeInMillis = currentDate.timeInMillis
                            when (selectedPeriod) {
                                StatsPeriod.WEEK -> add(Calendar.WEEK_OF_YEAR, -1)
                                StatsPeriod.MONTH -> add(Calendar.MONTH, -1)
                            }
                        }
                    }) {
                        Icon(Icons.Default.ChevronLeft, "上一个")
                    }

                    Text(
                        text = getDateRangeText(currentDate, selectedPeriod),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    IconButton(onClick = {
                        currentDate = Calendar.getInstance().apply {
                            timeInMillis = currentDate.timeInMillis
                            when (selectedPeriod) {
                                StatsPeriod.WEEK -> add(Calendar.WEEK_OF_YEAR, 1)
                                StatsPeriod.MONTH -> add(Calendar.MONTH, 1)
                            }
                        }
                    }) {
                        Icon(Icons.Default.ChevronRight, "下一个")
                    }
                }
            }

            // 4. 事件列表
            if (selectedPeriod == StatsPeriod.WEEK) {
                // 周视图：单列布局 + 瀑布流
                itemsIndexed(events) { index, event ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 50L) // 瀑布流延迟
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically { 50 } + fadeIn()
                    ) {
                        EventStatCard(
                            event = event,
                            period = selectedPeriod,
                            currentDate = currentDate,
                            viewModel = viewModel,
                            onClick = { onNavigateToEventStats(event.id) }
                        )
                    }
                }
            } else if (selectedPeriod == StatsPeriod.MONTH) {
                // 月视图：两列布局
                items(events.chunked(2)) { eventPair ->
                    // 暂不加瀑布流给 Grid Row，或者简单处理
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        eventPair.forEach { event ->
                            EventStatCard(
                                event = event,
                                period = selectedPeriod,
                                currentDate = currentDate,
                                viewModel = viewModel,
                                onClick = { onNavigateToEventStats(event.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 如果是奇数个事件，填充空白
                        if (eventPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // 年视图：单列布局 + 瀑布流
                itemsIndexed(events) { index, event ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 50L)
                        isVisible = true
                    }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically { 50 } + fadeIn()
                    ) {
                        EventStatCard(
                            event = event,
                            period = selectedPeriod,
                            currentDate = currentDate,
                            viewModel = viewModel,
                            onClick = { onNavigateToEventStats(event.id) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } // Scaffold content closed
    } // Box closed
}



@Composable
private fun MiniStatCard(
    title: String,
    value: Int,
    suffix: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            RollingText(
                targetValue = value,
                suffix = suffix,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color(0xFF5F6368),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EventStatCard(
    event: Event,
    period: StatsPeriod,
    currentDate: Calendar,
    viewModel: EventViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var recordDates by remember { mutableStateOf<List<Long>>(emptyList()) }
    var recordCount by remember { mutableStateOf(0) }
    var recordDays by remember { mutableStateOf(0) }

    val (startTime, endTime) = getTimeRange(currentDate, period)

    LaunchedEffect(event.id, period, currentDate) {
        scope.launch {
            recordDates = viewModel.getRecordDatesInRange(event.id, startTime, endTime)
            recordCount = viewModel.getRecordCountInRange(event.id, startTime, endTime)
            recordDays = viewModel.getRecordDaysInRange(event.id, startTime, endTime)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bouncyClickable(onClick = onClick), // 使用果冻效果
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.75f)
        )
    ) {
        // 周视图和月视图的布局
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 头部：图标 + 名称
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(event.backgroundColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = com.example.myapplication.ui.components.IconLibrary.getIcon(event.icon),
                        contentDescription = null,
                        tint = event.backgroundColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = event.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 根据周期显示不同的可视化
            when (period) {
                StatsPeriod.WEEK -> WeekView(event, recordDates, startTime)
                StatsPeriod.MONTH -> MonthView(event, recordDates, currentDate)
            }

            // 月视图显示统计数字（底部）
            if (period == StatsPeriod.MONTH) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = recordCount.toString(), fontSize = 14.sp, color = Color.Gray)
                    }
                    Text("|", color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.WbSunny,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = recordDays.toString(), fontSize = 14.sp, color = Color.Gray)
                    }
            }
        }
    }
}
}

@Composable
private fun WeekView(event: Event, recordDates: List<Long>, startTime: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        repeat(7) { dayIndex ->
            val dayTimestamp = startTime + (dayIndex * 86400000L)
            val hasRecord = recordDates.any { isSameDay(it, dayTimestamp) }

            val scale = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(dayIndex * 50L)
                scale.animateTo(1f, spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.6f))
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(
                        if (hasRecord) event.backgroundColor
                        else Color(0xFFE0E0E0)
                    )
            )
        }
    }
}

@Composable
private fun MonthView(event: Event, recordDates: List<Long>, currentDate: Calendar) {
    val daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in 0 until (daysInMonth + 6) / 7) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (col in 0 until 7) {
                    val day = row * 7 + col + 1
                    if (day <= daysInMonth) {
                        val dayTimestamp = Calendar.getInstance().apply {
                            set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), day, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        val hasRecord = recordDates.any { isSameDay(it, dayTimestamp) }

                        val scale = remember { Animatable(0f) }
                        LaunchedEffect(Unit) {
                            delay((row * 7 + col) * 10L) // 稍微快一点
                            scale.animateTo(1f, spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.6f))
                        }

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(scale.value)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (hasRecord) event.backgroundColor
                                    else Color(0xFFE0E0E0)
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun YearView(event: Event, recordDates: List<Long>, currentDate: Calendar) {
    val year = currentDate.get(Calendar.YEAR)

    // 年视图热力图：使用合适大小的方块，无需滚动
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally)
    ) {
        for (week in 0 until 53) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (dayOfWeek in 0 until 7) {
                    val dayOfYear = week * 7 + dayOfWeek + 1
                    if (dayOfYear <= 365) {
                        val dayTimestamp = Calendar.getInstance().apply {
                            clear()
                            set(Calendar.YEAR, year)
                            set(Calendar.DAY_OF_YEAR, dayOfYear)
                        }.timeInMillis

                        val hasRecord = recordDates.any { isSameDay(it, dayTimestamp) }

                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(
                                    if (hasRecord) event.backgroundColor.copy(alpha = 0.8f)
                                    else Color(0xFFE0E0E0)
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.size(5.dp))
                    }
                }
            }
        }
    }
}




private fun getDateRangeText(date: Calendar, period: StatsPeriod): String {
    return when (period) {
        StatsPeriod.WEEK -> {
            val start = Calendar.getInstance().apply {
                timeInMillis = date.timeInMillis
                set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }
            val end = Calendar.getInstance().apply {
                timeInMillis = start.timeInMillis
                add(Calendar.DAY_OF_YEAR, 6)
            }
            "${start.get(Calendar.YEAR)}年${start.get(Calendar.MONTH) + 1}月${start.get(Calendar.DAY_OF_MONTH)}日 - " +
                    "${end.get(Calendar.YEAR)}年${end.get(Calendar.MONTH) + 1}月${end.get(Calendar.DAY_OF_MONTH)}日"
        }
        StatsPeriod.MONTH -> {
            "${date.get(Calendar.YEAR)}年${date.get(Calendar.MONTH) + 1}月"
        }
    }
}

private fun getTimeRange(date: Calendar, period: StatsPeriod): Pair<Long, Long> {
    return when (period) {
        StatsPeriod.WEEK -> {
            val start = Calendar.getInstance().apply {
                timeInMillis = date.timeInMillis
                set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val end = Calendar.getInstance().apply {
                timeInMillis = start.timeInMillis
                add(Calendar.DAY_OF_YEAR, 6)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            Pair(start.timeInMillis, end.timeInMillis)
        }
        StatsPeriod.MONTH -> {
            val start = Calendar.getInstance().apply {
                set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val end = Calendar.getInstance().apply {
                timeInMillis = start.timeInMillis
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            Pair(start.timeInMillis, end.timeInMillis)
        }
    }
}

private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
