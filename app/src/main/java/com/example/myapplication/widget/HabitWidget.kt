package com.example.myapplication.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.repository.EventRepository
import com.example.myapplication.data.entity.EventRecordEntity
import com.example.myapplication.model.Event
import kotlinx.coroutines.flow.map

class HabitWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val repository = EventRepository(
            database.eventDao(), 
            database.eventRecordDao(), 
            database.reminderDao()
        )
        
        provideContent {
            GlanceTheme {
                val events by repository.getAllEvents().collectAsState(initial = emptyList())
                val todayRecords by repository.getAllRecords().map { records ->
                    val now = java.util.Calendar.getInstance()
                    val todayStart = now.apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    val todayEnd = todayStart + java.util.concurrent.TimeUnit.DAYS.toMillis(1)
                    
                    records.filter { it.timestamp in todayStart until todayEnd }
                }.collectAsState(initial = emptyList())

                val completedIds = todayRecords.map { record -> record.eventId }.toSet()
                WidgetContent(events, completedIds)
            }
        }
    }

    @Composable
    fun WidgetContent(events: List<Event>, completedEventIds: Set<String>) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日习惯",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        text = "${completedEventIds.size}/${events.size}",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(GlanceModifier.height(8.dp))
                
                if (events.isEmpty()) {
                     Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无习惯",
                            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = GlanceModifier.fillMaxSize()
                    ) {
                        items(events) { event ->
                            val isCompleted = completedEventIds.contains(event.id)
                            HabitItem(event, isCompleted)
                            Spacer(GlanceModifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun HabitItem(event: Event, isCompleted: Boolean) {
        val backgroundColor = if (isCompleted) GlanceTheme.colors.primaryContainer else GlanceTheme.colors.surfaceVariant
        val contentColor = if (isCompleted) GlanceTheme.colors.onPrimaryContainer else GlanceTheme.colors.onSurfaceVariant
        
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(12.dp)
                .clickable(actionRunCallback<OpenAppActionCallback>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = event.icon, style = TextStyle(fontSize = 18.sp))
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = event.name,
                style = TextStyle(
                    color = contentColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                ),
                modifier = GlanceModifier.defaultWeight()
            )
            if (isCompleted) {
               // Checkmark
                Text(text = "✓", style = TextStyle(color = contentColor, fontWeight = FontWeight.Bold))
            }
        }
    }
}

class OpenAppActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        context?.startActivity(intent)
    }
}
