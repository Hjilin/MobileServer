package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileserver.core.Paths
import com.mobileserver.ui.theme.*
import java.io.File

/**
 * 网站管理页：站点列表、添加站点（写 Nginx conf，重启生效）。
 */
@Composable
fun SitesScreen() {
    var showAdd by remember { mutableStateOf(false) }
    var refreshTick by remember { mutableStateOf(0) }

    val sitesDir = remember { File(Paths.confDir, "sites").apply { mkdirs() } }
    val sites = remember(sitesDir, refreshTick) {
        sitesDir.listFiles()?.filter { it.extension == "conf" }?.toList() ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("网站管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
            Button(onClick = { showAdd = true }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("添加站点", fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(14.dp))

        if (sites.isEmpty()) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
                Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Language, null, tint = MiuiTextHint, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("暂无自定义站点", color = MiuiTextSecondary, fontSize = 14.sp)
                    Text("默认站点运行在 8080 端口", color = MiuiTextHint, fontSize = 12.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sites.forEach { f ->
                    SiteCard(file = f, onDelete = { f.delete(); refreshTick++ })
                }
            }
        }
    }

    if (showAdd) {
        AddSiteDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, port, root ->
                val conf = """
                    server {
                        listen $port;
                        server_name $name;
                        root $root;
                        index index.php index.html;
                        location / { try_files ${'$'}uri ${'$'}/ /index.php?${'$'}uri=${'$'}query_string; }
                        location ~ \.php${'$'} {
                            fastcgi_pass 127.0.0.1:9000;
                            fastcgi_param SCRIPT_FILENAME $root${'$'}fastcgi_script_name;
                            include fastcgi_params;
                        }
                    }
                """.trimIndent()
                File(sitesDir, "$name.conf").writeText(conf)
                showAdd = false
                refreshTick++
            }
        )
    }
}

@Composable
private fun SiteCard(file: File, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(file.nameWithoutExtension, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(4.dp))
                Text("端口 ${file.readText().substringAfter("listen ").substringBefore(";")}", fontSize = 12.sp, color = MiuiTextSecondary)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "删除", tint = MiuiRed, modifier = Modifier.size(20.dp)) }
        }
    }
}

@Composable
private fun AddSiteDialog(onDismiss: () -> Unit, onConfirm: (name: String, port: String, root: String) -> Unit) {
    var name by remember { mutableStateOf("mysite") }
    var port by remember { mutableStateOf("8081") }
    var root by remember { mutableStateOf(Paths.wwwDir.absolutePath + "/mysite") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加站点") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("站点名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("端口") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = root, onValueChange = { root = it }, label = { Text("根目录") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(name, port, root) }) { Text("创建") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
