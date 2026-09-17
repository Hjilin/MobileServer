package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileserver.bridge.BridgeManager
import com.mobileserver.core.Paths
import com.mobileserver.ui.theme.*
import java.io.File

@Composable
fun FilesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("文件管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "统一管理网站目录与网盘缓存，一处操作两边生效",
            style = MaterialTheme.typography.bodySmall,
            color = MiuiTextSecondary
        )

        // 统一存储根目录信息卡
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("统一存储根目录", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(
                    Paths.serverRoot.absolutePath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MiuiTextSecondary
                )
            }
        }

        // 互通目录列表
        DirEntry("网站根目录 www", Paths.wwwDir, Icons.Default.Folder, "PHP站点文件 · OpenList可访问")
        DirEntry("网盘缓存 storage", Paths.storageDir, Icons.Default.Storage, "OpenList本地缓存目录")
        DirEntry("数据目录 data", Paths.dataDir, Icons.Default.Lock, "MariaDB / Redis 数据")
        DirEntry("日志目录 logs", Paths.logsDir, Icons.Default.InsertDriveFile, "全部服务统一日志")
        DirEntry("配置目录 conf", Paths.confDir, Icons.Default.FolderOpen, "Nginx/PHP/数据库配置")

        // 目录映射开关（核心互通开关）
        var mappingEnabled by remember { mutableStateOf(BridgeManager.webDirMappingEnabled) }
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("网盘目录映射网站目录", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "开启后OpenList可直接管理网站源码",
                        style = MaterialTheme.typography.bodySmall,
                        color = MiuiTextSecondary
                    )
                }
                Switch(
                    checked = mappingEnabled,
                    onCheckedChange = {
                        mappingEnabled = it
                        BridgeManager.webDirMappingEnabled = it
                        BridgeManager.applyWebDirMapping()
                    }
                )
            }
        }
    }
}

@Composable
fun DirEntry(title: String, dir: File, icon: ImageVector, desc: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MiuiSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = MiuiOrange, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MiuiTextSecondary)
                    Text(
                        "${dir.listFiles()?.size ?: 0} 个条目",
                        style = MaterialTheme.typography.bodySmall,
                        color = MiuiTextHint
                    )
                }
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MiuiTextHint)
        }
    }
}
