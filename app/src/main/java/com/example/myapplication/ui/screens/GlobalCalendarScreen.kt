package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.Event
import com.example.myapplication.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.myapplication.ui.components.AddRecordSheet
import com.example.myapplication.ui.components.IconLibrary
import com.example.myapplication.ui.components.bouncyClickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalCalendarScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit
) {
    val events by viewModel.events.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }

    var showEventSelector by remember { mutableStateOf(false) }
    var eventForBackfill by remember { mutableStateOf<Event?>(null) }
    val recordSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Prepare data for the grid
    val daysInMonth = remember(currentMonth, allRecords) {
        val days = mutableListOf<Calendar>()
        val cal = currentMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        
        // Backtrack to Sunday
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        cal.add(Calendar.DAY_OF_YEAR, -(firstDayOfWeek - 1))

        // 6 rows * 7 days = 42 cells
        repeat(42) {
            days.add(cal.clone() as Calendar)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    // Records for the selected date
    val selectedRecords = remember(selectedDate, allRecords) {
        allRecords.filter { record ->
            val recCal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
            recCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
            recCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
        }.sortedBy { it.timestamp }
    }

    // Helper to get dots for a day
    fun getDotsForDay(day: Calendar): List<Color> {
        val dayRecords = allRecords.filter { record ->
            val recCal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
            recCal.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
            recCal.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
        }
        return dayRecords.mapNotNull { record ->
            events.find { it.id == record.eventId }?.backgroundColor
        }.distinct().take(3)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("全局日历") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showEventSelector = true },
                icon = { Icon(Icons.Default.Add, "补打卡") },
                text = { Text("在此日补卡") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Month Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = currentMonth.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    currentMonth = newCal
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Month")
                }

                Text(
                    text = SimpleDateFormat("yyyy年 MM月", Locale.getDefault()).format(currentMonth.time),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    val newCal = currentMonth.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    currentMonth = newCal
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month")
                }
            }

            // Days of Week Header
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                daysInMonth.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            val isSelected = day.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                                           day.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
                            val isCurrentMonth = day.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
                            
                            // Calculate dots for this day (optimization: do this outside but for now simple)
                            val dots = remember(day, allRecords) {
                                val dayStart = day.clone() as Calendar
                                dayStart.set(Calendar.HOUR_OF_DAY, 0)
                                dayStart.set(Calendar.MINUTE, 0)
                                dayStart.set(Calendar.SECOND, 0)
                                dayStart.set(Calendar.MILLISECOND, 0)
                                val startMs = dayStart.timeInMillis
                                val endMs = startMs + 24 * 3600 * 1000
                                
                                val dayRecs = allRecords.filter { it.timestamp in startMs until endMs }
                                dayRecs.mapNotNull { rec -> events.find { it.id == rec.eventId }?.backgroundColor }.distinct().take(4)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .clickable { 
                                        selectedDate = day.clone() as Calendar
                                        if (!isCurrentMonth) {
                                            currentMonth = day.clone() as Calendar
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = day.get(Calendar.DAY_OF_MONTH).toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (dots.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.height(6.dp).padding(top = 2.dp)
                                        ) {
                                            dots.forEach { color ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .padding(horizontal = 0.5.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(top = 16.dp))
            
            // Selected Day List
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                 item {
                     val dateStr = SimpleDateFormat("MM月dd日 EEEE", Locale.getDefault()).format(selectedDate.time)
                     Text(
                         text = dateStr,
                         style = MaterialTheme.typography.titleMedium,
                         color = MaterialTheme.colorScheme.primary,
                         modifier = Modifier.padding(bottom = 8.dp)
                     )
                 }

                 if (selectedRecords.isEmpty()) {
                     item {
                         Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                             Text("这一天没有记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                         }
                     }
                 } else {
                     items(selectedRecords) { record ->
                         val event = events.find { it.id == record.eventId }
                         if (event != null) {
                             // Simple Record Item
                             Card(
                                 modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                 colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                             ) {
                                 Row(
                                     modifier = Modifier.padding(12.dp),
                                     verticalAlignment = Alignment.CenterVertically
                                 ) {
                                     // Dot
                                     Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(event.backgroundColor))
                                     
                                     Spacer(modifier = Modifier.width(12.dp))
                                     
                                     Column(modifier = Modifier.weight(1f)) {
                                         Text(event.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                         val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))
                                         Text(timeStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                     }
                                     
                                     if (record.note.isNotEmpty()) {
                                         Text(record.note, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                     }
                                 }
                             }
                         }
                     }
                 }
            }
        }
    }


    if (showEventSelector) {
        ModalBottomSheet(
            onDismissRequest = { showEventSelector = false }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "选择要补卡的事件",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                     items(events) { event ->
                         Row(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .clickable {
                                     eventForBackfill = event
                                     showEventSelector = false
                                 }
                                 .padding(vertical = 12.dp),
                             verticalAlignment = Alignment.CenterVertically
                         ) {
                             Box(
                                 modifier = Modifier
                                     .size(40.dp)
                                     .clip(RoundedCornerShape(12.dp))
                                     .background(event.backgroundColor.copy(alpha = 0.2f)),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     IconLibrary.getIcon(event.icon),
                                     contentDescription = null,
                                     tint = event.backgroundColor
                                 )
                             }
                             Spacer(modifier = Modifier.width(16.dp))
                             Text(event.name, style = MaterialTheme.typography.bodyLarge)
                         }
                     }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (eventForBackfill != null) {
        // Set timestamp to selected date (current time or 12:00)
        // Here we use selectedDate's date but current time if today, else 12:00
        val backfillTimestamp = remember(selectedDate) {
            val now = Calendar.getInstance()
            if (selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                now.timeInMillis
            } else {
                val c = selectedDate.clone() as Calendar
                c.set(Calendar.HOUR_OF_DAY, 12)
                c.set(Calendar.MINUTE, 0)
                c.timeInMillis
            }
        }

        AddRecordSheet(
            event = eventForBackfill!!,
            sheetState = recordSheetState,
            onDismiss = { eventForBackfill = null },
            initialTimestamp = backfillTimestamp,
            onConfirm = { note, timestamp, attributes ->
                viewModel.addRecord(
                    eventId = eventForBackfill!!.id,
                    note = note,
                    timestamp = timestamp,
                    attributes = attributes
                )
                eventForBackfill = null
            },
            onUpdateAttribute = { /* Updating definition in calendar view might not be needed or supported */ }
        )
    }
}
