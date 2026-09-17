package com.mobileserver.engine

import com.mobileserver.bridge.BridgeManager
import com.mobileserver.core.Paths
import com.mobileserver.util.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * 统一引擎控制器：管理全部5个服务的启动/停止/状态
 * 所有服务以独立进程运行，通过TCP本地socket通信，保持GPL隔离
 */
object EngineController {

    enum class ServiceType { NGINX, PHP, MARIADB, REDIS, OPENLIST }

    data class ServiceState(
        val type: ServiceType,
        val name: String,
        val port: Int,
        var isRunning: Boolean = false,
        var process: Process? = null
    )

    private val _services = MutableStateFlow(
        listOf(
            ServiceState(ServiceType.NGINX, "Nginx", 8080),
            ServiceState(ServiceType.PHP, "PHP-CGI", 9000),
            ServiceState(ServiceType.MARIADB, "MariaDB", 3306),
            ServiceState(ServiceType.REDIS, "Redis", 6379),
            ServiceState(ServiceType.OPENLIST, "OpenList", 5244)
        )
    )
    val services: StateFlow<List<ServiceState>> = _services.asStateFlow()

    /**
     * 启动指定服务
     */
    suspend fun start(type: ServiceType) = withContext(Dispatchers.IO) {
        val svc = _services.value.first { it.type == type }
        if (svc.isRunning) return@withContext
        val process = when (type) {
            ServiceType.NGINX -> startNginx()
            ServiceType.PHP -> startPhpCgi()
            ServiceType.MARIADB -> startMariaDB()
            ServiceType.REDIS -> startRedis()
            ServiceType.OPENLIST -> startOpenList()
        }
        svc.process = process
        svc.isRunning = process != null
        refresh()
    }

    /**
     * 停止指定服务
     */
    fun stop(type: ServiceType) {
        val svc = _services.value.first { it.type == type }
        svc.process?.destroy()
        svc.process = null
        svc.isRunning = false
        LogManager.append(svc.name.lowercase(), "[engine] 服务已停止")
        refresh()
    }

    /**
     * 一键启动全部（按依赖顺序：DB → Redis → PHP → Nginx → OpenList）
     */
    suspend fun startAll() {
        listOf(
            ServiceType.MARIADB,
            ServiceType.REDIS,
            ServiceType.PHP,
            ServiceType.NGINX,
            ServiceType.OPENLIST
        ).forEach { start(it) }
    }

    /**
     * 一键停止全部
     */
    fun stopAll() {
        _services.value.toList().forEach { stop(it.type) }
    }

    /**
     * 刷新状态到StateFlow
     */
    private fun refresh() {
        _services.value = _services.value.toList()
    }

    /**
     * 启动组件进程前统一设置：工作目录指向组件目录，
     * 并注入 LD_LIBRARY_PATH（含 lib/、dep/ 下随包分发的 so），否则进程因找不到库而启动失败。
     */
    private fun applyRuntimeEnv(pb: ProcessBuilder, comp: String) {
        pb.directory(Paths.compDir(comp))
        pb.environment()["LD_LIBRARY_PATH"] = Paths.ldLibraryPath(comp)
    }

    // ============ 各服务启动实现 ============

    private fun startNginx(): Process? {
        // 先生成反向代理配置（互通层核心）
        BridgeManager.applyWebDirMapping()
        val conf = BridgeManager.generateNginxConf()
        if (!Paths.nginxBin.exists()) {
            LogManager.append("nginx", "[error] nginx二进制不存在，请先在组件管理中下载")
            return null
        }
        val pb = ProcessBuilder(Paths.nginxBin.absolutePath, "-c", conf.absolutePath, "-p", Paths.serverRoot.absolutePath)
        applyRuntimeEnv(pb, "nginx")
        pb.redirectErrorStream(true)
        val p = pb.start()
        consumeOutput(p, "nginx")
        LogManager.append("nginx", "[engine] Nginx已启动 (反向代理统一入口 http://127.0.0.1:8080)")
        return p
    }

