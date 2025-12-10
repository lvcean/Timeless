package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据访问对象
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Update
    suspend fun updateUser(user: UserEntity)
    
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
