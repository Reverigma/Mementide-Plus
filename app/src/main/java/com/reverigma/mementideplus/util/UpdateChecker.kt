package com.reverigma.mementideplus.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 检查更新工具：查询 GitHub Releases 最新版本，与本地版本比对。
 * 仅由「设置 → 检查更新」手动触发，不自动检查、不强制更新。
 */
object UpdateChecker {

    /** 仓库最新 release 的 API 地址（免鉴权，公开仓库可读） */
    private const val RELEASES_URL = "https://api.github.com/repos/Reverigma/Mementide-Plus/releases/latest"

    /**
     * 查询远端最新版本号（如 "v0.5.4"），失败返回 null。
     * 在 IO 线程执行网络请求。
     */
    suspend fun fetchLatestVersion(): String? = withContext(Dispatchers.IO) {
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
                    json.optString("tag_name").takeIf { it.isNotBlank() }
                }
            } finally {
                conn.disconnect()
            }
        } catch (_: Exception) {
            null
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
