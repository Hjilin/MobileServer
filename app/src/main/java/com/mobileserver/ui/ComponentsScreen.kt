package com.mobileserver.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.mobileserver.core.Paths
import com.mobileserver.download.DownloadManager
import com.mobileserver.ui.theme.*
import java.io.File

/**
 * 组件管理页：每个组件独立下载，互不阻塞。
 */
@Composable
fun ComponentsScreen() {
    val context = LocalContext.current
    val dm = remember { DownloadManager(context) }
    var comps by remember { mutableStateOf<List<DownloadManager.Component>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    // 每个组件独立进度和状态
    val progressMap = remember { mutableStateMapOf<String, Float>() }
    val installingMap = remember { mutableStateMapOf<String, Boolean>() }
    val installedMap = remember { mutableStateMapOf<String, Boolean>() }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        loading = true
        errorMsg = null
        runCatching { dm.fetchManifest() }
            .onSuccess {
                comps = it
                it.forEach { c -> installedMap[c.name] = File(Paths.binDir, c.name).exists() }
                loading = false
            }
            .onFailure { errorMsg = "读取组件清单失败: ${it.message}"; loading = false }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("组件管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
        Text("Nginx / PHP / MariaDB / Redis / OpenList / FRP", style = MaterialTheme.typography.bodySmall, color = MiuiTextSecondary)
        Spacer(Modifier.height(14.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (errorMsg != null) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
                Column(Modifier.padding(16.dp)) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(comps) { c ->
                    CompCard(
                        comp = c,
                        installed = installedMap[c.name] == true,
                        installing = installingMap[c.name] == true,
                        progress = progressMap[c.name] ?: 0f,
                        onInstall = {
                            installingMap[c.name] = true
                            progressMap[c.name] = 0f
                            scope.launch {
                                runCatching { dm.installComponent(c) { p -> progressMap[c.name] = p } }
                                    .onSuccess {
                                        installedMap[c.name] = true
                                        Toast.makeText(context, "${c.name} 安装完成", Toast.LENGTH_SHORT).show()
                                    }
                                    .onFailure {
                                        Toast.makeText(context, "${c.name} 失败: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                                installingMap[c.name] = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompCard(
    comp: DownloadManager.Component,
    installed: Boolean,
    installing: Boolean,
    progress: Float,
    onInstall: () -> Unit
) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(comp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                    Text("v${comp.version}", fontSize = 12.sp, color = MiuiTextSecondary)
                }
                AssistChip(
                    onClick = {},
                    label = { Text(if (installed) "已安装" else "未安装", fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (installed) MiuiGreen.copy(alpha = 0.15f) else MiuiBackground,
                        labelColor = if (installed) MiuiGreen else MiuiTextSecondary
                    )
                )
            }
            Spacer(Modifier.height(10.dp))
            if (installing) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text("下载中 ${(progress * 100).toInt()}%", fontSize = 11.sp, color = MiuiTextSecondary)
            } else {
                Button(
                    onClick = onInstall,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (installed) "重新下载" else "下载安装", fontSize = 13.sp)
                }
            }
        }
    }
}
