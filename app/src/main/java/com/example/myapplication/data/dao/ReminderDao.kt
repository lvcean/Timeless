package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

/**
 * 提醒数据访问对象
 */
@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE eventId = :eventId")
    fun getRemindersByEventId(eventId: String): Flow<List<ReminderEntity>>
    
    @Query("SELECT * FROM reminders WHERE enabled = 1")
    suspend fun getAllEnabledReminders(): List<ReminderEntity>
    
    @Insert
    suspend fun insertReminder(reminder: ReminderEntity): Long
    
    @Update
    suspend fun updateReminder(reminder: ReminderEntity)
    
    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
    
    @Query("DELETE FROM reminders WHERE eventId = :eventId")
    suspend fun deleteRemindersByEventId(eventId: String)
}
