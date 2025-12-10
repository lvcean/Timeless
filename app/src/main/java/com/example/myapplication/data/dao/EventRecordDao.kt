package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.EventRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 事件记录数据访问对象
 */
@Dao
interface EventRecordDao {
    @Query("SELECT * FROM event_records WHERE eventId = :eventId ORDER BY timestamp DESC")
    fun getRecordsByEventId(eventId: String): Flow<List<EventRecordEntity>>

    @Query("SELECT * FROM event_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<EventRecordEntity>>
    
    @Query("SELECT * FROM event_records WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getRecordsByTimeRange(startTime: Long, endTime: Long): List<EventRecordEntity>
    
    @Query("SELECT * FROM event_records ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentRecords(limit: Int): List<EventRecordEntity>
    
    @Query("SELECT COUNT(*) FROM event_records WHERE eventId = :eventId")
    suspend fun getRecordCountByEventId(eventId: String): Int
    
    @Query("SELECT COUNT(*) FROM event_records")
    suspend fun getTotalRecordCount(): Int

    @Query("SELECT COUNT(*) FROM event_records WHERE eventId = :eventId AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getRecordCountInRange(eventId: String, startTime: Long, endTime: Long): Int

    @Query("SELECT COUNT(DISTINCT DATE(timestamp / 1000, 'unixepoch')) FROM event_records WHERE eventId = :eventId AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getRecordDaysInRange(eventId: String, startTime: Long, endTime: Long): Int

    @Query("SELECT timestamp FROM event_records WHERE eventId = :eventId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp")
    suspend fun getRecordDatesInRange(eventId: String, startTime: Long, endTime: Long): List<Long>


    
    @Insert
    suspend fun insertRecord(record: EventRecordEntity): Long
    
    @Update
    suspend fun updateRecord(record: EventRecordEntity)
    
    @Delete
    suspend fun deleteRecord(record: EventRecordEntity)
    
    @Query("DELETE FROM event_records WHERE eventId = :eventId")
    suspend fun deleteRecordsByEventId(eventId: String)
    
    @Query("DELETE FROM event_records")
    suspend fun deleteAllRecords()
}
