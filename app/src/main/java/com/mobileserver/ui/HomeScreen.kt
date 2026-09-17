package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileserver.engine.EngineController.ServiceState
import com.mobileserver.engine.EngineController.ServiceType
import com.mobileserver.ui.theme.*

@Composable
fun HomeScreen(viewModel: ServerViewModel = viewModel()) {
    val services by viewModel.services.collectAsState()
    val isAllRunning by viewModel.isAllRunning.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ===== 顶部状态卡片（MIUI橙色渐变感）=====
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiOrange),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    if (isAllRunning) "全部服务运行中" else "服务未全部启动",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "统一入口 http://127.0.0.1:8080",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.startAll() },
                        enabled = !loading && !isAllRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MiuiOrange)
                    ) {
                        if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = MiuiOrange, strokeWidth = 2.dp)
                        else Text("一键启动")
                    }
                    OutlinedButton(
                        onClick = { viewModel.stopAll() },
                        enabled = !loading && isAllRunning,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                    ) {
                        Text("一键停止")
                    }
                }
            }
        }

        // ===== 服务卡片组 =====
        services.forEach { svc ->
            ServiceCard(svc, viewModel)
        }

        // ===== 快捷功能宫格（MIUI大图标）=====
        Text("快捷功能", style = MaterialTheme.typography.titleMedium, color = MiuiTextPrimary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickAction("文件管理", Icons.Default.Folder)
            QuickAction("Web面板", Icons.Default.Public)
            QuickAction("日志查看", Icons.Default.Description)
            QuickAction("组件更新", Icons.Default.Cloud)
        }
    }
}

@Composable
fun ServiceCard(svc: ServiceState, viewModel: ServerViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MiuiSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 点击进入服务详情 */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 运行状态圆点
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(
                                if (svc.isRunning) MiuiGreen else MiuiTextHint,
                                RoundedCornerShape(50)
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(svc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "端口 ${svc.port}${if (svc.isRunning) " · 运行中" else " · 已停止"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MiuiTextSecondary
                )
            }
            Switch(
                checked = svc.isRunning,
                onCheckedChange = { viewModel.toggle(svc.type) }
            )
        }
    }
}

@Composable
fun QuickAction(name: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiSurface),
            modifier = Modifier.size(60.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = name, tint = MiuiOrange, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(name, fontSize = 11.sp, color = MiuiTextSecondary)
    }
}
