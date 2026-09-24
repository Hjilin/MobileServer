package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mobileserver.bridge.BridgeManager
import com.mobileserver.core.Paths
import com.mobileserver.core.SettingsManager
import com.mobileserver.ui.theme.*

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        // ===== 1. 通用设置（国内用户习惯置顶）=====
        SectionTitle("通用")
        SettingsCard {
            var autoStart by remember { mutableStateOf(SettingsManager.getAutoStart(context)) }
            ToggleItem("开机自启动", autoStart) { autoStart = it; SettingsManager.setAutoStart(context, it) }
            var keepAlive by remember { mutableStateOf(SettingsManager.getKeepAlive(context)) }
            ToggleItem("后台保活", keepAlive) { keepAlive = it; SettingsManager.setKeepAlive(context, it) }
            var notification by remember { mutableStateOf(SettingsManager.getNotification(context)) }
            ToggleItem("前台通知", notification) { notification = it; SettingsManager.setNotification(context, it) }
            NavItem("全局存储目录", Paths.serverRoot.absolutePath)
            NavItem("日志保存时长", "${SettingsManager.getLogDays(context)}天")
        }

        // ===== 2. Web网站设置 =====
        SectionTitle("Web 网站设置")
        SettingsCard {
            ClickItem("网站管理", "添加站点 / 端口绑定") { navController.navigate("sites") }
            ClickItem("数据库管理", "MariaDB / phpMyAdmin") { navController.navigate("database") }
            NavItem("网站根目录", "www/")
            NavItem("端口配置", "8080")
            NavItem("PHP 参数", "php.ini")
        }

        // ===== 3. OpenList网盘设置 =====
        SectionTitle("OpenList 网盘设置")
        SettingsCard {
            NavItem("网盘挂载管理", "3个挂载")
            NavItem("缓存大小", "512 MB")
            var mapping by remember { mutableStateOf(BridgeManager.webDirMappingEnabled) }
            ToggleDynamic("网盘目录映射网站目录", mapping) {
                mapping = it
                BridgeManager.webDirMappingEnabled = it
                BridgeManager.applyWebDirMapping()
            }
            NavItem("网页访问密码", "已设置")
        }

        // ===== 4. 组件管理 =====
        SectionTitle("组件管理")
        SettingsCard {
            ClickItem("组件版本管理", "查看/下载") { navController.navigate("components") }
            NavItem("校验文件完整性", "SHA256")
            NavItem("清理缓存", "")
        }

        // ===== 5. 系统信息 & 开源许可 =====
        SectionTitle("系统信息 & 开源许可")
        SettingsCard {
            ClickItem("关于 & 开源许可", "AGPLv3 / 组件协议") { navController.navigate("about") }
            NavItem("设备信息", "Android")
            NavItem("版本信息", "1.0.0")
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = MiuiTextPrimary, fontWeight = FontWeight.Medium)
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
        Column(Modifier.padding(horizontal = 16.dp), content = content)
    }
}

@Composable
fun ToggleItem(title: String, initial: Boolean, onChange: ((Boolean) -> Unit)? = null) {
    var checked by remember { mutableStateOf(initial) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = {
            checked = it
            onChange?.invoke(it)
        })
    }
}

@Composable
fun ToggleDynamic(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun NavItem(title: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = MiuiTextSecondary)
                Spacer(Modifier.width(6.dp))
            }
            Text("›", style = MaterialTheme.typography.bodyLarge, color = MiuiTextHint)
        }
    }
}

@Composable
fun ClickItem(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = MiuiTextSecondary)
                Spacer(Modifier.width(6.dp))
            }
            Text("›", style = MaterialTheme.typography.bodyLarge, color = MiuiTextHint)
        }
    }
}
