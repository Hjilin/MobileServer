package com.mobileserver.util

import com.mobileserver.core.Paths
import java.io.File

/**
 * 全局日志管理器：所有服务日志统一管理，支持筛选查看、一键清空
 */
object LogManager {

    data class LogEntry(val service: String, val time: String, val content: String)

    /**
     * 获取指定服务的日志文件路径
     */
    fun logFile(service: String): File = File(Paths.logsDir, "$service.log")

    /**
     * 追加日志
     */
    fun append(service: String, content: String) {
        val file = logFile(service)
        file.appendText("$content\n")
    }

    /**
     * 读取指定服务最新N行日志
     */
    fun readLastLines(service: String, maxLines: Int = 200): List<LogEntry> {
        val file = logFile(service)
        if (!file.exists()) return emptyList()
        return file.readLines()
            .takeLast(maxLines)
            .map { LogEntry(service, "", it) }
    }

    /**
     * 聚合读取全部服务日志（按服务分组）
     */
    fun readAll(maxLinesPerService: Int = 100): Map<String, List<LogEntry>> {
        val result = LinkedHashMap<String, List<LogEntry>>()
        listOf("nginx", "php-fpm", "mariadb", "redis", "openlist").forEach { svc ->
            result[svc] = readLastLines(svc, maxLinesPerService)
        }
        return result
    }

    /**
     * 一键清空所有日志
     */
    fun clearAll() {
        listOf("nginx", "php-fpm", "mariadb", "redis", "openlist").forEach { svc ->
            logFile(svc).delete()
        }
    }
}
