package com.mobileserver.ui

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mobileserver.core.Paths
import com.mobileserver.ui.theme.*
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TEXT_EXT = setOf("php", "html", "htm", "txt", "js", "css", "json", "conf", "ini", "log", "sh", "xml", "yml", "yaml", "md", "pdf")
private val MEDIA_EXT = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "mp4", "mkv", "webm", "mov", "avi", "m4v", "mp3", "flac", "wav", "aac", "ogg", "m4a")

@Composable
fun FilesScreen(navController: NavController) {
    val context = LocalContext.current
    var currentDir by remember { mutableStateOf(Paths.wwwDir) }
    var showNewFolder by remember { mutableStateOf(false) }
    var showNewFile by remember { mutableStateOf(false) }
    var editingFile by remember { mutableStateOf<File?>(null) }
    var renamingFile by remember { mutableStateOf<File?>(null) }
    var detailFile by remember { mutableStateOf<File?>(null) }
    var query by remember { mutableStateOf("") }
    var refreshTick by remember { mutableStateOf(0) }

    // 上传手机文件到当前目录
    val uploadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val name = queryDisplayName(context, it) ?: "upload_${System.currentTimeMillis()}"
            runCatching {
                context.contentResolver.openInputStream(it)?.use { input ->
                    File(currentDir, name).outputStream().use { out -> input.copyTo(out) }
                }
            }.onSuccess { Toast.makeText(context, "已上传 $name", Toast.LENGTH_SHORT).show() }
             .onFailure { Toast.makeText(context, "上传失败: ${it.message}", Toast.LENGTH_LONG).show() }
            refreshTick++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("文件管理", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
        Spacer(Modifier.height(10.dp))

        // 工具行
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AssistChip(onClick = { if (currentDir.parentFile != null) currentDir = currentDir.parentFile!! }, label = { Text("上级") })
            AssistChip(onClick = { currentDir = Paths.wwwDir }, label = { Text("根目录") })
            AssistChip(onClick = { refreshTick++ }, label = { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp)) })
            Spacer(Modifier.weight(1f))
            AssistChip(onClick = { showNewFolder = true }, label = { Text("新建夹") })
            AssistChip(onClick = { showNewFile = true }, label = { Text("新文件") })
            AssistChip(onClick = { uploadLauncher.launch("*/*") }, label = { Text("上传") })
        }
        Spacer(Modifier.height(8.dp))

        // 搜索框
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索当前目录…", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        // 当前路径
        Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Text(currentDir.absolutePath, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 11.sp, color = MiuiTextSecondary)
        }
        Spacer(Modifier.height(8.dp))

        val files = remember(currentDir, refreshTick, query) {
            (currentDir.listFiles()?.toList() ?: emptyList())
                .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
                .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        }
        if (files.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (query.isBlank()) "空目录" else "无匹配", color = MiuiTextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(files) { f ->
                    FileRow(
                        file = f,
                        onClick = {
                            when {
                                f.isDirectory -> currentDir = f
                                f.extension.lowercase() in TEXT_EXT -> editingFile = f
                                f.extension.lowercase() in MEDIA_EXT -> {
                                    val rel = f.absolutePath.removePrefix(Paths.wwwDir.absolutePath).removePrefix("/")
                                    val fileUrl = "http://127.0.0.1:5244/d/" + URLEncoder.encode(rel, "UTF-8")
                                    navController.navigate("preview?url=" + URLEncoder.encode(fileUrl, "UTF-8"))
                                }
                                else -> detailFile = f
                            }
                        },
                        onRename = { renamingFile = f },
                        onDetail = { detailFile = f },
                        onDelete = { f.delete(); refreshTick++ }
                    )
                }
            }
        }
    }

    if (showNewFolder) InputDialog("新建文件夹", "new_folder") { name -> File(currentDir, name).mkdirs(); showNewFolder = false; refreshTick++ }
    if (showNewFile) InputDialog("新建文件", "index.php") { name -> File(currentDir, name).writeText(""); showNewFile = false; refreshTick++ }
    editingFile?.let { EditTextDialog(it, { editingFile = null }) { refreshTick++ } }
    renamingFile?.let { old ->
        InputDialog("重命名", old.name) { newName ->
            old.renameTo(File(old.parentFile, newName))
            renamingFile = null; refreshTick++
        }
    }
    detailFile?.let { DetailDialog(it) { detailFile = null } }
}

@Composable
private fun FileRow(
    file: File,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDetail: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MiuiSurface),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                    fontSize = 11.sp, color = MiuiTextSecondary
                )
            }
            IconButton(onClick = onDetail) { Icon(Icons.Default.Info, "详情", tint = MiuiTextSecondary, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onRename) { Icon(Icons.Default.CreateNewFolder, "重命名", tint = Primary, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "删除", tint = MiuiRed, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun InputDialog(title: String, initial: String, confirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        text = { OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true) },
        confirmButton = { TextButton(onClick = { if (text.isNotBlank()) confirm(text.trim()) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = {}) { Text("取消") } }
    )
}

@Composable
private fun EditTextDialog(file: File, onDismiss: () -> Unit, onSaved: () -> Unit) {
    var content by remember(file) { mutableStateOf(try { file.readText() } catch (_: Exception) { "" }) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(file.name, fontSize = 16.sp) },
        text = { OutlinedTextField(value = content, onValueChange = { content = it }, modifier = Modifier.height(300.dp)) },
        confirmButton = { TextButton(onClick = { file.writeText(content); onDismiss(); onSaved() }) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun DetailDialog(file: File, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(file.name, fontSize = 16.sp) },
        text = {
            Column {
                DetailRow("类型", if (file.isDirectory) "文件夹" else "文件")
                DetailRow("路径", file.absolutePath)
                DetailRow("大小", if (file.isDirectory) "${file.listFiles()?.size ?: 0} 项" else formatSize(file.length()))
                DetailRow("修改时间", SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(file.lastModified())))
                DetailRow("可读", file.canRead().toString())
                DetailRow("可写", file.canWrite().toString())
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun DetailRow(k: String, v: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("$k：", fontSize = 13.sp, color = MiuiTextSecondary)
        Text(v, fontSize = 13.sp, color = MiuiTextPrimary)
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024.0)
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
        }
    }.getOrNull()
}
