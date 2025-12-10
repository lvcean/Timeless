package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.model.AttributeDefinition
import com.example.myapplication.model.AttributeOption
import com.example.myapplication.model.AttributeType
import com.example.myapplication.model.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordSheet(
    event: Event,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, Map<String, String>) -> Unit,
    onUpdateAttribute: (AttributeDefinition) -> Unit,
    initialTimestamp: Long? = null
) {
    var note by remember { mutableStateOf("") }
    var selectedDate by remember(initialTimestamp) { 
        mutableStateOf(Calendar.getInstance().apply {
            if (initialTimestamp != null) {
                timeInMillis = initialTimestamp
            }
        }) 
    }
    val attributeValues = remember { mutableStateMapOf<String, String>() }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    
    // Initialize default values
    LaunchedEffect(event) {
        event.attributes.forEach { attr ->
            if (attr.defaultValue != null) {
                attributeValues[attr.id] = attr.defaultValue
            }
        }
    }

    val datePickerDialog = android.app.DatePickerDialog(
        androidx.compose.ui.platform.LocalContext.current,
        { _, year, month, day ->
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, month)
            selectedDate.set(Calendar.DAY_OF_MONTH, day)
        },
        selectedDate.get(Calendar.YEAR),
        selectedDate.get(Calendar.MONTH),
        selectedDate.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = android.app.TimePickerDialog(
        androidx.compose.ui.platform.LocalContext.current,
        { _, hour, minute ->
            selectedDate.set(Calendar.HOUR_OF_DAY, hour)
            selectedDate.set(Calendar.MINUTE, minute)
        },
        selectedDate.get(Calendar.HOUR_OF_DAY),
        selectedDate.get(Calendar.MINUTE),
        true
    )

    
    // Info Dialog State
    var showInfoForAttribute by remember { mutableStateOf<AttributeDefinition?>(null) }
    
    // Add Option Dialog State
    var showAddOptionForAttribute by remember { mutableStateOf<AttributeDefinition?>(null) }

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
                .padding(bottom = 32.dp)
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
                    "记录 ${event.name}", 
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    "保存", 
                    modifier = Modifier.clickable { 
                        onConfirm(note, selectedDate.timeInMillis, attributeValues)
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
                // Time & Note Section
                item {
                    GroupContainer {
                        // Time Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() } // Click whole row for date
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("时间", fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    dateFormat.format(selectedDate.time),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                // Separate Time Click if needed, but whole row is easier
                                Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(16.dp).clickable { timePickerDialog.show() }) 
                            }
                        }
                        
                        Divider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        
                        // Note Row
                        InputRow(
                            label = "备注", 
                            value = note, 
                            onValueChange = { note = it }, 
                            placeholder = "写点什么...",
                            singleLine = false // Allow multi-line for note
                        )
                    }
                }

                // Attributes Section
                event.attributes.forEach { attr ->
                    item {
                        Column {
                            // Label with Info Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            ) {
                                Text(
                                    attr.name, 
                                    style = MaterialTheme.typography.labelLarge, 
                                    color = Color.Gray
                                )
                                if (attr.description.isNotBlank()) {
                                    Spacer(Modifier.width(6.dp))
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = "信息",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { showInfoForAttribute = attr }
                                    )
                                }
                            }

                            GroupContainer {
                                when (attr.type) {
                                    AttributeType.NUMBER -> {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            androidx.compose.foundation.text.BasicTextField(
                                                value = attributeValues[attr.id] ?: "",
                                                onValueChange = { 
                                                     if (it.all { char -> char.isDigit() || char == '.' }) {
                                                         attributeValues[attr.id] = it 
                                                     }
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                textStyle = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f),
                                                decorationBox = { innerTextField ->
                                                    Box {
                                                        if ((attributeValues[attr.id] ?: "").isEmpty()) {
                                                            Text("0", color = Color.LightGray)
                                                        }
                                                        innerTextField()
                                                    }
                                                }
                                            )
                                            if (attr.unit != null) {
                                                Text(attr.unit, color = Color.Gray)
                                            }
                                        }
                                    }
                                    AttributeType.TEXT, AttributeType.LONG_TEXT -> {
                                        InputRow(
                                            label = "", // No label inside
                                            value = attributeValues[attr.id] ?: "",
                                            onValueChange = { attributeValues[attr.id] = it },
                                            placeholder = "输入${attr.name}",
                                            singleLine = attr.type == AttributeType.TEXT
                                        )
                                    }
                                    AttributeType.SWITCH -> {
                                        YesNoSelector(
                                            selected = attributeValues[attr.id] == "true",
                                            onSelectionChange = { attributeValues[attr.id] = it.toString() }
                                        )
                                    }
                                    AttributeType.RATING -> {
                                        StarRatingInput(
                                            rating = (attributeValues[attr.id]?.toIntOrNull() ?: 0),
                                            onRatingChange = { attributeValues[attr.id] = it.toString() }
                                        )
                                    }
                                    AttributeType.SINGLE_SELECT -> {
                                        // Custom Selection Layout
                                        Column {
                                            attr.options.forEachIndexed { index, option ->
                                                val isSelected = attributeValues[attr.id] == option.label
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { attributeValues[attr.id] = option.label }
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(option.color)))
                                                        Spacer(Modifier.width(12.dp))
                                                        Text(option.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                    }
                                                    if (isSelected) {
                                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                if (index < attr.options.size - 1) {
                                                    Divider(Modifier.padding(start = 40.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                                }
                                            }
                                            // Add Option Button
                                            Divider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showAddOptionForAttribute = attr }
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("添加选项...", color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                    AttributeType.MULTI_SELECT -> {
                                        Column {
                                            val selectedSet = (attributeValues[attr.id] ?: "").split(",").filter { it.isNotBlank() }.toSet()
                                            
                                            attr.options.forEachIndexed { index, option ->
                                                val isSelected = selectedSet.contains(option.label)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { 
                                                            val newSet = if (isSelected) selectedSet - option.label else selectedSet + option.label
                                                            attributeValues[attr.id] = newSet.joinToString(",")
                                                        }
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(option.color)))
                                                        Spacer(Modifier.width(12.dp))
                                                        Text(option.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                    }
                                                    if (isSelected) {
                                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                                if (index < attr.options.size - 1) {
                                                    Divider(Modifier.padding(start = 40.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                                }
                                            }
                                            // Add Option Button
                                            Divider(Modifier.padding(start = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { showAddOptionForAttribute = attr }
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("添加选项...", color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Info Dialog
    if (showInfoForAttribute != null) {
        AlertDialog(
            onDismissRequest = { showInfoForAttribute = null },
            title = { Text(showInfoForAttribute!!.name) },
            text = { Text(showInfoForAttribute!!.description) },
            confirmButton = {
                TextButton(onClick = { showInfoForAttribute = null }) { Text("我知道了") }
            }
        )
    }
    
    // Add Option Dialog
    if (showAddOptionForAttribute != null) {
        NewOptionDialog(
            onDismiss = { showAddOptionForAttribute = null },
            onConfirm = { label, color ->
                // Update local state is not enough, we need to update the definition passed in.
                // Since 'event' is immutable data class, we can't modify it directly in place effectively for the UI to update unless we trigger a recomposition with new event.
                // However, we can callback to existing update mechanism.
                val updatedAttr = showAddOptionForAttribute!!.copy(
                    options = showAddOptionForAttribute!!.options + AttributeOption(label, color)
                )
                onUpdateAttribute(updatedAttr)
                // Note: The parent needs to update the event state for this sheet to reflect the new option immediately.
                showAddOptionForAttribute = null
            }
        )
    }
}

@Composable
fun YesNoSelector(
    selected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp) // Outer padding 
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE5E5EA)) // Track color
            .padding(2.dp), // Padding for inner items
    ) {
        // YES Button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) Color.White else Color.Transparent)
                .clickable { onSelectionChange(true) }
                .then(if (selected) Modifier.border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp)) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text("是", fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) Color.Black else Color.Gray)
        }
        
        // NO Button
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(if (!selected) Color.White else Color.Transparent)
                .clickable { onSelectionChange(false) }
                .then(if (!selected) Modifier.border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp)) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text("否", fontWeight = if (!selected) FontWeight.Bold else FontWeight.Medium, color = if (!selected) Color.Black else Color.Gray)
        }
    }
}

@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { index ->
            Icon(
                imageVector = if (index <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "$index 星",
                tint = if (index <= rating) Color(0xFFFFB300) else Color.LightGray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChange(index) }
                    .padding(4.dp)
            )
        }
    }
}

// Reusing InputRow generic helper but need ensure singleLine support in original if used
// Assuming InputRow in AttributeComponents.kt might need update or we define local override if signature matches?
// Actually InputRow is in AttributeComponents.kt. Let's make sure it supports customization or copy what we need.
// The InputRow in AttributeComponents.kt takes specific params. Let's just overload or use BasicTextField here for custom look.
// I used InputRow above, let's verify its signature in AttributeComponents.kt or check if I need to update it.
// Previous view showed: fun InputRow(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String)
// It doesn't have singleLine param. I should probably update `AttributeComponents.kt` to make it more flexible or just inline here.
// To keep it clean, I will inline a simple Row here or update common one. 
// Given the task, I'll update the common InputRow to support singleLine and reuse it.


