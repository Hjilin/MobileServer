package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileserver.ui.theme.*

/**
 * 数据库管理页：MariaDB 状态、连接信息、phpMyAdmin 入口。
 */
@Composable
fun DatabaseScreen(viewModel: ServerViewModel = viewModel()) {
    val services by viewModel.services.collectAsState()
    val mariadb = services.firstOrNull { it.name.contains("MariaDB", ignoreCase = true) }
    val redis = services.firstOrNull { it.name.contains("Redis", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("数据库", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)

        // ===== MariaDB 状态 =====
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, null, tint = Primary)
                    Spacer(Modifier.width(10.dp))
                    Text("MariaDB", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                }
                Spacer(Modifier.height(10.dp))
                StatusRow("状态", if (mariadb?.isRunning == true) "运行中" else "已停止", mariadb?.isRunning == true)
                StatusRow("主机", "127.0.0.1", null)
                StatusRow("端口", "3306", null)
                StatusRow("用户", "root", null)
                StatusRow("密码", "安装时自动生成", null)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { mariadb?.let { viewModel.toggle(it.type) } },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = mariadb != null
                ) {
                    Text(if (mariadb?.isRunning == true) "停止 MariaDB" else "启动 MariaDB", fontSize = 13.sp)
                }
            }
        }

        // ===== 数据库列表 =====
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("数据库", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                StatusRow("website", "PHP 站点使用", null)
                StatusRow("openlist", "网盘挂载使用", null)
            }
        }

        // ===== phpMyAdmin 入口 =====
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("可视化管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                AccessRow("phpMyAdmin", "http://127.0.0.1:8080/phpmyadmin/")
                AccessRow("命令行", "mysql -h127.0.0.1 -uroot")
                Spacer(Modifier.height(8.dp))
                Text(
                    "部署 phpMyAdmin：把 phpMyAdmin 解压到 www/phpmyadmin/ 即可通过上面地址访问。",
                    fontSize = 12.sp, color = MiuiTextSecondary
                )
            }
        }

        // ===== Redis 状态 =====
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, null, tint = Primary2)
                    Spacer(Modifier.width(10.dp))
                    Text("Redis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                }
                Spacer(Modifier.height(10.dp))
                StatusRow("状态", if (redis?.isRunning == true) "运行中" else "已停止", redis?.isRunning == true)
                StatusRow("端口", "6379", null)
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, running: Boolean?) {
    Row(Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MiuiTextSecondary, modifier = Modifier.width(70.dp))
        Text(
            value,
            fontSize = 13.sp,
            color = if (running == true) MiuiGreen else MiuiTextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AccessRow(label: String, url: String) {
    Row(Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MiuiTextSecondary, modifier = Modifier.width(90.dp))
        Text(url, fontSize = 12.sp, color = Primary)
    }
}
