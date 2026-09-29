package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.mobileserver.ui.theme.*

@Composable
fun WebDavScreen() {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().background(MiuiBackground).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("WebDAV 共享", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                Text("状态: 随 Nginx 启动", fontSize = 13.sp, color = MiuiTextSecondary)
                Text("端口: 8080", fontSize = 13.sp, color = MiuiTextSecondary)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("重启 WebDAV")
                }
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("连接地址", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("http://手机IP:8080/dav/", fontSize = 13.sp, color = MiuiTextSecondary, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString("http://手机IP:8080/dav/"))
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, null, tint = Primary)
                    }
                }
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("使用说明", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MiuiTextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Windows: 此电脑 → 映射网络驱动器 → 输入上面地址", fontSize = 12.sp, color = MiuiTextSecondary)
                Text("安卓: ES文件浏览器 → 网络 → WebDAV", fontSize = 12.sp, color = MiuiTextSecondary)
                Text("iPhone: 文件 → 连接服务器 → 输入地址", fontSize = 12.sp, color = MiuiTextSecondary)
            }
        }
    }
}
