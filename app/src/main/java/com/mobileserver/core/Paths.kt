package com.mobileserver.core

import android.content.Context
import java.io.File

/**
 * 统一路径管理：所有服务共享同一根目录，实现文件系统深度互通
 * 这是整个项目互通架构的核心：网站目录、网盘缓存、二进制、日志全部集中管理
 */
object Paths {
    lateinit var rootDir: File
        private set

    // 统一根目录结构
    val serverRoot get() = File(rootDir, "server")
    val binDir get() = File(serverRoot, "bin")
    val wwwDir get() = File(serverRoot, "www")          // Nginx PHP站点根目录
    val storageDir get() = File(serverRoot, "storage") // OpenList本地网盘缓存目录
    val dataDir get() = File(serverRoot, "data")       // MariaDB/Redis持久化数据
    val logsDir get() = File(serverRoot, "logs")      // 全部服务统一日志目录
    val confDir get() = File(serverRoot, "conf")      // 统一配置文件目录

    // 各服务二进制路径
    val nginxBin get() = File(binDir, "nginx/nginx")
    val phpCgiBin get() = File(binDir, "php/php-cgi")
    val mariadbBin get() = File(binDir, "mariadb/mariadbd")
    val redisBin get() = File(binDir, "redis/redis-server")
    val openlistBin get() = File(binDir, "openlist/openlist")

    fun init(context: Context) {
        rootDir = context.filesDir
        // 自动创建所有目录
        listOf(binDir, wwwDir, storageDir, dataDir, logsDir, confDir).forEach {
            it.mkdirs()
        }
    }

    /**
     * 赋予二进制可执行权限
     */
    fun setExecutable() {
        listOf(nginxBin, phpCgiBin, mariadbBin, redisBin, openlistBin).forEach { bin ->
            if (bin.exists()) {
                bin.setExecutable(true, false)
            }
        }
    }
}
