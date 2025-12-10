package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.NavGraph
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EventTrackerApp()
            }
        }
    }
}

@Composable
fun EventTrackerApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val viewModel: EventViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // 获取登录状态 (initial = null to waiting for state)
    val isLoggedInState by authViewModel.isLoggedIn.collectAsState(initial = null)
    
    if (isLoggedInState == null) {
        // Splash / Loading Screen
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val isLoggedIn = isLoggedInState!!
        
        // 定义底部导航项
        val bottomNavItems = listOf(
            BottomNavItem("首页", Screen.Home.route, Icons.Filled.Home),
            BottomNavItem("统计", Screen.Statistics.route, Icons.Filled.BarChart),
            BottomNavItem("设置", Screen.Settings.route, Icons.Filled.Settings)
        )
        
        // 判断是否显示底部导航栏 (登录页不显示)
        val showBottomBar = currentRoute in bottomNavItems.map { it.route } && isLoggedIn
        
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Home.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    isLoggedIn = isLoggedIn
                )
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)