package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.dao.EventDao
import com.example.myapplication.data.dao.EventRecordDao
import com.example.myapplication.data.dao.ReminderDao
import com.example.myapplication.data.dao.UserDao
import com.example.myapplication.data.entity.Converters
import com.example.myapplication.data.entity.EventEntity
import com.example.myapplication.data.entity.EventRecordEntity
import com.example.myapplication.data.entity.ReminderEntity
import com.example.myapplication.data.entity.UserEntity

/**
 * 应用数据库
 */
@Database(
    entities = [
        EventEntity::class,
        EventRecordEntity::class,
        ReminderEntity::class,
        UserEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun eventRecordDao(): EventRecordDao
    abstract fun reminderDao(): ReminderDao
    abstract fun userDao(): UserDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "event_tracker_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
