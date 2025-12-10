package com.example.myapplication.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class BadgeType {
    FIRST_STEP,
    STREAK_3,
    STREAK_7,
    TOTAL_10,
    TOTAL_100,
    EARLY_BIRD,
    NIGHT_OWL
}

data class Badge(
    val type: BadgeType,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isUnlocked: Boolean,
    val unlockedDate: Long? = null
)

object BadgeDefinitions {
    fun getAllBadges(unlockedTypes: Set<BadgeType>, unlockDates: Map<BadgeType, Long> = emptyMap()): List<Badge> {
        return listOf(
            Badge(
                BadgeType.FIRST_STEP,
                "第一步",
                "完成你的第一次打卡记录",
                Icons.Filled.LooksOne,
                Color(0xFF4CAF50),
                unlockedTypes.contains(BadgeType.FIRST_STEP),
                unlockDates[BadgeType.FIRST_STEP]
            ),
            Badge(
                BadgeType.STREAK_3,
                "三日连胜",
                "连续 3 天都有打卡记录",
                Icons.Filled.TrendingUp,
                Color(0xFF2196F3),
                unlockedTypes.contains(BadgeType.STREAK_3),
                unlockDates[BadgeType.STREAK_3]
            ),
            Badge(
                BadgeType.STREAK_7,
                "一周坚持",
                "连续 7 天都有打卡记录",
                Icons.Filled.LocalFireDepartment,
                Color(0xFFFF5722),
                unlockedTypes.contains(BadgeType.STREAK_7),
                unlockDates[BadgeType.STREAK_7]
            ),
            Badge(
                BadgeType.TOTAL_10,
                "初级记录员",
                "累计打卡 10 次",
                Icons.Filled.EmojiEvents,
                Color(0xFFFFC107),
                unlockedTypes.contains(BadgeType.TOTAL_10),
                unlockDates[BadgeType.TOTAL_10]
            ),
            Badge(
                BadgeType.TOTAL_100,
                "打卡大师",
                "累计打卡 100 次",
                Icons.Filled.EmojiEvents,
                Color(0xFF9C27B0),
                unlockedTypes.contains(BadgeType.TOTAL_100),
                unlockDates[BadgeType.TOTAL_100]
            ),
            Badge(
                BadgeType.EARLY_BIRD,
                "早起鸟",
                "在清晨 (4:00-8:00) 完成一次打卡",
                Icons.Filled.WbSunny,
                Color(0xFFFF9800),
                unlockedTypes.contains(BadgeType.EARLY_BIRD),
                unlockDates[BadgeType.EARLY_BIRD]
            ),
            Badge(
                BadgeType.NIGHT_OWL,
                "夜猫子",
                "在深夜 (23:00-3:00) 完成一次打卡",
                Icons.Filled.Bedtime,
                Color(0xFF607D8B),
                unlockedTypes.contains(BadgeType.NIGHT_OWL),
                unlockDates[BadgeType.NIGHT_OWL]
            )
        )
    }
}
