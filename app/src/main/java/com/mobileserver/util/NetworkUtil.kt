package com.mobileserver.util

import java.net.NetworkInterface

object NetworkUtil {
    /** 获取本机局域网 IP（WiFi 地址） */
    fun getLocalIp(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (iface in interfaces) {
                if (!iface.isUp || iface.isLoopback || iface.isVirtual) continue
                // 优先 wlan0
                if (!iface.name.startsWith("wlan") && !iface.name.startsWith("eth")) continue
                for (addr in iface.inetAddresses) {
                    val ip = addr.hostAddress
                    if (ip != null && ip.contains('.') && !ip.startsWith("127.")) {
                        return ip
                    }
                }
            }
            "127.0.0.1"
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }
}
