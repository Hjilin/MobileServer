package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileserver.engine.EngineController.ServiceState
import com.mobileserver.ui.theme.*

@Composable
fun ServicesScreen(navController: NavController, viewModel: ServerViewModel = viewModel()) {
    val services by viewModel.services.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("服务管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        // 端口占用提示条
        val busyPorts = services.filter { !it.isRunning && viewModel.isPortBusy(it.port) }
        if (busyPorts.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MiuiOrangeLight)
            ) {
                Text(
                    "提示：${busyPorts.joinToString("、") { "${it.name}(${it.port})" }} 端口已被其他程序占用",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MiuiOrangeDark
                )
            }
        }

        // 服务列表卡片
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                services.forEachIndexed { index, svc ->
                    ServiceRow(svc, viewModel)
                    if (index < services.size - 1) {
                        HorizontalDivider(color = MiuiDivider)
                    }
                }
            }
        }

        // 运行日志入口
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiSurface),
            modifier = Modifier.clickable { navController.navigate("logs") }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("查看运行日志", style = MaterialTheme.typography.bodyLarge, color = MiuiTextPrimary)
                Text("nginx/php/mariadb… ›", style = MaterialTheme.typography.bodySmall, color = MiuiTextSecondary)
            }
        }

        // 访问入口卡片
        Text("访问入口", style = MaterialTheme.typography.titleMedium, color = MiuiTextPrimary)
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                EntryRow("管理面板", "http://127.0.0.1:8080")
                EntryRow("PHP网站", "http://127.0.0.1:8080/web/")
                EntryRow("OpenList网盘", "http://127.0.0.1:8080/alist/")
            }
        }
    }
}

@Composable
fun ServiceRow(svc: ServiceState, viewModel: ServerViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(if (svc.isRunning) MiuiGreen else MiuiTextHint, RoundedCornerShape(50))
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(svc.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    "端口 ${svc.port} · ${if (svc.isRunning) "运行中" else "已停止"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MiuiTextSecondary
                )
            }
        }
        Switch(checked = svc.isRunning, onCheckedChange = { viewModel.toggle(svc.type) })
    }
}

@Composable
fun EntryRow(title: String, url: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(url, style = MaterialTheme.typography.bodySmall, color = MiuiTextSecondary)
    }
}
