package com.mobileserver.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobileserver.ui.theme.*

/**
 * 网盘管理页：OpenList(AList) 状态、访问入口、目录互通、n2n 远程组网。
 * 对齐简云卡片风格。
 */
@Composable
fun NetDiskScreen(viewModel: ServerViewModel = viewModel()) {
    val services by viewModel.services.collectAsState()
    val openlist = services.firstOrNull { it.name.contains("OpenList", ignoreCase = true) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    // n2n 公共免费节点（截图教程提供，P2P 打洞，无需公网 IP）
    val superNode = "wang.switchy.hin2n.stars:10086"
    val community = "nas-network"
    val password = "nas123456"
    val virtualIp = "192.168.100.1"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ===== OpenList 状态 =====
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = Primary)
                    Spacer(Modifier.width(10.dp))
                    Text("OpenList 网盘", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    if (openlist?.isRunning == true) "运行中 · 端口 5244" else "未启动",
                    fontSize = 13.sp,
                    color = if (openlist?.isRunning == true) MiuiGreen else MiuiTextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.toggle(openlist!!.type) },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = openlist != null
                ) {
                    Text(if (openlist?.isRunning == true) "停止网盘服务" else "启动网盘服务")
                }
            }
        }

        // ===== 访问入口 =====
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("访问入口", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                AccessLine("统一入口", "http://127.0.0.1:8080/alist/")
                AccessLine("直连端口", "http://127.0.0.1:5244/")
            }
        }

        // ===== 目录互通 =====
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("目录互通", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                Spacer(Modifier.height(10.dp))
                AccessLine("网站目录", "/files/server/www")
                AccessLine("网盘缓存", "/files/server/storage")
                AccessLine("打通说明", "网盘可浏览网站文件，PHP 可读取网盘内容")
            }
        }

        // ===== n2n 远程组网（星空组网）=====
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MiuiSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Router, contentDescription = null, tint = Primary2)
                    Spacer(Modifier.width(10.dp))
                    Text("远程组网 n2n", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                }
                Spacer(Modifier.height(4.dp))
                Text("免费 P2P 打洞，无需公网 IP / 云服务器", fontSize = 12.sp, color = MiuiTextSecondary)
                Spacer(Modifier.height(12.dp))
                AccessLine("超级节点", superNode)
                AccessLine("社区名", community)
                AccessLine("加密密钥", password)
                AccessLine("虚拟 IP", virtualIp)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val cfg = "n2n 组网配置：\n超级节点: $superNode\n社区名: $community\n密钥: $password\n虚拟IP: $virtualIp"
                        clipboard.setText(AnnotatedString(cfg))
                        Toast.makeText(context, "配置已复制，打开 Hin2n 导入即可", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("复制组网配置")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "使用：手机安装 Hin2n App → 导入上面配置 → 启动；其他设备填同样的社区名和密钥即可互通。",
                    fontSize = 12.sp,
                    color = MiuiTextSecondary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
