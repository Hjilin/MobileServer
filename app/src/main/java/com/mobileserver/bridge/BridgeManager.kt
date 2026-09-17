package com.mobileserver.bridge

import com.mobileserver.core.Paths
import java.io.File

/**
 * 核心互通层：打通LAMP与OpenList两套服务的文件、数据库、日志、目录映射
 * 这是"高度集成"的关键实现，解决两个项目割裂问题
 */
object BridgeManager {

    /** 目录映射开关：是否允许OpenList直接浏览/修改网站目录 */
    var webDirMappingEnabled: Boolean = true

    /** 统一数据库配置：一处修改，PHP网站和OpenList同时生效 */
    var dbHost: String = "127.0.0.1"
    var dbPort: Int = 3306
    var dbUser: String = "mobileserver"
    var dbPassword: String = "change_me"
    var dbNameWebsite: String = "website"
    var dbNameOpenList: String = "openlist"

    /**
     * 统一文件系统初始化：确保所有互通目录存在且权限正确
     */
    fun ensureAllDirectories() {
        Paths.serverRoot.mkdirs()
        listOf(Paths.binDir, Paths.wwwDir, Paths.storageDir, Paths.dataDir, Paths.logsDir, Paths.confDir).forEach { it.mkdirs() }
        // 网站目录与网盘缓存互相可见
        setDirPermission(Paths.wwwDir)
        setDirPermission(Paths.storageDir)
        setDirPermission(Paths.logsDir)
        setDirPermission(Paths.dataDir)
    }

    /**
     * 设置目录可读写
     */
    private fun setDirPermission(dir: File) {
        dir.setReadable(true, false)
        dir.setWritable(true, false)
        dir.setExecutable(true, false)
    }

    /**
     * 网站目录映射：当映射开关打开时，在网盘缓存目录创建指向网站目录的入口文件说明
     * 实际实现：生成webpanel可见的目录索引说明文件
     */
    fun applyWebDirMapping() {
        val notice = File(Paths.storageDir, "WEB_SITE_DIR.txt")
        if (webDirMappingEnabled) {
            notice.writeText(
                """
                【互通说明】
                网站根目录位于：${Paths.wwwDir.absolutePath}
                OpenList可直接将文件上传到 www 目录，PHP站点立即可访问。
                反向代理入口：
                - http://127.0.0.1:8080/web/  → 网站服务
                - http://127.0.0.1:8080/alist/ → OpenList网盘
                """.trimIndent()
            )
        } else {
            notice.delete()
        }
    }

    /**
     * 生成Nginx反向代理配置，统一入口
     * 将8080端口的 /alist/ 转发到 OpenList 5244端口
     */
    fun generateNginxConf(): File {
        val conf = File(Paths.confDir, "nginx.conf")
        val confContent = """
            worker_processes  1;
            events { worker_connections  1024; }
            http {
                include       mime.types;
                default_type  application/octet-stream;
                sendfile      on;
                keepalive_timeout  65;

                server {
                    listen       8080;
                    server_name  localhost;

                    # 管理面板入口
                    location / {
                        root   ${Paths.serverRoot.absolutePath}/www;
                        index  index.html index.htm;
                    }

                    # PHP站点
                    location /web/ {
                        alias ${Paths.wwwDir.absolutePath}/;
                        index  index.php index.html;
                    }

                    # PHP处理
                    location ~ \.php$ {
                        root           ${Paths.wwwDir.absolutePath};
                        fastcgi_pass   127.0.0.1:9000;
                        fastcgi_index  index.php;
                        fastcgi_param  SCRIPT_FILENAME  ${'$'}document_root${'$'}fastcgi_script_name;
                        include        fastcgi_params;
                    }

                    # OpenList反向代理：统一入口
                    location /alist/ {
                        proxy_pass http://127.0.0.1:5244/;
                        proxy_set_header Host ${'$'}host;
                        proxy_set_header X-Real-IP ${'$'}remote_addr;
                        proxy_set_header X-Forwarded-For ${'$'}proxy_add_x_forwarded_for;
                    }

                    error_page   500 502 503 504  /50x.html;
                    location = /50x.html {
                        root   ${Paths.serverRoot.absolutePath}/www;
                    }
                }
            }
        """.trimIndent()
        conf.writeText(confContent)
        return conf
    }

    /**
     * 生成统一数据库初始化SQL：创建website和openlist两个库
     * OpenList与PHP网站共用同一MariaDB实例
     */
    fun generateDbInitSql(): File {
        val sql = File(Paths.confDir, "init_db.sql")
        sql.writeText(
            """
            CREATE DATABASE IF NOT EXISTS $dbNameWebsite CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            CREATE DATABASE IF NOT EXISTS $dbNameOpenList CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
            CREATE USER IF NOT EXISTS '$dbUser'@'127.0.0.1' IDENTIFIED BY '$dbPassword';
            CREATE USER IF NOT EXISTS '$dbUser'@'localhost' IDENTIFIED BY '$dbPassword';
            GRANT ALL PRIVILEGES ON $dbNameWebsite.* TO '$dbUser'@'127.0.0.1';
            GRANT ALL PRIVILEGES ON $dbNameOpenList.* TO '$dbUser'@'127.0.0.1';
            GRANT ALL PRIVILEGES ON $dbNameWebsite.* TO '$dbUser'@'localhost';
            GRANT ALL PRIVILEGES ON $dbNameOpenList.* TO '$dbUser'@'localhost';
            FLUSH PRIVILEGES;
            """.trimIndent()
        )
        return sql
    }
}
