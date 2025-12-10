package com.example.myapplication.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.data.dao.EventDao
import com.example.myapplication.data.dao.EventRecordDao
import com.example.myapplication.data.dao.ReminderDao
import com.example.myapplication.data.entity.EventEntity
import com.example.myapplication.data.entity.EventRecordEntity
import com.example.myapplication.data.entity.ReminderEntity
import com.example.myapplication.model.Event
import com.example.myapplication.model.EventCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 事件仓库
 */
class EventRepository(
    private val eventDao: EventDao,
    private val recordDao: EventRecordDao,
    private val reminderDao: ReminderDao
) {
    // 获取所有事件
    fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { entities ->
            entities.map { it.toEvent() }
        }
    }
    
    // 获取特定分类的事件
    fun getEventsByCategory(category: EventCategory): Flow<List<Event>> {
        return eventDao.getEventsByCategory(category.name).map { entities ->
            entities.map { it.toEvent() }
        }
    }
    
    // 添加事件
    suspend fun addEvent(event: Event) {
        // 1. 先存本地
        eventDao.insertEvent(event.toEntity())
        
        // 2. 尝试上传到后端
        try {
            val networkEvent = com.example.myapplication.data.model.NetworkEvent(
                id = event.id,
                name = event.name,
                icon = event.icon,
                backgroundColor = String.format("#%06X", (0xFFFFFF and event.backgroundColor.toArgb())), // Standard 6-digit Hex
                category = event.category.name,
                creatorId = com.example.myapplication.data.api.RetrofitClient.userId
            )
            
            val response = com.example.myapplication.data.api.RetrofitClient.eventApi.createEvent(networkEvent)
            if (response.isSuccessful) {
                // Supabase returns a list of created items
                val createdEvents = response.body()
                if (!createdEvents.isNullOrEmpty()) {
                    println("Backend upload success: ${createdEvents[0].name}")
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown Error"
                println("Backend upload failed: ${response.code()} - $errorMsg")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Ignore failure for now, data is safe in local DB
        }
    }
    
    // 批量添加事件
    suspend fun addEvents(events: List<Event>) {
        eventDao.insertEvents(events.map { it.toEntity() })
    }
    
    // 更新事件
    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event.toEntity())
    }
    
    // 删除事件
    suspend fun deleteEvent(event: Event) {
        // 1. Delete from remote
        // 1. Delete from remote
        try {
            val id = event.id
            // Retrofit interface now expects just the value for @Query("id")
            // So we send "eq.UUID"
            val queryVal = "eq.$id"
            com.example.myapplication.data.api.RetrofitClient.eventApi.deleteEvent(queryVal)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // 2. Delete from local
        eventDao.deleteEvent(event.toEntity())
    }
    
    // 添加记录
    suspend fun addRecord(
        eventId: String, 
        note: String = "", 
        timestamp: Long = System.currentTimeMillis(),
        attributes: Map<String, String> = emptyMap()
    ): Long {
        val gson = Gson()
        val attributesData = gson.toJson(attributes)
        
        // 1. 本地插入
        val localId = recordDao.insertRecord(
            EventRecordEntity(
                eventId = eventId,
                timestamp = timestamp,
                note = note,
                attributesData = attributesData
            )
        )

        // 2. 远程上传 (Fire and forget, or handle error)
        try {
            val netRecord = com.example.myapplication.data.model.NetworkRecord(
                id = null, // Backend will generate ID
                eventId = eventId,
                timestamp = timestamp,
                note = note,
                attributesData = attributesData
            )
            com.example.myapplication.data.api.RetrofitClient.eventApi.createRecord(netRecord)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return localId
    }
    
    // 获取事件的所有记录
    fun getRecordsByEventId(eventId: String): Flow<List<EventRecordEntity>> {
        return recordDao.getRecordsByEventId(eventId)
    }

    // 获取所有记录
    fun getAllRecords(): Flow<List<EventRecordEntity>> {
        return recordDao.getAllRecords()
    }
    
    // 删除记录
    // 删除记录
    suspend fun deleteRecord(record: EventRecordEntity) {
        // Remote delete
        try {
             com.example.myapplication.data.api.RetrofitClient.eventApi.deleteRecord(
                 eventId = "eq.${record.eventId}",
                 timestamp = "eq.${record.timestamp}"
             )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        recordDao.deleteRecord(record)
    }
    
    // 获取记录统计
    suspend fun getRecordCount(eventId: String): Int {
        return recordDao.getRecordCountByEventId(eventId)
    }
    
    suspend fun getTotalRecordCount(): Int {
        return recordDao.getTotalRecordCount()
    }
    
    // 获取时间范围内的记录
    suspend fun getRecordsByTimeRange(startTime: Long, endTime: Long): List<EventRecordEntity> {
        return recordDao.getRecordsByTimeRange(startTime, endTime)
    }

    // 获取指定事件在时间范围内的打卡次数
    suspend fun getRecordCountInRange(eventId: String, startTime: Long, endTime: Long): Int {
        return recordDao.getRecordCountInRange(eventId, startTime, endTime)
    }

    // 获取指定事件在时间范围内的打卡天数（去重）
    suspend fun getRecordDaysInRange(eventId: String, startTime: Long, endTime: Long): Int {
        return recordDao.getRecordDaysInRange(eventId, startTime, endTime)
    }

    // 获取指定事件在时间范围内的所有打卡日期
    suspend fun getRecordDatesInRange(eventId: String, startTime: Long, endTime: Long): List<Long> {
        return recordDao.getRecordDatesInRange(eventId, startTime, endTime)
    }

    
    // 提醒相关
    suspend fun addReminder(eventId: String, hour: Int, minute: Int): Long {
        return reminderDao.insertReminder(
            ReminderEntity(
                eventId = eventId,
                hour = hour,
                minute = minute
            )
        )
    }
    
    fun getRemindersByEventId(eventId: String): Flow<List<ReminderEntity>> {
        return reminderDao.getRemindersByEventId(eventId)
    }
    
    suspend fun deleteReminder(reminder: ReminderEntity) {
        reminderDao.deleteReminder(reminder)
    }
    
    suspend fun getAllEnabledReminders(): List<ReminderEntity> {
        return reminderDao.getAllEnabledReminders()
    }
    
    // 清除所有数据
    suspend fun clearAllData() {
        recordDao.deleteAllRecords()
        eventDao.deleteAllEvents()
    }
    
    // 扩展函数：Entity 转 Model
    private fun EventEntity.toEvent(): Event {
        val gson = Gson()
        val type = object : TypeToken<List<com.example.myapplication.model.AttributeDefinition>>() {}.type
        val attributes: List<com.example.myapplication.model.AttributeDefinition> = 
            try {
                gson.fromJson(attributesConfig, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

        // Safe Enum Parsing
        val safeCategory = try {
            EventCategory.valueOf(category) // Try matching "DAILY"
        } catch (e: IllegalArgumentException) {
            // Try matching "日常" (displayName)
            EventCategory.values().find { it.displayName == category } ?: EventCategory.DAILY
        }

        return Event(
            id = id,
            name = name,
            icon = icon,
            backgroundColor = Color(backgroundColor),
            lastRecordTime = null, // 需要单独查询
            category = safeCategory,
            isQuickRecord = isQuickRecord,
            attributes = attributes,
            eventType = eventType,
            groupName = groupName
        )
    }
    
    // 扩展函数：Model 转 Entity
    private fun Event.toEntity(): EventEntity {
        val gson = Gson()
        val attributesConfig = gson.toJson(attributes)
        
        return EventEntity(
            id = id,
            name = name,
            icon = icon,
            backgroundColor = backgroundColor.hashCode(),
            category = category.name, // Always store "DAILY" (name), not "日常"
            isCustom = false,
            isQuickRecord = isQuickRecord,
            attributesConfig = attributesConfig,
            eventType = eventType,
            groupName = groupName
        )
    }

    // Sync from Cloud
    suspend fun syncEvents() {
        try {
            val response = com.example.myapplication.data.api.RetrofitClient.eventApi.getEvents()
            if (response.isSuccessful && response.body() != null) {
                val networkEvents = response.body()!!
                
                // 1. Update/Insert Remote Events
                if (networkEvents.isNotEmpty()) {
                    val entities = networkEvents.map { it.toEntity() }
                    eventDao.insertEvents(entities)
                    println("Sync update: ${entities.size} events loaded from cloud")
                }
                
                // 2. Handle Deletions (Local has it, Remote doesn't)
                // Need to collect from Flow inside suspend function
                val localEventsFlow: Flow<List<EventEntity>> = eventDao.getAllEvents()
                // Use first() to get the current snapshot of the database
                val localEvents: List<EventEntity> = localEventsFlow.first()
                
                val localIds: Set<String> = localEvents.map { it.id }.toSet()
                val remoteIds: Set<String> = networkEvents.mapNotNull { it.id }.toSet()
                
                /* Temporarily Disabled to prevent data loss on upload failure
                // Find IDs to delete
                val idsToDelete: Set<String> = localIds - remoteIds
                
                if (idsToDelete.isNotEmpty()) {
                    println("Sync delete: Removing ${idsToDelete.size} events not found on server")
                    
                    idsToDelete.forEach { idToDelete ->
                        val entityToDelete = localEvents.find { it.id == idToDelete }
                        if (entityToDelete != null) {
                            eventDao.deleteEvent(entityToDelete)
                        }
                    }
                }
                */
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // NetworkEvent -> EventEntity
    private fun com.example.myapplication.data.model.NetworkEvent.toEntity(): EventEntity {
        val colorInt = try {
            // Try parsing Hex string
            if (backgroundColor.startsWith("#")) {
                 android.graphics.Color.parseColor(backgroundColor)
            } else {
                 // Try parsing Int string
                 backgroundColor.toInt()
            }
        } catch (e: Exception) {
            // Log error
            println("Color parse error for ${backgroundColor}: ${e.message}")
            try {
               android.graphics.Color.parseColor("#FF6366F1") // Default Indigo
            } catch (e2: Exception) { -1 }
        }
        
        val gson = Gson()
        val attrString = "[]" // Default empty since attributes removed from NetworkEvent
        
        return EventEntity(
            id = id ?: java.util.UUID.randomUUID().toString(),
            name = name,
            icon = icon,
            backgroundColor = colorInt,
            category = category, // Pass through, let toEvent handle the safety check
            isCustom = false,
            isQuickRecord = false,
            attributesConfig = attrString,
            eventType = "DEFAULT",
            groupName = "默认分组"
        )
    }
}
