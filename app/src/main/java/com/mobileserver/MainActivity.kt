package com.mobileserver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.mobileserver.ui.*
import com.mobileserver.ui.theme.MobileServerTheme
import com.mobileserver.ui.theme.*

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

data class NavItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items = listOf(
        NavItem("home", "仪表盘", Icons.Default.Home),
        NavItem("sites", "网站管理", Icons.Default.Info),
        NavItem("files", "文件管理", Icons.Default.Folder),
        NavItem("ftp", "FTP 用户", Icons.Default.Share),
        NavItem("webdav", "WebDAV 共享", Icons.Default.Cloud),
        NavItem("db", "数据库", Icons.Default.Storage),
        NavItem("tunnel", "内网穿透", Icons.Default.Build),
        NavItem("components", "组件管理", Icons.Default.Download),
        NavItem("logs", "运行日志", Icons.Default.Article),
        NavItem("settings", "设置", Icons.Default.Settings),
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = items.find { it.route == currentRoute }?.title ?: "仪表盘"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(260.dp),
                drawerContainerColor = MiuiSurface
            ) {
                Column(Modifier.padding(vertical = 20.dp)) {
                    Row(
                        Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Cloud, null, tint = Primary)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("简云", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MiuiTextPrimary)
                            Text("管理面板", fontSize = 12.sp, color = MiuiTextSecondary)
                        }
                    }
                    Divider(color = MiuiBackground)
                    Spacer(Modifier.height(8.dp))
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        items.forEach { item ->
                            val selected = currentRoute == item.route
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(item.route) {
                                            popUpTo("home") { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                    .background(if (selected) Primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    item.icon, null,
                                    tint = if (selected) Primary else MiuiTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    item.title,
                                    color = if (selected) Primary else MiuiTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Surface(color = MiuiSurface, shadowElevation = 2.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(currentTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MiuiTextPrimary)
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen() }
                composable("sites") { SitesScreen() }
                composable("files") { FilesScreen(navController) }
                composable("ftp") { FtpScreen() }
                composable("webdav") { WebDavScreen() }
                composable("db") { DatabaseScreen() }
                composable("tunnel") { TunnelScreen() }
                composable("components") { ComponentsScreen() }
                composable("logs") { LogsScreen() }
                composable("settings") { SettingsScreen(navController) }
                composable("about") { AboutScreen() }
                composable(
                    route = "preview?url={url}",
                    arguments = listOf(navArgument("url") { type = NavType.StringType; defaultValue = "" })
                ) { backStackEntry ->
                    PreviewScreen(
                        url = backStackEntry.arguments?.getString("url") ?: "",
                        navController = navController
                    )
                }
            }
        }
    }
}
