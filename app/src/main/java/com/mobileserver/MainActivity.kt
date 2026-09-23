package com.mobileserver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobileserver.ui.ComponentsScreen
import com.mobileserver.ui.FilesScreen
import com.mobileserver.ui.HomeScreen
import com.mobileserver.ui.LogsScreen
import com.mobileserver.ui.NetDiskScreen
import com.mobileserver.ui.PreviewScreen
import com.mobileserver.ui.ServicesScreen
import com.mobileserver.ui.SettingsScreen
import com.mobileserver.ui.theme.MobileServerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileServerTheme {
                MainScaffold()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "首页", Icons.Default.Home)
    data object Files : Screen("files", "文件", Icons.Default.Storage)
    data object NetDisk : Screen("netdisk", "网盘", Icons.Default.Cloud)
    data object Services : Screen("services", "服务", Icons.Default.Devices)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Files, Screen.NetDisk, Screen.Services, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry = navController.currentBackStackEntryAsState().value
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Files.route) { FilesScreen(navController) }
            composable(Screen.NetDisk.route) { NetDiskScreen() }
            composable(Screen.Services.route) { ServicesScreen(navController) }
            composable("logs") { LogsScreen() }
            composable("components") { ComponentsScreen() }
            composable(
                route = "preview?url={url}",
                arguments = [navArgument("url") { type = NavType.StringType; defaultValue = "" }]
            ) { backStackEntry ->
                PreviewScreen(
                    url = backStackEntry.arguments?.getString("url") ?: "",
                    navController = navController
                )
            }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
        }
    }
}
