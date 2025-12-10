package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Event
import com.example.myapplication.viewmodel.EventViewModel
import com.example.myapplication.model.AttributeType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EventStatisticsScreen(
    eventId: String,
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val events by viewModel.events.collectAsState()
    val event = events.find { it.id == eventId }
    
    // Load records for this specific event
    val records by viewModel.getRecordsByEventId(eventId).collectAsState(initial = emptyList())

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // State for delete confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除事件") },
            text = { Text("确定要删除这个事件吗？所有的历史记录也将被删除，此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (event != null) {
                            viewModel.deleteEvent(event)
                            onNavigateBack()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(event?.name ?: "事件详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete, 
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("未找到事件")
            }
        } else {
            // Prepared Data for Numeric Analysis
            val numericAttributes = remember(event) { 
                event.attributes.filter { it.type == AttributeType.NUMBER } 
            }
            
            val numericDataMap = remember(records, numericAttributes) {
                val map = mutableMapOf<String, MutableList<Pair<Long, Float>>>()
                records.forEach { record ->
                     if (record.attributesData.isNotEmpty() && record.attributesData != "{}") {
                         try {
                             val type = object : TypeToken<Map<String, String>>() {}.type
                             val attrMap = Gson().fromJson<Map<String, String>>(record.attributesData, type)
                             numericAttributes.forEach { attr ->
                                 val valStr = attrMap[attr.id]
                                 if (valStr != null) {
                                     val valFloat = valStr.toFloatOrNull()
                                     if (valFloat != null) {
                                         map.getOrPut(attr.id) { mutableListOf() }.add(record.timestamp to valFloat)
                                     }
                                 }
                             }
                         } catch(e: Exception) {}
                     }
                }
                map.forEach { (_, list) -> list.sortBy { it.first } }
                map
            }



            // Prepared Data for Text/Multi Attributes (Diversity Analysis)
            val distributionAttributes = remember(event) {
                event.attributes.filter { 
                    it.type == AttributeType.SINGLE_SELECT || 
                    it.type == AttributeType.MULTI_SELECT ||
                    it.type == AttributeType.RATING
                }
            }

            val distributionDataMap = remember(records, distributionAttributes) {
                val map = mutableMapOf<String, MutableMap<String, Int>>()
                records.forEach { record ->
                    if (record.attributesData.isNotEmpty() && record.attributesData != "{}") {
                        try {
                            val type = object : TypeToken<Map<String, String>>() {}.type
                            val attrMap = Gson().fromJson<Map<String, String>>(record.attributesData, type)
                            distributionAttributes.forEach { attr ->
                                val valStr = attrMap[attr.id]
                                if (!valStr.isNullOrEmpty()) {
                                    if (attr.type == AttributeType.MULTI_SELECT) {
                                        valStr.split(",").forEach { option ->
                                            val trimmed = option.trim()
                                            if (trimmed.isNotEmpty()) {
                                                val countMap = map.getOrPut(attr.id) { mutableMapOf() }
                                                countMap[trimmed] = (countMap[trimmed] ?: 0) + 1
                                            }
                                        }
                                    } else {
                                        val countMap = map.getOrPut(attr.id) { mutableMapOf() }
                                        countMap[valStr] = (countMap[valStr] ?: 0) + 1
                                    }
                                }
                            }
                        } catch (e: Exception) {}
                    }
                }
                map
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. Header Card (Icon & Basic Info)
                item {
                    ModernStatContainer(title = "信息") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(event.backgroundColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = com.example.myapplication.ui.components.IconLibrary.getIcon(event.icon),
                                    contentDescription = null,
                                    tint = event.backgroundColor,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "分类: ${event.category.displayName}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val animatedCount by androidx.compose.animation.core.animateIntAsState(
                                    targetValue = records.size,
                                    animationSpec = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                    label = "totalRecords"
                                )
                                Text(
                                    text = "总记录: $animatedCount 次",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // 1.5 Streaks & Heatmap
                item {
                    val (currentStreak, maxStreak) = remember(records) { calculateStreaks(records) }
                    val heatData = remember(records) { getHeatMapData(records) }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        StreakCard(currentStreak, maxStreak)
                        
                        ModernStatContainer(title = "活跃热力 (近30天)") {
                            HeatMapCalendar(heatData)
                        }
                    }
                }

                // 2. Weekly Trend for THIS event
                item {
                    val trendData = remember(records) {
                        calculateEventTrend(records)
                    }
                    
                    ModernStatContainer(title = "近期趋势") {
                        if (trendData.any { it.second > 0 }) {
                            SimpleBarChart(
                                data = trendData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(top = 16.dp)
                            )
                        } else {
                            EmptyStatsBox(height = 150.dp)
                        }
                    }
                }

                // 2.5 Numeric Attribute Analysis (Visuals)
                numericAttributes.forEach { attr ->
                    val data = numericDataMap[attr.id]
                    if (!data.isNullOrEmpty()) {
                        item {
                            ModernStatContainer(title = "${attr.name} 趋势") {
                                val lastValue = data.last().second
                                val lastDate = formatTimestamp(data.last().first)
                                
                                Column {
                                    Text(
                                        text = "最新: $lastValue ${attr.unit ?: ""}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = lastDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Convert to display data (MM-dd)
                                    val displayData = data.takeLast(20).map { (ts, v) ->
                                         val sdf = SimpleDateFormat("MM-dd", Locale.getDefault())
                                         sdf.format(Date(ts)) to v
                                    }
                                    
                                    AttributeLineChart(
                                        data = displayData,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Recent History List

                // 3. Recent History List
                item {
                    Text(
                        text = "最近记录",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("删除此事件")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (records.isEmpty()) {
                    item { EmptyStatsBox(height = 100.dp) }
                } else {
                    // Show last 10 records
                    val recentRecords = records.sortedByDescending { it.timestamp }.take(20)
                    itemsIndexed(recentRecords) { index, record ->
                        var isVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 30L)
                            isVisible = true
                        }
                        
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isVisible,
                            enter = androidx.compose.animation.slideInHorizontally { 50 } + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.deleteRecord(record)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                        else -> Color.Transparent
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.White
                                        )
                                    }
                                },
                                content = {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Filled.DateRange,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = formatTimestamp(record.timestamp),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                            
                                            if (record.note.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(verticalAlignment = Alignment.Top) {
                                                    Icon(
                                                        Icons.Outlined.Notes,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp).padding(top = 2.dp),
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = record.note,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                            
                                            // Attributes (if any)
                                            if (record.attributesData.isNotEmpty() && record.attributesData != "{}") {
                                                val attributesMap = try {
                                                    val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                                                    com.google.gson.Gson().fromJson<Map<String, String>>(record.attributesData, type)
                                                } catch (e: Exception) {
                                                    emptyMap()
                                                }

                                                if (attributesMap.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        attributesMap.forEach { (key, value) ->
                                                            SuggestionChip(
                                                                onClick = {},
                                                                label = { Text("$key: $value") },
                                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
    }
    }
        }
    }
}

// Helper to format timestamp
private fun formatTimestamp(ts: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(ts))
}

// Helper to calculate trend (similar to VM logic but local)
private fun calculateEventTrend(records: List<com.example.myapplication.data.entity.EventRecordEntity>): List<Pair<String, Int>> {
    val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
    val trendMap = mutableMapOf<String, Int>()
    val tempCalendar = Calendar.getInstance()
    
    // Initialize last 7 days with 0
    for (i in 6 downTo 0) {
        tempCalendar.timeInMillis = System.currentTimeMillis()
        tempCalendar.add(Calendar.DAY_OF_YEAR, -i)
        val dayStr = dateFormat.format(tempCalendar.time)
        trendMap[dayStr] = 0
    }
    
    // Fill data
    records.forEach { record ->
        val recordDate = dateFormat.format(Date(record.timestamp))
        if (trendMap.containsKey(recordDate)) {
            trendMap[recordDate] = trendMap[recordDate]!! + 1
        }
    }
    
    return trendMap.entries.map { it.key to it.value }.sortedBy { it.first }
}

@Composable
fun StreakCard(currentStreak: Int, maxStreak: Int) {
    // Animate numbers
    val animatedCurrent by androidx.compose.animation.core.animateIntAsState(
        targetValue = currentStreak,
        animationSpec = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "currentStreak"
    )
    val animatedMax by androidx.compose.animation.core.animateIntAsState(
        targetValue = maxStreak,
        animationSpec = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "maxStreak"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Current Streak
        Card(
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)) // Light Orange
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("当前连续", style = MaterialTheme.typography.labelMedium, color = Color(0xFFEF6C00))
                Text("$animatedCurrent 天", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFEF6C00), fontWeight = FontWeight.Bold)
            }
        }
        
        // Max Streak
        Card(
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)) // Light Blue
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("历史最高", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1565C0))
                Text("$animatedMax 天", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HeatMapCalendar(heatData: Map<String, Int>) {
    // 简单热力图：展示过去 4 周 (28天) 的格子
    // 7 行 (周一到周日) x 4 列 (周)
    val days = 28
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            repeat(4) { week ->
                 // Column for each week
                 Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                     repeat(7) { day ->
                         // Placeholder
                     }
                 }
            }
        }
        
        // Grid Layout Implementation
        // We use a simple flowrow or just rows
        val today = Calendar.getInstance()
        val dayList = (0 until 28).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -(27 - i))
            val dateStr = sdf.format(cal.time)
            val count = heatData[dateStr] ?: 0
            dateStr to count
        }
        
        // Animation visibility
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Split into 4 chunks (weeks)
            dayList.chunked(7).forEachIndexed { weekIndex, weekDays ->
                 Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                     weekDays.forEachIndexed { dayIndex, (date, count) ->
                         val alpha by androidx.compose.animation.core.animateFloatAsState(
                             targetValue = if (isVisible) 1f else 0f,
                             animationSpec = androidx.compose.animation.core.tween(
                                 durationMillis = 500,
                                 delayMillis = (weekIndex * 7 + dayIndex) * 20 
                             ),
                             label = "cellAlpha"
                         )
                         
                         Box(
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { this.alpha = alpha }
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        count == 0 -> Color.LightGray.copy(alpha = 0.3f)
                                        count == 1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        count < 4 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                         )
                     }
                 }
            }
        }
    }
}

// Logic to calculate streaks
private fun calculateStreaks(records: List<com.example.myapplication.data.entity.EventRecordEntity>): Pair<Int, Int> {
    if (records.isEmpty()) return 0 to 0
    
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dates = records.map { sdf.format(Date(it.timestamp)) }.distinct().sortedDescending()
    
    var currentStreak = 0
    var maxStreak = 0
    var tempStreak = 0
    
    // Check if performed today or yesterday to start current streak
    val today = sdf.format(Date())
    val yesterday = sdf.format(Date(System.currentTimeMillis() - 86400000))
    
    val hasToday = dates.contains(today)
    val hasYesterday = dates.contains(yesterday)
    
    if (!hasToday && !hasYesterday) {
        currentStreak = 0
    } else {
        // Iterate to find continuity
        // Needs a robust calendar loop, but for simple logic:
        var streakEnd = if (hasToday) 0 else 1 // Index to start checking continuity
        // Implementation detail: Convert to Calendar days to be safe against month boundaries
    }
    
    // Simplest robust streak logic:
    // 1. Convert all unique dates to Epoch Days
    val days = records.map { 
        it.timestamp / (1000 * 60 * 60 * 24) 
    }.distinct().sorted() // Ascending
    
    if (days.isEmpty()) return 0 to 0
    
    // Calculate Max
    var max = 1
    var current = 1
    for (i in 0 until days.size - 1) {
        if (days[i + 1] == days[i] + 1) {
            current++
        } else {
            max = maxOf(max, current)
            current = 1
        }
    }
    max = maxOf(max, current)
    
    // Calculate Current
    val todayDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
    val lastRecordDay = days.last()
    
    var activeStreak = 0
    if (lastRecordDay == todayDay || lastRecordDay == todayDay - 1) {
        // Trace back
        activeStreak = 1
        for (i in days.size - 1 downTo 1) {
            if (days[i] == days[i-1] + 1) {
                activeStreak++
            } else {
                break
            }
        }
    }
    
    return activeStreak to max
}

private fun getHeatMapData(records: List<com.example.myapplication.data.entity.EventRecordEntity>): Map<String, Int> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return records.groupingBy { sdf.format(Date(it.timestamp)) }.eachCount()
}
