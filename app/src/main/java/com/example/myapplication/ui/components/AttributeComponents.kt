package com.example.myapplication.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.ui.window.SecureFlagPolicy
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
import com.example.myapplication.model.AttributeOption
import com.example.myapplication.model.AttributeType
import androidx.compose.ui.window.Dialog

/**
 * iOS-Style Bottom Sheet for Attribute Configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeConfigSheet(
    type: AttributeType,
    sheetState: SheetState, // Hoisted state
    onDismiss: () -> Unit,
    onConfirm: (AttributeDefinition) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isRequired by remember { mutableStateOf(false) }
    var defaultValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") } // For Number
    
    // For Options
    var options by remember { mutableStateOf(listOf<AttributeOption>()) }
    var showAddOptionDialog by remember { mutableStateOf(false) }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val topPadding = screenHeight * 0.05f // 95% screen height (unified)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = topPadding),
        containerColor = Color(0xFFF2F2F7),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = true,
            isFocusable = true,
            securePolicy = SecureFlagPolicy.Inherit
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp) // Safety padding
        ) {
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
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Text(
                    "新建属性", 
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    "新建", 
                    modifier = Modifier.clickable { 
                        if (name.isNotBlank()) {
                            onConfirm(
                                AttributeDefinition(
                                    name = name,
                                    type = type,
                                    description = description,
                                    isRequired = isRequired,
                                    unit = if (type == AttributeType.NUMBER) unit else null,
                                    defaultValue = defaultValue.ifBlank { null },
                                    options = options
                                )
                            )
                        }
                    },
                    color = if (name.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    
                )
            }
            
            // Content Scroll
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Type specific header
                item {
                    Text(type.displayName, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }

                // Base Info Group
                item {
                    GroupContainer {
                        InputRow(label = "名称*", value = name, onValueChange = { name = it }, placeholder = "名称, 例如心情")
                        HorizontalDivider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        InputRow(label = "描述", value = description, onValueChange = { description = it }, placeholder = "链接或者文本, 可为空")
                        
                        // Default Value Logic varies by Type
                        if (type != AttributeType.SINGLE_SELECT && type != AttributeType.MULTI_SELECT) {
                             HorizontalDivider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                             InputRow(label = "默认值", value = defaultValue, onValueChange = { defaultValue = it }, placeholder = "默认值")
                        }
                        
                        HorizontalDivider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        SwitchRow(label = "必填", checked = isRequired, onCheckedChange = { isRequired = it })
                    }
                }
                
                // Number Specific: Unit
                if (type == AttributeType.NUMBER) {
                    item {
                        GroupContainer {
                            InputRow(label = "单位*", value = unit, onValueChange = { unit = it }, placeholder = "单位, 例如元/杯")
                        }
                    }
                }
                
                // Options Group
                if (type == AttributeType.SINGLE_SELECT || type == AttributeType.MULTI_SELECT) {
                    item {
                        Text("选项", style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                        GroupContainer {
                           options.forEachIndexed { index, option ->
                               SwipeableOptionRow(
                                   option = option,
                                   onDelete = {
                                       options = options.toMutableList().apply { removeAt(index) }
                                   }
                               )
                               HorizontalDivider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                           }
                           
                           // Add Button
                           Box(
                               modifier = Modifier
                                   .fillMaxWidth()
                                   .clickable { showAddOptionDialog = true }
                                   .padding(16.dp),
                               contentAlignment = Alignment.Center
                           ) {
                               Row(verticalAlignment = Alignment.CenterVertically) {
                                   Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                   Spacer(Modifier.width(4.dp))
                                   Text("新增", color = MaterialTheme.colorScheme.primary)
                               }
                           }
                        }
                    }
                }
            }
        }
    }
    
    if (showAddOptionDialog) {
        NewOptionDialog(
            onDismiss = { showAddOptionDialog = false },
            onConfirm = { label, color ->
                options = options + AttributeOption(label, color)
                showAddOptionDialog = false
            }
        )
    }
}

@Composable
fun GroupContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        content()
    }
}

@Composable
fun InputRow(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    placeholder: String,
    singleLine: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label.isNotEmpty()) {
            Text(label, modifier = Modifier.width(80.dp), fontWeight = FontWeight.Medium)
        }
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = Color.LightGray)
            }
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 3
            )
        }
    }
}

@Composable
fun SwitchRow(
    label: String, 
    checked: Boolean, 
    onCheckedChange: (Boolean) -> Unit,
    showInfo: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontWeight = FontWeight.Medium)
            if (showInfo) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Info, 
                    contentDescription = null, 
                    tint = Color.Gray, 
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SwipeableOptionRow(
    option: AttributeOption,
    onDelete: () -> Unit
) {
    // Simplified Swipe Logic (Just display delete button for simplicity or custom implementation)
    // Note: True swipe-to-delete is complex in standard Compose without SwipeToDismiss Box.
    // I will implement a Row with a Delete button on the left (minus icon in red circle) like iOS edit mode
    // as shown in Image 3 (Red minus icon on left).
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Delete Indicator
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
             Icon(
                 Icons.Default.RemoveCircle, 
                 null, 
                 tint = Color.Red
             )
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Color Dot
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(option.color))
        )
        
        Spacer(Modifier.width(12.dp))
        
        // Name
        Text(option.label, modifier = Modifier.weight(1f))
        
        // Reorder Icon
        Icon(Icons.Default.Menu, null, tint = Color.LightGray)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewOptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFFEF5350)) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
             shape = RoundedCornerShape(16.dp),
             color = Color.White,
             modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("取消", color = Color.Gray, modifier = Modifier.clickable { onDismiss() })
                    Text("新建选项", fontWeight = FontWeight.Bold)
                    Text(
                        "新建", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { 
                            if (text.isNotBlank()) onConfirm(text, selectedColor.value.toInt())
                        }
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Input
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("名称, 例如开心") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Color Picker
                ColorPicker(selectedColor) { selectedColor = it }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttributeTypeSelectionDialog(
    onDismiss: () -> Unit,
    onTypeSelected: (AttributeType) -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val topPadding = screenHeight * 0.05f // 95% screen height (unified)
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.padding(top = topPadding),
        containerColor = Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = true,
            isFocusable = true,
            securePolicy = SecureFlagPolicy.Inherit
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("请您选择属性类型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            AttributeType.entries.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTypeSelected(type) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(getAttributeIcon(type), null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(type.displayName, fontWeight = FontWeight.Bold)
                        Text(getAttributeDescription(type), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

fun getAttributeIcon(type: AttributeType): ImageVector {
    return when(type) {
        AttributeType.NUMBER -> Icons.Default.Tag
        AttributeType.SINGLE_SELECT -> Icons.Default.RadioButtonChecked
        AttributeType.MULTI_SELECT -> Icons.AutoMirrored.Filled.List
        AttributeType.TEXT -> Icons.AutoMirrored.Filled.ShortText // Short
        AttributeType.LONG_TEXT -> Icons.AutoMirrored.Filled.Notes
        AttributeType.SWITCH -> Icons.Default.ToggleOn
        AttributeType.RATING -> Icons.Default.Star
    }
}

fun getAttributeDescription(type: AttributeType): String {
    return when(type) {
        AttributeType.NUMBER -> "可以用来追踪价格、重量等数值"
        AttributeType.SINGLE_SELECT -> "从预设的选项中单选"
        AttributeType.MULTI_SELECT -> "从预设的选项中多选"
        AttributeType.TEXT -> "短文本，支持输入提示"
        AttributeType.LONG_TEXT -> "长文本备注"
        AttributeType.SWITCH -> "轻松设置为是或否"
        AttributeType.RATING -> "用于给美食、电影评分"
    }
}

@Composable
fun EmojiPicker(selectedEmoji: String, onEmojiSelected: (String) -> Unit) {
    val emojis = listOf(
        "⭐", "❤️", "🎯", "🔥", "💡", "📝", "💰", "📚",
        "🏃", "🧘", "💧", "🏥", "💊", "😴", "⚖️", "📈",
        "🏦", "🧾", "💻", "🔤", "✍️", "🎬", "🎮", "🎵"
    )
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 44.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.heightIn(max = 240.dp)
    ) {
        items(emojis) { emoji ->
            val isSelected = emoji == selectedEmoji
            val scale by animateFloatAsState(if (isSelected) 1.1f else 1f, label = "scale")
            
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .scale(scale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun ColorPicker(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    val colors = listOf(
        Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC),
        Color(0xFF7E57C2), Color(0xFF5C6BC0), Color(0xFF42A5F5),
        Color(0xFF29B6F6), Color(0xFF26A69A), Color(0xFF66BB6A),
        Color(0xFF9CCC65), Color(0xFFFFCA28), Color(0xFFFFE082),
        Color(0xFFFF8A65), Color(0xFF8D6E63), Color(0xFF78909C)
    )
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.heightIn(max = 240.dp)
    ) {
        items(colors) { color ->
            val isSelected = color == selectedColor
            val scale by animateFloatAsState(if (isSelected) 1.1f else 1f, label = "scale")
            
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .border(
                        if (isSelected) 2.dp else 0.dp,
                        MaterialTheme.colorScheme.onSurface,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, null, tint = Color.White)
                }
            }
        }
    }
}
