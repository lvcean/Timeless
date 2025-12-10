package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun IconPicker(
    selectedIconName: String,
    onIconSelected: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("健康") }
    val categories = IconLibrary.AllCategories.keys.toList()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Categories Tabs
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Icons Grid
        val icons = IconLibrary.AllCategories[selectedCategory] ?: emptyList()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 64.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(), // Fill remaining space instead of fixed height
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(icons) { iconModel ->
                val isSelected = iconModel.name == selectedIconName
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                        .clickable { onIconSelected(iconModel.name) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconModel.icon, 
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
