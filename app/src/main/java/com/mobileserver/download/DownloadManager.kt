package com.mobileserver.download

import com.mobileserver.core.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile

/**
 * 组件下载管理器：读取manifest清单，下载二进制包，校验，解压
 */
class DownloadManager {
    private val client = OkHttpClient()
    // 指向本项目的二进制资源仓库 manifest
    private val manifestUrl = "https://raw.githubusercontent.com/Hjilin/mobileserver-bin-resources/main/manifest.json"

    data class Component(
        val name: String,
        val version: String,
        val url: String,
        val sha256: String
    )

    suspend fun fetchManifest(): List<Component> = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(manifestUrl).build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val componentsObj = json.getJSONObject("components")
            val components = mutableListOf<Component>()
            componentsObj.keys().forEach { key ->
                val item = componentsObj.getJSONObject(key)
                components.add(
                    Component(
                        name = key,
                        version = item.getString("version"),
                        url = item.getString("url"),
                        sha256 = item.getString("sha256")
                    )
                )
            }
            components
        }
    }

    /**
     * 下载并解压指定组件
     */
    suspend fun installComponent(comp: Component, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val zipFile = File(Paths.binDir, "${comp.name}.zip")
        // 下载
        val req = Request.Builder().url(comp.url).build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body ?: return@withContext
            val total = body.contentLength()
            var downloaded = 0L
            body.byteStream().use { input ->
                zipFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        onProgress(downloaded.toFloat() / total)
                    }
                }
            }
        }
        // 校验sha256
        val actualSha = sha256(zipFile)
        if (!actualSha.equals(comp.sha256, ignoreCase = true)) {
            zipFile.delete()
            throw SecurityException("SHA256校验失败: ${comp.name}")
        }
        // 解压到对应目录
        val targetDir = File(Paths.binDir, comp.name)
        targetDir.mkdirs()
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                val file = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.outputStream().use { out ->
                        zip.getInputStream(entry).copyTo(out)
                    }
                }
            }
        }
        zipFile.delete()
        Paths.setExecutable()
    }

    private fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                md.update(buffer, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
