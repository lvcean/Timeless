package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.entity.EventRecordEntity
import com.example.myapplication.model.Event
import com.example.myapplication.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: Event,
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    val records by viewModel.getRecordsByEventId(event.id).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    val totalCount = records.size
    val thisMonthCount = remember(records) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        records.count {
            val recordCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            recordCal.get(Calendar.MONTH) == currentMonth && recordCal.get(Calendar.YEAR) == currentYear
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = event.backgroundColor,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "添加记录")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    event.backgroundColor,
                                    event.backgroundColor.copy(alpha = 0.8f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White)
                            }
                            IconButton(onClick = onNavigateToReminders) {
                                Icon(Icons.Default.Notifications, "提醒", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = event.name,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-40).dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard(
                        title = "总次数",
                        value = totalCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    StatisticCard(
                        title = "本月打卡",
                        value = thisMonthCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "最近记录",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 16.dp)
                )
            }

            if (records.isEmpty()) {
                item {
                    EmptyRecordsState()
                }
            } else {
                items(records.sortedByDescending { it.timestamp }) { record ->
                    TimelineRecordItem(
                        record = record,
                        event = event,
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        com.example.myapplication.ui.components.AddRecordSheet(
            event = event,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismiss = { showAddDialog = false },
            onConfirm = { eventId, timestamp, attributesMap ->
                viewModel.addRecord(
                    eventId = eventId,
                    timestamp = timestamp,
                    attributes = attributesMap
                )
                showAddDialog = false
            },
            onUpdateAttribute = { }
        )
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun EmptyRecordsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无记录，快去打卡吧！",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun TimelineRecordItem(
    record: EventRecordEntity,
    event: Event,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = dateFormat.format(Date(record.timestamp))

    val attributesMap: Map<String, String> = try {
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
        gson.fromJson(record.attributesData, type) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(60.dp)
        ) {
            Text(
                text = timeText,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(event.backgroundColor)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (attributesMap.isNotEmpty()) {
                    attributesMap.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = value,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                } else {
                    Text(
                        text = "已打卡",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
