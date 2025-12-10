package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Material 3 Floating Toolbar
 * Simulates a contextual action bar in a floating pill
 */
@Composable
fun FloatingToolbar(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .shadow(
                elevation = 6.dp, 
                shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.2f)
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.inverseSurface, // Dark background usually
        contentColor = MaterialTheme.colorScheme.inverseOnSurface
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit Action
            ToolbarAction(
                icon = Icons.Default.Edit,
                label = "编辑",
                onClick = onEdit
            )

            // Divider
            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.2f)
            )

            // Delete Action
            ToolbarAction(
                icon = Icons.Default.Delete,
                label = "删除",
                onClick = onDelete,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun ToolbarAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.inverseOnSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.inverseOnSurface
        )
    }
}

@Preview
@Composable
fun FloatingToolbarPreview() {
    FloatingToolbar(onEdit = {}, onDelete = {})
}
