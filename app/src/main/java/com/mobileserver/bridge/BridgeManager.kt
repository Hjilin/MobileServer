package com.mobileserver.bridge

import com.mobileserver.core.Paths
import java.io.File

object BridgeManager {

    var webDirMappingEnabled: Boolean = true
    var dbHost: String = "127.0.0.1"
    var dbPort: Int = 3306
    var dbUser: String = "mobileserver"
    var dbPassword: String = "change_me"
    var dbNameWebsite: String = "website"
    var dbNameOpenList: String = "openlist"

    fun ensureAllDirectories() {
        Paths.serverRoot.mkdirs()
        listOf(Paths.binDir, Paths.wwwDir, Paths.storageDir, Paths.dataDir, Paths.logsDir, Paths.confDir).forEach { it.mkdirs() }
        setDirPermission(Paths.wwwDir)
        setDirPermission(Paths.storageDir)
        setDirPermission(Paths.logsDir)
        setDirPermission(Paths.dataDir)
    }

    private fun setDirPermission(dir: File) {
        dir.setReadable(true, false)
        dir.setWritable(true, false)
        dir.setExecutable(true, false)
    }

    fun applyWebDirMapping() {
        val notice = File(Paths.storageDir, "WEB_SITE_DIR.txt")
        if (webDirMappingEnabled) {
            notice.writeText(
                """
                【互通说明】
                网站根目录：${Paths.wwwDir.absolutePath}
                OpenList可直接上传文件到www目录，PHP站点立即可访问。
                入口：http://127.0.0.1:8080/web/  → 网站
                入口：http://127.0.0.1:8080/alist/ → 网盘
                """.trimIndent()
            )
        } else {
            notice.delete()
        }
    }

    fun generateNginxConf(): File {
        val conf = File(Paths.confDir, "nginx.conf")
        val confContent = """
worker_processes  1;
events { worker_connections  1024; }
http {
    include       ${Paths.confDir.absolutePath}/mime.types;
    default_type  application/octet-stream;
    sendfile      on;
    keepalive_timeout  65;

    server {
        listen       8080;
        server_name  localhost;

        location / {
            root   ${Paths.serverRoot.absolutePath}/www;
            index  index.html index.htm;
        }

        location /web/ {
            alias ${Paths.wwwDir.absolutePath}/;
            index  index.php index.html;
        }

        location ~ \.php${'$'} {
            root           ${Paths.wwwDir.absolutePath};
            fastcgi_pass   127.0.0.1:9000;
            fastcgi_index  index.php;
            fastcgi_param  SCRIPT_FILENAME  ${'$'}document_root${'$'}fastcgi_script_name;
            fastcgi_param  QUERY_STRING     ${'$'}query_string;
            fastcgi_param  REQUEST_METHOD  ${'$'}request_method;
            fastcgi_param  CONTENT_TYPE     ${'$'}content_type;
            fastcgi_param  CONTENT_LENGTH   ${'$'}content_length;
        }

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

        // 写出 mime.types
        val mime = File(Paths.confDir, "mime.types")
        if (!mime.exists()) {
            mime.writeText("""
types {
    text/html                             html htm shtml;
    text/css                              css;
    application/xml                       xml;
    image/gif                             gif;
    image/jpeg                            jpeg jpg;
    application/javascript                js;
    image/png                             png;
    image/svg+xml                         svg svgz;
    application/json                      json;
    application/wasm                     wasm;
    application/octet-stream              bin exe dll;
    application/zip                       zip;
    application/pdf                       pdf;
    video/mp4                             mp4;
    audio/mpeg                            mp3;
    font/woff                             woff;
    font/woff2                            woff2;
}
            """.trimIndent())
        }
        return conf
    }

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
