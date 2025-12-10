package com.example.myapplication.navigation

/**
 * 导航路由
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object Statistics : Screen("statistics")
    object EventStatistics : Screen("event_statistics/{eventId}") {
        fun createRoute(eventId: String) = "event_statistics/$eventId"
    }
    object Settings : Screen("settings")
    object CustomEvent : Screen("custom_event?eventId={eventId}") {
        fun createRoute(eventId: String? = null) = 
            if (eventId != null) "custom_event?eventId=$eventId" else "custom_event"
    }
    object ReminderSetting : Screen("reminder_setting/{eventId}") {
        fun createRoute(eventId: String) = "reminder_setting/$eventId"
    }
    object GlobalCalendar : Screen("global_calendar")
    object Achievements : Screen("achievements")
}
