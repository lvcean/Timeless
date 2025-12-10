package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val phone: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
