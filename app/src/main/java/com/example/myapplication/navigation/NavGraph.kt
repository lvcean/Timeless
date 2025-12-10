package com.example.myapplication.navigation

import androidx.compose.animation.*
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.*
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.EventViewModel

/**
 * 导航图 - 带丝滑过渡动画
 */
@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: EventViewModel,
    authViewModel: AuthViewModel,
    isLoggedIn: Boolean
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
            // 全局默认动画
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeOut(tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300)) }
        ) {
            // 登录页 - 淡入淡出
            composable(
                route = Screen.Login.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    authViewModel = authViewModel
                )
            }
            
            // 首页 - 无动画（底部导航不需要滑动）
            composable(
                route = Screen.Home.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                HomeScreen(
                    viewModel = viewModel,
                    onEventClick = { event ->
                        navController.navigate(Screen.EventDetail.createRoute(event.id))
                    },
                    onEditEvent = { event ->
                        navController.navigate(Screen.CustomEvent.createRoute(event.id))
                    },
                    onGlobalCalendarClick = {
                        navController.navigate(Screen.GlobalCalendar.route)
                    },
                    onAchievementsClick = {
                        navController.navigate(Screen.Achievements.route)
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this
                )
            }
            
            // 统计页 - 无动画（底部导航）
            composable(
                route = Screen.Statistics.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                StatisticsScreen(
                    viewModel = viewModel,
                    onNavigateToEventStats = { eventId ->
                        navController.navigate(Screen.EventStatistics.createRoute(eventId))
                    }
                )
            }

            // 事件详情页 - 从右滑入
            composable(
                route = Screen.EventDetail.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
                enterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + 
                    fadeIn(tween(400)) 
                },
                exitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + 
                    fadeOut(tween(400)) 
                },
                popEnterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + 
                    fadeIn(tween(400)) 
                },
                popExitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + 
                    fadeOut(tween(400)) 
                }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                if (eventId != null) {
                    val events by viewModel.events.collectAsState()
                    val event = events.find { it.id == eventId }
                    if (event != null) {
                        EventDetailScreen(
                            event = event,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToReminders = { navController.navigate(Screen.ReminderSetting.createRoute(event.id)) }
                        )
                    }
                }
            }

            // 事件统计页 - 从右滑入 + 轻微缩放
            composable(
                route = Screen.EventStatistics.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
                enterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + 
                    fadeIn(tween(400)) +
                    scaleIn(initialScale = 0.95f, animationSpec = tween(400))
                },
                exitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + 
                    fadeOut(tween(400)) 
                },
                popEnterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + 
                    fadeIn(tween(400)) 
                },
                popExitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + 
                    fadeOut(tween(400)) +
                    scaleOut(targetScale = 0.95f, animationSpec = tween(400))
                }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                if (eventId != null) {
                    EventStatisticsScreen(
                        eventId = eventId,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this
                    )
                }
            }
            
            // 设置页 - 无动画(底部导航)
            composable(
                route = Screen.Settings.route,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                SettingsScreen(
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            
            // 自定义事件页 - 从底部滑入（模态效果）
            composable(
                route = Screen.CustomEvent.route,
                arguments = listOf(
                    navArgument("eventId") {
                        type = NavType.StringType
                        nullable = true
                    }
                ),
                enterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400)) + 
                    fadeIn(tween(400)) 
                },
                exitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) + 
                    fadeOut(tween(400)) 
                },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) + 
                    fadeOut(tween(400)) 
                }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                CustomEventScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    eventId = eventId
                )
            }
            
            // 提醒设置页 - 从右滑入
            composable(
                route = Screen.ReminderSetting.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
                enterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + 
                    fadeIn(tween(400)) 
                },
                exitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + 
                    fadeOut(tween(400)) 
                },
                popEnterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + 
                    fadeIn(tween(400)) 
                },
                popExitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + 
                    fadeOut(tween(400)) 
                }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                if (eventId != null) {
                    val events by viewModel.events.collectAsState()
                    val event = events.find { it.id == eventId }
                    if (event != null) {
                        ReminderSettingScreen(
                            event = event,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }

            
            // 全局日历页 - 从左滑入
            composable(
                route = Screen.GlobalCalendar.route,
                enterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + 
                    fadeIn(tween(400)) 
                },
                exitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) + 
                    fadeOut(tween(400)) 
                },
                popEnterTransition = { 
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + 
                    fadeIn(tween(400)) 
                },
                popExitTransition = { 
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) + 
                    fadeOut(tween(400)) 
                }
            ) {
                GlobalCalendarScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )

            }
            
            // 成就页 - 无动画 (Simple Fade)
            composable(
                route = Screen.Achievements.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                AchievementsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
