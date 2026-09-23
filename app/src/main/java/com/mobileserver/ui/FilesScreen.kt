package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileserver.core.Paths
import com.mobileserver.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TEXT_EXT = setOf("php", "html", "htm", "txt", "js", "css", "json", "conf", "ini", "log", "sh", "xml", "yml", "yaml", "md")

@Composable
fun FilesScreen() {
    var currentDir by remember { mutableStateOf(Paths.wwwDir) }
    var showNewFolder by remember { mutableStateOf(false) }
    var showNewFile by remember { mutableStateOf(false) }
    var editingFile by remember { mutableStateOf<File?>(null) }
    var refreshTick by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 顶部标题
        Text("文件管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
        Spacer(Modifier.height(4.dp))
        Text("统一管理网站目录与网盘缓存", style = MaterialTheme.typography.bodySmall, color = MiuiTextSecondary)
        Spacer(Modifier.height(12.dp))

        // 工具行
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = { if (currentDir.parentFile != null) currentDir = currentDir.parentFile!! }, label = { Text("上级") })
            AssistChip(onClick = { currentDir = Paths.wwwDir }, label = { Text("网站根") })
            AssistChip(onClick = { refreshTick++ }, label = { Text("刷新") })
            Spacer(Modifier.weight(1f))
            AssistChip(onClick = { showNewFolder = true }, label = { Text("新建夹") })
            AssistChip(onClick = { showNewFile = true }, label = { Text("新建文件") })
        }
        Spacer(Modifier.height(8.dp))

        // 当前路径
        Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Text(
                currentDir.absolutePath,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 11.sp,
                color = MiuiTextSecondary
            )
        }
        Spacer(Modifier.height(8.dp))

        // 文件列表（refreshTick 用于触发重组）
        val files = remember(currentDir, refreshTick) {
            currentDir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
        }
        if (files.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("空目录", color = MiuiTextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(files) { f ->
                    FileRow(
                        file = f,
                        onClick = {
                            if (f.isDirectory) currentDir = f
                            else if (f.extension.lowercase() in TEXT_EXT) editingFile = f
                        },
                        onDelete = { f.delete(); refreshTick++ }
                    )
                }
            }
        }
    }

    // 新建文件夹对话框
    if (showNewFolder) {
        InputDialog(
            title = "新建文件夹",
            initial = "new_folder",
            confirm = { name -> File(currentDir, name).mkdirs(); showNewFolder = false; refreshTick++ },
            dismiss = { showNewFolder = false }
        )
    }
    // 新建文件对话框
    if (showNewFile) {
        InputDialog(
            title = "新建文件",
            initial = "index.php",
            confirm = { name -> File(currentDir, name).writeText(""); showNewFile = false; refreshTick++ },
            dismiss = { showNewFile = false }
        )
    }
    // 文本文件编辑对话框
    editingFile?.let { f ->
        EditTextDialog(file = f, onDismiss = { editingFile = null }, onSaved = { refreshTick++ })
    }
}

@Composable
private fun FileRow(file: File, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MiuiSurface),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (file.isDirectory) Amber else MiuiTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(file.name, fontSize = 14.sp, color = MiuiTextPrimary, fontWeight = FontWeight.Medium)
                Text(
                    if (file.isDirectory) "${file.listFiles()?.size ?: 0} 项"
                    else "${formatSize(file.length())} · ${SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(file.lastModified()))}",
                    fontSize = 11.sp,
                    color = MiuiTextSecondary
                )
            }
            TextButton(onClick = onDelete) { Text("删除", fontSize = 12.sp, color = MiuiRed) }
        }
    }
}

@Composable
private fun InputDialog(title: String, initial: String, confirm: (String) -> Unit, dismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (text.isNotBlank()) confirm(text.trim()) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("取消") } }
    )
}

@Composable
private fun EditTextDialog(file: File, onDismiss: () -> Unit, onSaved: () -> Unit) {
    var content by remember(file) { mutableStateOf(try { file.readText() } catch (_: Exception) { "" }) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(file.name, fontSize = 16.sp) },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.height(300.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
            )
        },
        confirmButton = {
            TextButton(onClick = {
                file.writeText(content)
                onDismiss(); onSaved()
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024.0)
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}
