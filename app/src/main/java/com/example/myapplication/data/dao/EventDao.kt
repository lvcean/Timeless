package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 事件数据访问对象
 */
@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE category = :category ORDER BY createdAt DESC")
    fun getEventsByCategory(category: String): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: String): EventEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
