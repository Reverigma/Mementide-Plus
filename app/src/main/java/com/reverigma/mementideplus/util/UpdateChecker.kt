package com.reverigma.mementideplus.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 检查更新工具：查询最新版本与 APK 下载地址，支持 Gitee / GitHub 双更新源。
 * 仅由「设置 → 检查更新」手动触发，不自动检查、不强制更新。
 */
object UpdateChecker {

    /** 更新源标识 */
    const val SOURCE_GITEE = "gitee"
    const val SOURCE_GITHUB = "github"

    private const val OWNER = "Reverigma"
    private const val REPO = "Mementide-Plus"

    /** 浏览器风格 UA：Gitee 对非浏览器 UA 的匿名 API 请求风控较严（会 404/403） */
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 13; MementidePlus) AppleWebKit/537.36"

    /** 各源最新 release API（公开仓库免鉴权） */
    private fun latestApiUrl(source: String): String = when (source) {
        SOURCE_GITEE -> "https://gitee.com/api/v5/repos/$OWNER/$REPO/releases/latest"
        else -> "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    }

    /** 各源 APK 直链前缀：releases/download/{tag}/MementidePlus-{tag}.apk（命名固定，可离线拼） */
    private fun downloadBase(source: String): String = when (source) {
        SOURCE_GITEE -> "https://gitee.com/$OWNER/$REPO/releases/download"
        else -> "https://github.com/$OWNER/$REPO/releases/download"
    }

    /** 最新版本信息 */
    data class LatestRelease(val version: String, val apkUrl: String, val source: String)

    /**
     * 查询远端最新版本号与 APK 下载地址，失败返回 null。
     * Gitee 源：优先 API（浏览器 UA），失败兜底用网页重定向解析最新 tag。
     * 在 IO 线程执行网络请求。
     */
    suspend fun fetchLatestRelease(source: String): LatestRelease? = withContext(Dispatchers.IO) {
        val fromApi = queryLatestFromApi(source)
        if (fromApi != null) {
            fromApi
        } else if (source == SOURCE_GITEE) {
            // Gitee 兜底：/releases/latest 网页 302 重定向到最新版本页，解析 tag
            queryLatestFromGiteeRedirect()
        } else {
            null
        }
    }

    private fun queryLatestFromApi(source: String): LatestRelease? {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL(latestApiUrl(source)).openConnection() as HttpURLConnection
            conn!!.requestMethod = "GET"
            conn!!.connectTimeout = 8000
            conn!!.readTimeout = 8000
            conn!!.setRequestProperty("Accept", "application/json")
            conn!!.setRequestProperty("User-Agent", USER_AGENT)
            if (conn!!.responseCode != 200) {
                null
            } else {
                val body = conn!!.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val tag = json.optString("tag_name").takeIf { it.isNotBlank() } ?: return null
                // 优先取 assets 里的 APK 下载地址（Gitee 资产含源码 zip，需过滤 .apk）
                var apkUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val name = assets.getJSONObject(i).optString("name", "")
                        if (name.endsWith(".apk")) {
                            apkUrl = assets.getJSONObject(i).optString("browser_download_url", "")
                            break
                        }
                    }
                }
                // 兜底：APK 命名固定为 MementidePlus-{tag}.apk，直链可离线拼
                if (apkUrl.isBlank()) {
                    apkUrl = "${downloadBase(source)}/$tag/MementidePlus-$tag.apk"
                }
                LatestRelease(tag, apkUrl, source)
            }
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
        }
    }

    /** Gitee 网页兜底：请求 /releases/latest，从 302 Location 中解析最新版本 tag */
    private fun queryLatestFromGiteeRedirect(): LatestRelease? {
        var conn: HttpURLConnection? = null
        return try {
            conn = URL("https://gitee.com/$OWNER/$REPO/releases/latest").openConnection() as HttpURLConnection
            conn!!.requestMethod = "GET"
            conn!!.connectTimeout = 8000
            conn!!.readTimeout = 8000
            conn!!.instanceFollowRedirects = false
            conn!!.setRequestProperty("User-Agent", USER_AGENT)
            val loc = conn!!.getHeaderField("Location") ?: return null
            // Location 形如 .../releases/v0.5.21 或 v0.5.21
            val tag = Regex("v\\d+\\.\\d+\\.\\d+").find(loc)?.value ?: return null
            LatestRelease(tag, "${downloadBase(SOURCE_GITEE)}/$tag/MementidePlus-$tag.apk", SOURCE_GITEE)
        } catch (_: Exception) {
            null
        } finally {
            conn?.disconnect()
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
