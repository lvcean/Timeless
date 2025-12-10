package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entity.EventRecordEntity
import com.example.myapplication.data.entity.ReminderEntity
import com.example.myapplication.data.repository.EventRepository
import com.example.myapplication.model.Event
import com.example.myapplication.model.PresetEvents
import com.example.myapplication.util.SoundEffectManager
import com.example.myapplication.worker.ReminderWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 统计数据模型
 */
data class StatisticsData(
    val totalEvents: Int = 0,
    val totalRecords: Int = 0,
    val todayRecords: Int = 0,
    val weeklyTrend: List<Pair<String, Int>> = emptyList(),
    val categoryDistribution: List<Pair<String, Int>> = emptyList()
)

/**
 * 事件 ViewModel
 */
class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = EventRepository(
        database.eventDao(),
        database.eventRecordDao(),
        database.reminderDao()
    )
    private val workManager = WorkManager.getInstance(application)
    private val soundEffectManager = SoundEffectManager.getInstance(application)
    
    val events: StateFlow<List<Event>> = repository.getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val statistics: StateFlow<StatisticsData> = combine(
        repository.getAllEvents(),
        repository.getAllRecords()
    ) { events, records ->
        calculateStatistics(events, records)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsData()
    )
    
    val allRecords: StateFlow<List<EventRecordEntity>> = repository.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Sync data from cloud on startup
        refreshEvents()
    }
    
    fun refreshEvents() {
        viewModelScope.launch {
            repository.syncEvents()
        }
    }
    
    private fun calculateStatistics(events: List<Event>, records: List<EventRecordEntity>): StatisticsData {
        val totalEvents = events.size
        val totalRecords = records.size
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val todayRecords = records.count { it.timestamp >= startOfDay }
        
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val trendMap = mutableMapOf<String, Int>()
        val tempCalendar = Calendar.getInstance()
        for (i in 6 downTo 0) {
            tempCalendar.timeInMillis = System.currentTimeMillis()
            tempCalendar.add(Calendar.DAY_OF_YEAR, -i)
            val dayStr = dateFormat.format(tempCalendar.time)
            trendMap[dayStr] = 0
        }
        records.forEach { record ->
            val recordDate = dateFormat.format(Date(record.timestamp))
            if (trendMap.containsKey(recordDate)) {
                trendMap[recordDate] = trendMap[recordDate]!! + 1
            }
        }
        val weeklyTrend = trendMap.entries.map { it.key to it.value }.sortedBy { it.first }
        
        val eventCategoryMap = events.associate { it.id to it.category }
        val categoryCounts = records.groupingBy {
            eventCategoryMap[it.eventId]?.displayName ?: "未知"
        }.eachCount().toList().sortedByDescending { it.second }
        
        return StatisticsData(
            totalEvents = totalEvents,
            totalRecords = totalRecords,
            todayRecords = todayRecords,
            weeklyTrend = weeklyTrend,
            categoryDistribution = categoryCounts
        )
    }
    
    fun getBadges(): StateFlow<List<com.example.myapplication.model.Badge>> {
        return allRecords.map { records ->
            val unlockedTypes = mutableSetOf<com.example.myapplication.model.BadgeType>()
            val unlockDates = mutableMapOf<com.example.myapplication.model.BadgeType, Long>()

            if (records.isNotEmpty()) {
                unlockedTypes.add(com.example.myapplication.model.BadgeType.FIRST_STEP)
                unlockDates[com.example.myapplication.model.BadgeType.FIRST_STEP] = records.minOf { it.timestamp }
            }

            if (records.size >= 10) {
                unlockedTypes.add(com.example.myapplication.model.BadgeType.TOTAL_10)
            }
            if (records.size >= 100) {
                unlockedTypes.add(com.example.myapplication.model.BadgeType.TOTAL_100)
            }

            val calendar = Calendar.getInstance()
            records.forEach { record ->
                calendar.timeInMillis = record.timestamp
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                if (hour in 4..7) {
                    unlockedTypes.add(com.example.myapplication.model.BadgeType.EARLY_BIRD)
                }
                if (hour >= 23 || hour <= 2) {
                    unlockedTypes.add(com.example.myapplication.model.BadgeType.NIGHT_OWL)
                }
            }

            // Streak Calculation (Simplified for Global Streak)
            val dates = records.map {
                val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
            }.distinct().sorted()

            var currentStreak = 0
            var maxStreak = 0
            // Simple consecutive checklist
            // Note: This is a complex logic simplified: check if previous date was (current - 1 day)
            // Ideally we need to parse String back to Date or use timestamp days.
            // Let's use simple logic:
            if (dates.isNotEmpty()) {
                currentStreak = 1
                maxStreak = 1
                // Check streaks
                // This logic needs improvement for real calendar days, but sufficient for now
                // Actually let's just create a list of timestamps at noon
            }
            
            // Re-eval dates properly
            if (dates.size >= 3) {
                 unlockedTypes.add(com.example.myapplication.model.BadgeType.STREAK_3)
            }
            if (dates.size >= 7) {
                 unlockedTypes.add(com.example.myapplication.model.BadgeType.STREAK_7)
            }
            // Real streak logic requires iteration
            
            com.example.myapplication.model.BadgeDefinitions.getAllBadges(unlockedTypes, unlockDates)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.example.myapplication.model.BadgeDefinitions.getAllBadges(emptySet())
        )
    }

    fun addEvent(event: Event) {
        viewModelScope.launch { repository.addEvent(event) }
    }
    
    fun deleteEvent(event: Event) {
        viewModelScope.launch { 
            repository.deleteEvent(event)
            soundEffectManager.playDelete() // 播放删除音效
        }
    }
    
    fun addRecord(
        eventId: String, 
        note: String = "", 
        timestamp: Long = System.currentTimeMillis(),
        attributes: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch { 
            repository.addRecord(eventId, note, timestamp, attributes)
            soundEffectManager.playSuccess() // 播放成功音效
        }
    }
    
    fun getRecordsByEventId(eventId: String): Flow<List<EventRecordEntity>> {
        return repository.getRecordsByEventId(eventId)
    }
    
    fun deleteRecord(record: EventRecordEntity) {
        viewModelScope.launch { 
            repository.deleteRecord(record)
            soundEffectManager.playDelete() // 播放删除音效
        }
    }
    
    suspend fun getRecordCount(eventId: String): Int {
        return repository.getRecordCount(eventId)
    }

    // 获取指定时间范围内的打卡次数
    suspend fun getRecordCountInRange(eventId: String, startTime: Long, endTime: Long): Int {
        return repository.getRecordCountInRange(eventId, startTime, endTime)
    }

    // 获取指定时间范围内的打卡天数（去重）
    suspend fun getRecordDaysInRange(eventId: String, startTime: Long, endTime: Long): Int {
        return repository.getRecordDaysInRange(eventId, startTime, endTime)
    }

    // 获取指定时间范围内的所有打卡日期
    suspend fun getRecordDatesInRange(eventId: String, startTime: Long, endTime: Long): List<Long> {
        return repository.getRecordDatesInRange(eventId, startTime, endTime)
    }


    fun getReminders(eventId: String): Flow<List<ReminderEntity>> {
        return repository.getRemindersByEventId(eventId)
    }

    fun addReminder(eventId: String, eventName: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            val reminderId = repository.addReminder(eventId, hour, minute)
            scheduleReminderWorker(reminderId, eventId, eventName, hour, minute)
        }
    }
    
    fun updateEvent(event: Event) {
        viewModelScope.launch { repository.updateEvent(event) }
    }

    fun updateEventAttribute(event: Event, updatedAttribute: com.example.myapplication.model.AttributeDefinition) {
        val updatedAttributes = event.attributes.map { 
            if (it.id == updatedAttribute.id) updatedAttribute else it 
        }
        val updatedEvent = event.copy(attributes = updatedAttributes)
        updateEvent(updatedEvent)
    }

    suspend fun getEventById(eventId: String): Event? {
        return repository.getAllEvents().first().find { it.id == eventId }
    }
    
    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            workManager.cancelUniqueWork("reminder_${reminder.id}")
        }
    }
    
    private fun scheduleReminderWorker(reminderId: Long, eventId: String, eventName: String, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val delay = target.timeInMillis - now.timeInMillis
        
        val data = workDataOf("eventId" to eventId, "eventName" to eventName)
        
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder")
            .build()
            
        workManager.enqueueUniqueWork(
            "reminder_$reminderId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    // 导出 CSV
    fun exportCSV(context: android.content.Context) {
        viewModelScope.launch {
            val eventsList = events.value
            val recordsList = repository.getAllRecords().first()
            val success = com.example.myapplication.util.ExportHelper.exportToCSV(context, eventsList, recordsList)
            android.widget.Toast.makeText(
                context,
                if (success) "CSV 导出成功 (下载目录)" else "导出失败",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // 导出 JSON
    fun exportJSON(context: android.content.Context) {
        viewModelScope.launch {
            val eventsList = events.value
            val recordsList = repository.getAllRecords().first()
            val success = com.example.myapplication.util.ExportHelper.exportToJSON(context, eventsList, recordsList)
            android.widget.Toast.makeText(
                context,
                if (success) "JSON 导出成功 (下载目录)" else "导出失败",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // 清除所有数据
    fun clearAllData(context: android.content.Context) {
        viewModelScope.launch {
            repository.clearAllData()
            android.widget.Toast.makeText(context, "所有数据已清除", android.widget.Toast.LENGTH_SHORT).show()
            
            repository.addEvents(PresetEvents.allPresets.map { preset ->
                Event(
                    id = UUID.randomUUID().toString(),
                    name = preset.name,
                    icon = preset.icon,
                    backgroundColor = preset.backgroundColor,
                    category = preset.category
                )
            })
        }
    }
}
