package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.EventCategory
import com.example.myapplication.model.PresetEvent
import com.example.myapplication.model.PresetEvents
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * 添加事件底部弹窗组件 (Redesigned List Style)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onEventSelected: (PresetEvent) -> Unit,
    onCustomEvent: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(EventCategory.ALL) }
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val topPadding = screenHeight * 0.05f // 95% screen height (unified)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier.padding(top = topPadding),
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
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "新建事件",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "没有想法？从选取一些预设事件开始吧",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "关闭", tint = Color.Gray)
                }
            }
            
            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = EventCategory.entries.indexOf(selectedCategory),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                EventCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = category.displayName,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Preset List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(PresetEvents.getByCategory(selectedCategory)) { preset ->
                    PresetListItem(
                        presetEvent = preset,
                        onClick = {
                            onEventSelected(preset)
                            // Don't dismiss here - let parent handle the flow
                        }
                    )
                }
            }
            
            // Footer Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onCustomEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("自定义事件", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "从模版导入",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { /* TODO: Import Template */ }
                )
            }
        }
    }
}

@Composable
fun PresetListItem(
    presetEvent: PresetEvent,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(presetEvent.backgroundColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val vector = com.example.myapplication.ui.components.IconLibrary.IconMap[presetEvent.icon]
                if (vector != null) {
                    Icon(
                        imageVector = vector,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = presetEvent.backgroundColor
                    )
                } else {
                    Text(presetEvent.icon, fontSize = 20.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = presetEvent.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddEventBottomSheetPreview2() {
    MyApplicationTheme {
        val sheetState = rememberModalBottomSheetState()
        AddEventBottomSheet(sheetState = sheetState, onDismiss = {}, onEventSelected = {})
    }
}
