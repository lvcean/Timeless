package com.example.myapplication.model

import androidx.compose.ui.graphics.Color
import com.example.myapplication.model.AttributeDefinition

/**
 * 事件分类枚举
 */
enum class EventCategory(val displayName: String) {
    ALL("所有"),
    DAILY("日常"),
    HEALTH("健康"),
    FINANCE("财务"),
    LEARNING("学习"),
    ENTERTAINMENT("娱乐")
}

/**
 * 事件数据类
 */
data class Event(
    val id: String,
    val name: String,
    val icon: String,
    val backgroundColor: Color,
    val lastRecordTime: String? = null,
    val category: EventCategory = EventCategory.DAILY,
    val isQuickRecord: Boolean = false,
    val attributes: List<AttributeDefinition> = emptyList(),
    val eventType: String = "DEFAULT",
    val groupName: String = "默认分组"
)

/**
 * 预设事件模板
 */
data class PresetEvent(
    val name: String,
    val icon: String,
    val backgroundColor: Color,
    val category: EventCategory,
    val attributes: List<AttributeDefinition> = emptyList(),
    val eventType: String = "DEFAULT",
    val groupName: String = "默认分组"
)

/**
 * 预设事件列表
 */
object PresetEvents {
    val allPresets = listOf(
        // 日常类
        PresetEvent("日记", "Outlined.EditCalendar", Color(0xFFFFE082), EventCategory.DAILY, 
            attributes = listOf(AttributeDefinition(name = "心情", type = AttributeType.TEXT, description = "今天的心情"))),
        PresetEvent("记账", "Outlined.AttachMoney", Color(0xFFFFE082), EventCategory.FINANCE,
            attributes = listOf(
                AttributeDefinition(name = "金额", type = AttributeType.NUMBER, unit = "元", isRequired = true),
                AttributeDefinition(name = "类型", type = AttributeType.SINGLE_SELECT, options = listOf(
                    AttributeOption("餐饮", 0xFFEF5350.toInt()),
                    AttributeOption("交通", 0xFF42A5F5.toInt()),
                    AttributeOption("购物", 0xFFFFCA28.toInt())
                ))
            )),
        PresetEvent("读书", "Outlined.MenuBook", Color(0xFF81C784), EventCategory.LEARNING,
            attributes = listOf(AttributeDefinition(name = "页数", type = AttributeType.NUMBER, unit = "页"))),
        PresetEvent("运动", "Outlined.DirectionsRun", Color(0xFF64B5F6), EventCategory.HEALTH,
            attributes = listOf(AttributeDefinition(name = "时长", type = AttributeType.NUMBER, unit = "分钟"))),
        PresetEvent("冥想", "Outlined.SelfImprovement", Color(0xFFBA68C8), EventCategory.HEALTH,
            attributes = listOf(AttributeDefinition(name = "时长", type = AttributeType.NUMBER, unit = "分钟"))),
        PresetEvent("喝水", "Outlined.WaterDrop", Color(0xFF4FC3F7), EventCategory.HEALTH,
            attributes = listOf(AttributeDefinition(name = "容量", type = AttributeType.NUMBER, unit = "ml", defaultValue = "200"))),
        
        // 健康类
        PresetEvent("体检", "Outlined.LocalHospital", Color(0xFFEF5350), EventCategory.HEALTH),
        PresetEvent("吃药", "Outlined.MonitorHeart", Color(0xFFFF8A65), EventCategory.HEALTH),
        PresetEvent("睡眠", "Outlined.Bed", Color(0xFF9575CD), EventCategory.HEALTH),
        PresetEvent("体重", "Outlined.FitnessCenter", Color(0xFF4DB6AC), EventCategory.HEALTH,
             attributes = listOf(AttributeDefinition(name = "体重", type = AttributeType.NUMBER, unit = "kg"))),
        
        // 财务类
        PresetEvent("投资", "Outlined.TrendingUp", Color(0xFF66BB6A), EventCategory.FINANCE),
        PresetEvent("储蓄", "Outlined.Savings", Color(0xFFFFCA28), EventCategory.FINANCE),
        PresetEvent("账单", "Outlined.Receipt", Color(0xFFFF7043), EventCategory.FINANCE,
             attributes = listOf(AttributeDefinition(name = "金额", type = AttributeType.NUMBER, unit = "元"))),
        
        // 学习类
        PresetEvent("编程", "Outlined.Code", Color(0xFF42A5F5), EventCategory.LEARNING),
        PresetEvent("英语", "Outlined.School", Color(0xFF26A69A), EventCategory.LEARNING),
        PresetEvent("写作", "Outlined.Brush", Color(0xFFAB47BC), EventCategory.LEARNING),
        
        // 娱乐类
        PresetEvent("电影", "Outlined.Movie", Color(0xFFEC407A), EventCategory.ENTERTAINMENT,
             attributes = listOf(AttributeDefinition(name = "评分", type = AttributeType.RATING))),
        PresetEvent("游戏", "Outlined.Gamepad", Color(0xFF7E57C2), EventCategory.ENTERTAINMENT),
        PresetEvent("音乐", "Outlined.MusicNote", Color(0xFF5C6BC0), EventCategory.ENTERTAINMENT),
        PresetEvent("旅行", "Outlined.Flight", Color(0xFF29B6F6), EventCategory.ENTERTAINMENT)
    )
    
    fun getByCategory(category: EventCategory): List<PresetEvent> {
        return if (category == EventCategory.ALL) {
            allPresets
        } else {
            allPresets.filter { it.category == category }
        }
    }
}
