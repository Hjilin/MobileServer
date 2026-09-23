package com.mobileserver.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileserver.engine.EngineController.ServiceState
import com.mobileserver.ui.theme.*

@Composable
fun HomeScreen(viewModel: ServerViewModel = viewModel()) {
    val services by viewModel.services.collectAsState()
    val isAllRunning by viewModel.isAllRunning.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val runningCount = services.count { it.isRunning }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ===== 顶部渐变状态卡（简云主色渐变）=====
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .background(
                        Brush.linearGradient(listOf(Primary, Primary2))
                    )
                    .padding(20.dp)
            ) {
                Column {
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Primary
                            )
                        ) {
                            if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = Primary, strokeWidth = 2.dp)
                            else Text("一键启动")
                        }
                        OutlinedButton(
                            onClick = { viewModel.stopAll() },
                            enabled = !loading && isAllRunning,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                        ) { Text("一键停止") }
                    }
                }
            }
        }

        // ===== 统计卡 2x2（对齐简云 grid）=====
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("运行服务", "$runningCount/${services.size}", Icons.Default.Memory, Primary, Modifier.weight(1f))
            StatCard("统一端口", "8080", Icons.Default.Dns, Green, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("本机地址", "127.0.0.1", Icons.Default.Public, Amber, Modifier.weight(1f))
            StatCard("网盘", "OpenList", Icons.Default.Cloud, Purple, Modifier.weight(1f))
        }

        // ===== 服务状态列表（简云 service-row：绿点 + 名称/描述 + 开关）=====
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("服务状态", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(6.dp))
                services.forEach { svc -> HomeServiceRow(svc, viewModel) }
            }
        }

        // ===== 访问信息卡 =====
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("访问信息", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                AccessLine("Web 面板", "http://127.0.0.1:8080/")
                AccessLine("PHP 站点", "http://127.0.0.1:8080/web/")
                AccessLine("网盘管理", "http://127.0.0.1:8080/alist/")
                AccessLine("本机 IP", "在同一 WiFi 下局域网可访问")
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MiuiSurface),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = MiuiTextSecondary)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
        }
    }
}

@Composable
fun HomeServiceRow(svc: ServiceState, viewModel: ServerViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (svc.isRunning) MiuiGreen else MiuiTextHint)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(svc.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MiuiTextPrimary)
                Text("端口 ${svc.port}${if (svc.isRunning) " · 运行中" else " · 已停止"}", fontSize = 12.sp, color = MiuiTextSecondary)
            }
        }
        Switch(checked = svc.isRunning, onCheckedChange = { viewModel.toggle(svc.type) })
    }
}

@Composable
fun AccessLine(label: String, value: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("$label：", fontSize = 13.sp, color = MiuiTextSecondary)
        Text(value, fontSize = 13.sp, color = MiuiTextPrimary, fontWeight = FontWeight.Medium)
    }
}
