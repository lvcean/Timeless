package com.example.myapplication.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.myapplication.model.EventCategory

/**
 * 事件实体类
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String,
    val backgroundColor: Int,
    val category: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false,
    val isQuickRecord: Boolean = false, // 快速记录开关
    val attributesConfig: String = "[]", // 属性配置 JSON
    val eventType: String = "DEFAULT", // 事件类型: DEFAULT, DURATION
    val groupName: String = "默认分组" // 用户分组
)

class Converters {
    @TypeConverter
    fun fromColor(color: Color): Int {
        return color.toArgb()
    }
    
    @TypeConverter
    fun toColor(argb: Int): Color {
        return Color(argb)
    }
    
    @TypeConverter
    fun fromEventCategory(category: EventCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toEventCategory(name: String): EventCategory {
        return EventCategory.valueOf(name)
    }

    // 可以在这里添加 List<AttributeDefinition> 的转换逻辑，或者直接在 ViewModel 层处理 JSON 序列化
    // 为了简单起见，Entity 中直接存储 String，转换在 Service/ViewModel 层做，
    // 或者在这里引入 Gson。假设项目已有 Gson。
}
