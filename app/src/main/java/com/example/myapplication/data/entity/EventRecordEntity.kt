package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 事件记录实体类
 */
@Entity(
    tableName = "event_records",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId")]
)
data class EventRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val attributesData: String = "{}" // 属性值 JSON (Map<String, String>)
)
