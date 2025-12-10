package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.AttributeDefinition
import com.example.myapplication.model.AttributeType
import com.example.myapplication.model.Event
import com.example.myapplication.model.EventCategory
import com.example.myapplication.ui.theme.PrimaryLight
import java.util.UUID

/**
 * 自定义事件创建/编辑抽屉 (Custom Implementation)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEventSheet(
    initialEvent: Event? = null,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (Event) -> Unit
) {
    var eventName by remember { mutableStateOf(initialEvent?.name ?: "") }
    var selectedEmoji by remember { mutableStateOf(initialEvent?.icon ?: "Filled.Star") }
    var selectedColor by remember { mutableStateOf(initialEvent?.backgroundColor ?: PrimaryLight) }
    var selectedCategory by remember { mutableStateOf(initialEvent?.category ?: EventCategory.DAILY) }
    var groupName by remember { mutableStateOf(initialEvent?.groupName ?: "未选择") }
    var eventType by remember { mutableStateOf(initialEvent?.eventType ?: "DEFAULT") }
    var isQuickRecord by remember { mutableStateOf(initialEvent?.isQuickRecord ?: false) }
    var attributes by remember { mutableStateOf(initialEvent?.attributes ?: listOf<AttributeDefinition>()) }

    // Dialog States
    var showTypeParamsDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var selectedAttributeType by remember { mutableStateOf<AttributeType?>(null) }
    
    // TypeParams Dialog
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

    // Attribute/Config Sheet State
    val attributeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Config Dialog (Nested Sheet)
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

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val topPadding = screenHeight * 0.05f // 95% screen height (unified)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = topPadding),
        containerColor = Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = true,
            isFocusable = true,
            securePolicy = SecureFlagPolicy.Inherit
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F7))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp)
            ) {
                 // Drag Handle (Custom)
                 Box(
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(vertical = 10.dp),
                     contentAlignment = Alignment.Center
                 ) {
                     Box(
                         modifier = Modifier
                             .width(32.dp)
                             .height(4.dp)
                             .background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                     )
                 }
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "取消", 
                    modifier = Modifier.clickable { onDismiss() },
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Text(
                    if (initialEvent == null) "新建事件" else "编辑事件", 
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    if (initialEvent == null) "新建" else "保存", 
                    modifier = Modifier.clickable { 
                        if (eventName.isNotBlank()) {
                            val newEvent = Event(
                                id = initialEvent?.id ?: UUID.randomUUID().toString(),
                                name = eventName,
                                icon = selectedEmoji,
                                backgroundColor = selectedColor,
                                category = selectedCategory,
                                groupName = groupName,
                                eventType = eventType,
                                isQuickRecord = isQuickRecord,
                                attributes = attributes
                            )
                            onConfirm(newEvent)
                        }
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Identity Section (Icon & Name)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Picker Trigger
                        var showIconColorPicker by remember { mutableStateOf(false) }
                        
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(selectedColor.copy(alpha = 0.2f))
                                .clickable { showIconColorPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            val vector = IconLibrary.IconMap[selectedEmoji]
                            if (vector != null) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            } else {
                                Text(selectedEmoji, fontSize = 32.sp)
                            }
                        }
                        
                        // Icon & Color Picker Sheet
                        if (showIconColorPicker) {
                            val iconColorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                            IconColorPickerSheet(
                                selectedIcon = selectedEmoji,
                                selectedColor = selectedColor,
                                sheetState = iconColorSheetState,
                                onDismiss = { showIconColorPicker = false },
                                onIconSelected = { selectedEmoji = it },
                                onColorSelected = { selectedColor = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Name Input
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (eventName.isEmpty()) {
                                Text("事件名称", color = Color.LightGray)
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = eventName,
                                onValueChange = { eventName = it },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black, fontSize = 16.sp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                

                // Basic Info
                item {
                    Text("基本", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
                    GroupContainer {
                        // Group
                        ConfigRow(
                            label = "所属分组",
                            value = groupName,
                            onClick = { /* TODO: Open Group Picker */ }
                        )
                        HorizontalDivider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        
                        // Event Type
                        ConfigRow(
                            label = "事件类型",
                            value = if (eventType == "DEFAULT") "默认" else "时间段",
                            onClick = { 
                                eventType = if (eventType == "DEFAULT") "DURATION" else "DEFAULT"
                            },
                            showInfo = true
                        )
                        HorizontalDivider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        
                        // Quick Record
                        SwitchRow(
                            label = "快速记录",
                            checked = isQuickRecord,
                            onCheckedChange = { isQuickRecord = it },
                            showInfo = true
                        )
                    }
                }
                
                // Attributes
                item {
                    Text("属性", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
                    GroupContainer {
                        if (attributes.isNotEmpty()) {
                            attributes.forEachIndexed { index, attr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(getAttributeIcon(attr.type), null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(attr.name, modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { attributes = attributes.toMutableList().apply { removeAt(index) } },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                                    }
                                }
                                HorizontalDivider(Modifier.padding(start = 56.dp), color = Color.LightGray.copy(alpha = 0.3f))
                            }
                        }
                        
                        // Add Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTypeParamsDialog = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                             Spacer(modifier = Modifier.width(4.dp))
                             Text("新增", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                // Reminders
                item {
                     Text("定期提醒", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
                     GroupContainer {
                          SwitchRow(label = "定期提醒", checked = false, onCheckedChange = {}, showInfo = true)
                     }
                }
                
                 item {
                     Text("间隔提醒", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
                     GroupContainer {
                          SwitchRow(label = "间隔提醒", checked = false, onCheckedChange = {}, showInfo = true)
                     }
                }
            }
        } // End of Column
        } // End of Box
    } // End of ModalBottomSheet
}

@Composable
fun ConfigRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    showInfo: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
         Row(verticalAlignment = Alignment.CenterVertically) {
             Text(label, fontSize = 16.sp)
             if (showInfo) {
                 Spacer(Modifier.width(4.dp))
                 Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
             }
         }
         
         Row(verticalAlignment = Alignment.CenterVertically) {
             Text(value, fontSize = 16.sp, color = if (value == "未选择") Color.Gray else MaterialTheme.colorScheme.primary)
             Spacer(Modifier.width(4.dp))
             Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f))
         }
    }
}
