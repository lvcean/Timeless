package com.example.myapplication.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

data class IconUiModel(
    val name: String, // Unique key for storage, e.g., "Outlined.Home"
    val icon: ImageVector,
    val tags: List<String> = emptyList()
)

object IconLibrary {

    // Helper to format name
    private fun key(style: String, name: String) = "$style.$name"

    // CATEGORY: HEALTH & SPORTS
    val HealthIcons = listOf(
        IconUiModel("Outlined.DirectionsRun", Icons.Outlined.DirectionsRun),
        IconUiModel("Outlined.FitnessCenter", Icons.Outlined.FitnessCenter),
        IconUiModel("Outlined.SelfImprovement", Icons.Outlined.SelfImprovement), // Yoga/Meditation
        IconUiModel("Outlined.Pool", Icons.Outlined.Pool),
        IconUiModel("Outlined.DirectionsBike", Icons.Outlined.DirectionsBike),
        IconUiModel("Outlined.Hiking", Icons.Outlined.Hiking),
        IconUiModel("Outlined.WaterDrop", Icons.Outlined.WaterDrop),
        IconUiModel("Outlined.Bed", Icons.Outlined.Bed), // Sleep
        IconUiModel("Outlined.MonitorHeart", Icons.Outlined.MonitorHeart),
        IconUiModel("Outlined.Restaurant", Icons.Outlined.Restaurant), // Diet
        IconUiModel("Outlined.LocalHospital", Icons.Outlined.LocalHospital),
        IconUiModel("Outlined.Spa", Icons.Outlined.Spa),
        // Filled variants for emphasis
        IconUiModel("Filled.DirectionsRun", Icons.Filled.DirectionsRun),
        IconUiModel("Filled.Favorites", Icons.Filled.Favorite),
        IconUiModel("Filled.LocalFireDepartment", Icons.Filled.LocalFireDepartment),
    )

    // CATEGORY: PRODUCTIVITY & WORK
    val WorkIcons = listOf(
        IconUiModel("Outlined.Work", Icons.Outlined.Work),
        IconUiModel("Outlined.Computer", Icons.Outlined.Computer),
        IconUiModel("Outlined.Code", Icons.Outlined.Code),
        IconUiModel("Outlined.MenuBook", Icons.Outlined.MenuBook), // Reading/Study
        IconUiModel("Outlined.School", Icons.Outlined.School),
        IconUiModel("Outlined.EditCalendar", Icons.Outlined.EditCalendar),
        IconUiModel("Outlined.Email", Icons.Outlined.Email),
        IconUiModel("Outlined.Call", Icons.Outlined.Call),
        IconUiModel("Outlined.Schedule", Icons.Outlined.Schedule),
        IconUiModel("Outlined.Assignment", Icons.Outlined.Assignment),
        IconUiModel("Outlined.FactCheck", Icons.Outlined.FactCheck),
        IconUiModel("Filled.Bolt", Icons.Filled.Bolt), // Energy/Focus
    )

    // CATEGORY: LIFESTYLE & DAILY
    val LifestyleIcons = listOf(
        IconUiModel("Outlined.Home", Icons.Outlined.Home),
        IconUiModel("Outlined.ShoppingCart", Icons.Outlined.ShoppingCart),
        IconUiModel("Outlined.LocalCafe", Icons.Outlined.LocalCafe),
        IconUiModel("Outlined.LocalBar", Icons.Outlined.LocalBar),
        IconUiModel("Outlined.MusicNote", Icons.Outlined.MusicNote),
        IconUiModel("Outlined.Movie", Icons.Outlined.Movie),
        IconUiModel("Outlined.Gamepad", Icons.Outlined.Gamepad),
        IconUiModel("Outlined.Pets", Icons.Outlined.Pets),
        IconUiModel("Outlined.LocalFlorist", Icons.Outlined.LocalFlorist), // Garden
        IconUiModel("Outlined.Brush", Icons.Outlined.Brush), // Art
        IconUiModel("Outlined.CameraAlt", Icons.Outlined.CameraAlt),
        IconUiModel("Outlined.Flight", Icons.Outlined.Flight),
        IconUiModel("Outlined.DirectionsCar", Icons.Outlined.DirectionsCar),
        IconUiModel("Filled.Star", Icons.Filled.Star),
        IconUiModel("Filled.AutoAwesome", Icons.Filled.AutoAwesome),
    )
    
     // CATEGORY: FINANCE
    val FinanceIcons = listOf(
        IconUiModel("Outlined.AttachMoney", Icons.Outlined.AttachMoney),
        IconUiModel("Outlined.Savings", Icons.Outlined.Savings),
        IconUiModel("Outlined.AccountBalance", Icons.Outlined.AccountBalance),
        IconUiModel("Outlined.AccountBalanceWallet", Icons.Outlined.AccountBalanceWallet),
        IconUiModel("Outlined.TrendingUp", Icons.Outlined.TrendingUp),
        IconUiModel("Outlined.CreditCard", Icons.Outlined.CreditCard),
        IconUiModel("Outlined.Receipt", Icons.Outlined.Receipt),
        IconUiModel("Outlined.ShoppingBag", Icons.Outlined.ShoppingBag),
    )

    val AllCategories = mapOf(
        "健康" to HealthIcons,
        "工作" to WorkIcons,
        "生活" to LifestyleIcons,
        "财务" to FinanceIcons
    )

    // Lookup map
    val IconMap: Map<String, ImageVector> = (HealthIcons + WorkIcons + LifestyleIcons + FinanceIcons)
        .associate { it.name to it.icon }

    fun getIcon(name: String): ImageVector {
        return IconMap[name] ?: Icons.Default.Event // Fallback
    }
}
