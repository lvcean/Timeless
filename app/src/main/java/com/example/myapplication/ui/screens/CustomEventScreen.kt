package com.example.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.AttributeDefinition
import com.example.myapplication.model.AttributeType
import com.example.myapplication.model.Event
import com.example.myapplication.model.EventCategory
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.PrimaryLight
import com.example.myapplication.viewmodel.EventViewModel
import java.util.UUID

/**
 * 自定义事件创建页面
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomEventScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit,
    eventId: String? = null
) {
    var eventName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("⭐") }
    var selectedColor by remember { mutableStateOf(PrimaryLight) }
    var selectedCategory by remember { mutableStateOf(EventCategory.DAILY) }
    var isQuickRecord by remember { mutableStateOf(false) }
    var attributes by remember { mutableStateOf(listOf<AttributeDefinition>()) }
    
    // Dialog States
    var showTypeParamsDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var selectedAttributeType by remember { mutableStateOf<AttributeType?>(null) }
    
    // Load event data...
    LaunchedEffect(eventId) {
        if (eventId != null) {
            val event = viewModel.getEventById(eventId)
            if (event != null) {
                eventName = event.name
                selectedEmoji = event.icon
                selectedColor = event.backgroundColor
                selectedCategory = event.category
                isQuickRecord = event.isQuickRecord
                attributes = event.attributes
            }
        }
    }
    
    if (showTypeParamsDialog) {
        AttributeTypeSelectionDialog(
            onDismiss = { showTypeParamsDialog = false },
            onTypeSelected = { type ->
                selectedAttributeType = type
                showTypeParamsDialog = false
                showConfigDialog = true
            }
        )
    }

    val attributeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showConfigDialog && selectedAttributeType != null) {
        AttributeConfigSheet(
            type = selectedAttributeType!!,
            sheetState = attributeSheetState,
            onDismiss = { showConfigDialog = false },
            onConfirm = { definition ->
                attributes = attributes + definition
                showConfigDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (eventId != null) "编辑事件" else "新建事件",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("取消", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (eventName.isNotBlank()) {
                                val event = Event(
                                    id = eventId ?: UUID.randomUUID().toString(),
                                    name = eventName,
                                    icon = selectedEmoji,
                                    backgroundColor = selectedColor,
                                    category = selectedCategory,
                                    isQuickRecord = isQuickRecord,
                                    attributes = attributes
                                )
                                if (eventId != null) viewModel.updateEvent(event) else viewModel.addEvent(event)
                                onNavigateBack()
                            }
                        },
                        enabled = eventName.isNotBlank()
                    ) {
                        Text("保存", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }

        AnimatedVisibility(
            visible = isVisible,
            enter = androidx.compose.animation.slideInVertically { 100 } + androidx.compose.animation.fadeIn()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
            item {
                // Top Icon & Name Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(selectedColor.copy(alpha = 0.2f))
                            .border(1.dp, selectedColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedEmoji, fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = eventName,
                        onValueChange = { eventName = it },
                        placeholder = { Text("事件名称") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            item {
                Text(
                    "基本",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column {
                        // Category Selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* Logic to Expand Category Picker if needed, or use existing flow row below */ }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("所属分组", fontSize = 16.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    selectedCategory.displayName, 
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        // FlowRow Category Picker (Inline)
                         Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                             FlowRow(
                                 horizontalArrangement = Arrangement.spacedBy(8.dp),
                                 verticalArrangement = Arrangement.spacedBy(8.dp)
                             ) {
                                 EventCategory.entries.filter { it != EventCategory.ALL }.forEach { category ->
                                     FilterChip(
                                         selected = selectedCategory == category,
                                         onClick = { selectedCategory = category },
                                         label = { Text(category.displayName) },
                                         shape = CircleShape,
                                         colors = FilterChipDefaults.filterChipColors(
                                             selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                         )
                                     )
                                 }
                             }
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        // Quick Record Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("快速记录", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "info",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isQuickRecord,
                                onCheckedChange = { isQuickRecord = it }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "自定义属性",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { showTypeParamsDialog = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (attributes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { showTypeParamsDialog = true },
                            contentAlignment = Alignment.Center
                    ) {
                        Text("点击添加属性 (如: 金额, 心情)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column {
                            attributes.forEachIndexed { index, attr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        getAttributeIcon(attr.type),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(attr.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            attr.type.displayName, 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = {
                                        attributes = attributes.toMutableList().apply { removeAt(index) }
                                    }) {
                                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                                if (index < attributes.size - 1) {
                                    Divider(Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Text(
                    "图标 & 颜色",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Reuse existing pickers...
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(Modifier.padding(16.dp)) {
                         EmojiPicker(selectedEmoji) { selectedEmoji = it }
                         Spacer(Modifier.height(16.dp))
                         Divider()
                         Spacer(Modifier.height(16.dp))
                         ColorPicker(selectedColor) { selectedColor = it }
                    }
                }
            }
        }
    }
    }
}

