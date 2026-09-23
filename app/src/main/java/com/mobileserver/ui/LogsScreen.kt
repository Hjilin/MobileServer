package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileserver.core.Paths
import com.mobileserver.ui.theme.*
import java.io.File

/**
 * 运行日志页：聚合查看各服务日志，深色终端风格。
 */
@Composable
fun LogsScreen() {
    val services = listOf("nginx", "php", "mariadb", "redis", "openlist")
    var selected by remember { mutableStateOf("nginx") }
    var content by remember { mutableStateOf("") }
    var refreshTick by remember(selected) { mutableStateOf(0) }

    // 读取当前选中服务的日志
    LaunchedEffect(selected, refreshTick) {
        val f = File(Paths.logsDir, "$selected.log")
        content = if (f.exists()) {
            val txt = f.readText()
            if (txt.length > 50000) txt.takeLast(50000) else txt
        } else "（暂无日志）"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("运行日志", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { refreshTick++ }) { Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = Primary) }
                IconButton(onClick = {
                    File(Paths.logsDir, "$selected.log").writeText("")
                    refreshTick++
                }) { Icon(Icons.Default.DeleteSweep, contentDescription = "清空", tint = MiuiRed) }
            }
        }
        Spacer(Modifier.height(10.dp))

        // 服务切换标签
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            services.forEach { s ->
                FilterChip(
                    selected = selected == s,
                    onClick = { selected = s },
                    label = { Text(s, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        // 终端输出区
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TerminalBg),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                content.ifBlank { "（暂无日志）" },
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                color = TerminalFg,
                fontSize = 11.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}
