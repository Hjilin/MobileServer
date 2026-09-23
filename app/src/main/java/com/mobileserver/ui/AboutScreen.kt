package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileserver.ui.theme.*

/**
 * 关于 & 开源许可页（AGPLv3 合规必需）。
 */
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Icon(Icons.Default.Info, null, tint = Primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(10.dp))
                Text("MobileServer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Text("v1.0.0", fontSize = 13.sp, color = MiuiTextSecondary)
                Spacer(Modifier.height(8.dp))
                Text("手机一体化服务器 · Nginx + PHP + MariaDB + Redis + OpenList", fontSize = 12.sp, color = MiuiTextSecondary)
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("开源许可", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                LicenseRow("APP 源码", "AGPL-3.0")
                LicenseRow("Nginx", "BSD 2-Clause")
                LicenseRow("PHP", "PHP License 3.01")
                LicenseRow("MariaDB", "GPL v2（独立进程隔离）")
                LicenseRow("Redis", "RSALv2 / SSPLv1")
                LicenseRow("OpenList", "AGPL-3.0")
                LicenseRow("n2n", "GPL v3")
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("说明", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                Text(
                    "本应用为自由软件，源码可在 GitHub 获取。\n" +
                    "各服务二进制以独立进程方式运行，不与 APP 链接，符合 GPL mere aggregation 条款。\n" +
                    "使用 OpenList 需遵守 AGPL-3.0，本 APP 整体源码以相同协议开源。",
                    fontSize = 12.sp, color = MiuiTextSecondary
                )
            }
        }
    }
}

@Composable
private fun LicenseRow(name: String, license: String) {
    Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, fontSize = 13.sp, color = MiuiTextSecondary, modifier = Modifier.width(100.dp))
        Text(license, fontSize = 13.sp, color = MiuiTextPrimary, fontWeight = FontWeight.Medium)
    }
}
