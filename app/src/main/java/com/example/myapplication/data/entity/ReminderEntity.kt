package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 提醒实体类
 */
@Entity(
    tableName = "reminders",
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
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: String,
    val hour: Int,
    val minute: Int,
    val repeatDaily: Boolean = true,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
