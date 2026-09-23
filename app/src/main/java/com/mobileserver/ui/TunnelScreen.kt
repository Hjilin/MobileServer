package com.mobileserver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileserver.core.Paths
import com.mobileserver.core.SettingsManager
import com.mobileserver.ui.theme.*
import java.io.File

/**
 * 内网穿透 / 远程访问页：
 * - frp (frpc) 控制：配置 frps 地址/令牌，把本机 8080 暴露到公网
 * - n2n 组网：引导用 Hin2n App（公共节点）
 * - WebDAV：OpenList 内置，替代 FTP
 */
@Composable
fun TunnelScreen() {
    val context = LocalContext.current
    var frpServer by remember { mutableStateOf(SettingsManager.getFrpServer(context)) }
    var frpToken by remember { mutableStateOf(SettingsManager.getFrpToken(context)) }
    var frpRemotePort by remember { mutableStateOf(SettingsManager.getFrpRemotePort(context).toString()) }
    var frpRunning by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MiuiBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("内网穿透 / 远程访问", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)

        // ===== frp =====
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cloud, null, tint = Primary)
                    Spacer(Modifier.width(10.dp))
                    Text("frp 公网穿透", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = frpServer, onValueChange = { frpServer = it }, label = { Text("frps 服务器地址") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = frpToken, onValueChange = { frpToken = it }, label = { Text("认证令牌 token") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = frpRemotePort, onValueChange = { frpRemotePort = it }, label = { Text("远程端口") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            runCatching {
                                SettingsManager.setFrpServer(context, frpServer)
                                SettingsManager.setFrpToken(context, frpToken)
                                SettingsManager.setFrpRemotePort(context, frpRemotePort.toIntOrNull() ?: 8080)
                                val conf = """
                                    serverAddr = "${frpServer.trim()}"
                                    serverPort = 7000
                                    auth.token = "${frpToken.trim()}"
                                    [[proxies]]
                                    name = "mobileserver-web"
                                    type = "tcp"
                                    localIP = "127.0.0.1"
                                    localPort = 8080
                                    remotePort = ${frpRemotePort.trim()}
                                """.trimIndent()
                                File(Paths.confDir, "frpc.toml").writeText(conf)
                                val frpc = File(Paths.compDir("frp"), "bin/frpc")
                                val pb = ProcessBuilder(frpc.absolutePath, "-c", File(Paths.confDir, "frpc.toml").absolutePath)
                                pb.directory(Paths.serverRoot)
                                pb.start()
                                frpRunning = true
                                log = "frpc 已启动，配置已保存"
                            }.onFailure { log = "启动失败: ${it.message}" }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.weight(1f)
                    ) { Text("启动 frpc", fontSize = 13.sp) }
                    OutlinedButton(onClick = {
                        runCatching {
                            Runtime.getRuntime().exec("pkill -f frpc")
                        }
                        frpRunning = false
                        log = "frpc 已停止"
                    }, modifier = Modifier.weight(1f)) { Text("停止", fontSize = 13.sp) }
                }
                if (log.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(log, fontSize = 12.sp, color = MiuiTextSecondary)
                }
            }
        }

        // ===== n2n =====
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, null, tint = Primary2)
                    Spacer(Modifier.width(10.dp))
                    Text("n2n P2P 组网", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                }
                Spacer(Modifier.height(10.dp))
                InfoRow("公共节点", SettingsManager.getN2nSupernode(context))
                InfoRow("社区名", SettingsManager.getN2nCommunity(context))
                InfoRow("密钥", SettingsManager.getN2nPassword(context))
                InfoRow("虚拟IP", SettingsManager.getN2nVirtualIp(context))
                Spacer(Modifier.height(8.dp))
                Text("手机安装 Hin2n App，导入以上配置即可与本机组成虚拟局域网。", fontSize = 12.sp, color = MiuiTextSecondary)
            }
        }

        // ===== WebDAV / FTP =====
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MiuiSurface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, null, tint = Amber)
                    Spacer(Modifier.width(10.dp))
                    Text("WebDAV / 文件访问", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MiuiTextPrimary)
                }
                Spacer(Modifier.height(10.dp))
                InfoRow("WebDAV", "http://127.0.0.1:5244/dav/")
                InfoRow("网盘面板", "http://127.0.0.1:5244")
                InfoRow("统一入口", "http://127.0.0.1:8080/alist/")
                Spacer(Modifier.height(8.dp))
                Text("OpenList 已内置 WebDAV，可直接用文件管理器/RaiSync 等客户端连接，无需单独装 FTP。", fontSize = 12.sp, color = MiuiTextSecondary)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = MiuiTextSecondary, modifier = Modifier.width(90.dp))
        Text(value, fontSize = 12.sp, color = Primary)
    }
}