    private fun startPhpCgi(): Process? {
        if (!Paths.phpCgiBin.exists()) {
            LogManager.append("php-fpm", "[error] php-cgi二进制不存在，请先下载")
            return null
        }
        val pb = ProcessBuilder(Paths.phpCgiBin.absolutePath, "-b", "127.0.0.1:9000", "-c", Paths.confDir.absolutePath)
        applyRuntimeEnv(pb, "php")
        pb.redirectErrorStream(true)
        val p = pb.start()
        consumeOutput(p, "php-fpm")
        LogManager.append("php-fpm", "[engine] PHP-CGI已启动 (127.0.0.1:9000)")
        return p
    }

    private fun startMariaDB(): Process? {
        if (!Paths.mariadbBin.exists()) {
            LogManager.append("mariadb", "[error] mariadbd二进制不存在，请先下载")
            return null
        }
        val dataDir = File(Paths.dataDir, "mysql")
        dataDir.mkdirs()
        // 首次启动初始化数据目录并执行统一建库SQL
        if (!File(dataDir, "ibdata1").exists()) {
            val initPb = ProcessBuilder(Paths.mariadbBin.absolutePath, "--initialize-insecure", "--datadir=${dataDir.absolutePath}")
            applyRuntimeEnv(initPb, "mariadb")
            initPb.start().waitFor()
            // 初始化后写入统一数据库配置
            val initSql = BridgeManager.generateDbInitSql()
            val warmPb = ProcessBuilder(
                Paths.mariadbBin.absolutePath, "--datadir=${dataDir.absolutePath}",
                "--socket=${dataDir.absolutePath}/mysql.sock"
            )
            applyRuntimeEnv(warmPb, "mariadb")
            warmPb.start()
            // 执行init SQL（简化：由Web面板引导执行）
            LogManager.append("mariadb", "[engine] 已生成统一建库SQL: ${initSql.absolutePath}")
        }
        val pb = ProcessBuilder(
            Paths.mariadbBin.absolutePath,
            "--datadir=${dataDir.absolutePath}",
            "--port=3306",
            "--bind-address=127.0.0.1"
        )
        applyRuntimeEnv(pb, "mariadb")
        pb.redirectErrorStream(true)
        val p = pb.start()
        consumeOutput(p, "mariadb")
        LogManager.append("mariadb", "[engine] MariaDB已启动 (127.0.0.1:3306)")
        return p
    }

    private fun startRedis(): Process? {
        if (!Paths.redisBin.exists()) {
            LogManager.append("redis", "[error] redis-server二进制不存在，请先下载")
            return null
        }
        val pb = ProcessBuilder(
            Paths.redisBin.absolutePath,
            "--port", "6379",
            "--bind", "127.0.0.1",
            "--dir", Paths.dataDir.absolutePath
        )
        applyRuntimeEnv(pb, "redis")
        pb.redirectErrorStream(true)
        val p = pb.start()
        consumeOutput(p, "redis")
        LogManager.append("redis", "[engine] Redis已启动 (127.0.0.1:6379)")
        return p
    }

    private fun startOpenList(): Process? {
        if (!Paths.openlistBin.exists()) {
            LogManager.append("openlist", "[error] openlist二进制不存在，请先下载")
            return null
        }
        val pb = ProcessBuilder(
            Paths.openlistBin.absolutePath,
            "server",
            "--port", "5244",
            "--database", "sqlite",
            "--dbpath", "${Paths.dataDir}/openlist.db"
        )
        pb.directory(Paths.storageDir)
        pb.redirectErrorStream(true)
        val p = pb.start()
        consumeOutput(p, "openlist")
        LogManager.append("openlist", "[engine] OpenList已启动 (经Nginx /alist/ 统一入口访问)")
        return p
    }

    private fun consumeOutput(process: Process, tag: String) {
        Thread {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.forEachLine { line ->
                LogManager.append(tag, line)
            }
        }.start()
    }
}
