package com.reverigma.mementideplus.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 检查更新工具：查询 GitHub Releases 最新版本，与本地版本比对；支持应用内直接下载 APK。
 * 仅由「设置 → 检查更新」手动触发，不自动检查、不强制更新。
 */
object UpdateChecker {

    /** 仓库最新 release 的 API 地址（免鉴权，公开仓库可读） */
    private const val RELEASES_URL = "https://api.github.com/repos/Reverigma/Mementide-Plus/releases/latest"

    /** 最新版本信息 */
    data class LatestRelease(val version: String, val apkUrl: String)

    /**
     * 查询远端最新版本号与 APK 下载地址，失败返回 null。
     * 在 IO 线程执行网络请求。
     */
    suspend fun fetchLatestRelease(): LatestRelease? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(RELEASES_URL).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "MementidePlus")
            try {
                if (conn.responseCode != 200) {
                    null
                } else {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(body)
                    val tag = json.optString("tag_name").takeIf { it.isNotBlank() } ?: return@withContext null
                    // 优先取 assets 里的 APK 下载地址（发布时统一命名为 MementidePlus-vX.Y.Z.apk）
                    var apkUrl = ""
                    val assets = json.optJSONArray("assets")
                    if (assets != null && assets.length() > 0) {
                        apkUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                    }
                    if (apkUrl.isBlank()) {
                        apkUrl = "https://github.com/Reverigma/Mementide-Plus/releases/download/$tag/MementidePlus-$tag.apk"
                    }
                    LatestRelease(tag, apkUrl)
                }
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 下载 APK 到本地文件，带进度回调（已下载字节数 / 总字节数）。
     * @return 是否成功
     */
    suspend fun downloadApk(url: String, destFile: File, onProgress: (done: Long, total: Long) -> Unit): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val conn = URL(url).openConnection() as HttpURLConnection
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                conn.setRequestProperty("User-Agent", "MementidePlus")
                try {
                    if (conn.responseCode != 200) return@withContext false
                    val total = conn.contentLength.toLong()
                    conn.inputStream.use { input ->
                        FileOutputStream(destFile).use { output ->
                            val buf = ByteArray(8192)
                            var done = 0L
                            var read: Int
                            while (input.read(buf).also { read = it } != -1) {
                                output.write(buf, 0, read)
                                done += read
                                onProgress(done, total)
                            }
                        }
                    }
                    destFile.length() > 0
                } finally {
                    conn.disconnect()
                }
            } catch (_: Exception) {
                false
            }
        }

    /**
     * 版本号比较。支持 "v0.5.4" / "0.5.4" / "0.5.4.1" 等点分格式。
     * 返回 >0 表示 a 更新，<0 表示 b 更新，0 表示相同。
     */
    fun compareVersions(a: String, b: String): Int {
        fun parse(s: String): List<Int> =
            s.trim().removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }

        val pa = parse(a)
        val pb = parse(b)
        val n = maxOf(pa.size, pb.size)
        for (i in 0 until n) {
            val x = pa.getOrElse(i) { 0 }
            val y = pb.getOrElse(i) { 0 }
            if (x != y) return x - y
        }
        return 0
    }
}
