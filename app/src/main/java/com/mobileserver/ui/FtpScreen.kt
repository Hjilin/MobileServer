package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileserver.ui.theme.*

@Composable
fun FtpScreen() {
    Column(
        Modifier.fillMaxSize().background(MiuiBackground).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("FTP 用户管理", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MiuiTextPrimary)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Text("添加")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("FTP 端口: 21", fontSize = 13.sp, color = MiuiTextSecondary)
                Text("状态: 未启动", fontSize = 13.sp, color = MiuiTextSecondary)
                Spacer(Modifier.height(12.dp))
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("启动 FTP 服务")
                }
            }
        }
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Text("使用说明", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MiuiTextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("手机和电脑连同一WiFi，用文件管理器访问 ftp://手机IP:21", fontSize = 12.sp, color = MiuiTextSecondary)
            }
        }
    }
}
