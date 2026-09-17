package com.mobileserver.util

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/**
 * 端口占用检测工具
 */
object PortChecker {

    /**
     * 检测端口是否被占用
     * @return true=端口被占用 false=端口空闲
     */
    fun isPortInUse(port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", port), 500)
                true
            }
        } catch (e: IOException) {
            false
        }
    }
}
