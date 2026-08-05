package com.reverigma.mementideplus.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.reverigma.mementideplus.data.model.Habit
import java.io.File

/**
 * 生成打卡成就图并分享（Android 10+ 自动存入相册，旧版本走缓存 + FileProvider）。
 */
object AchievementShare {

    fun share(context: Context, habit: Habit, streak: Int, totalDone: Int, thisWeekDone: Int) {
        val bmp = AchievementCardGenerator.generate(habit, streak, totalDone, thisWeekDone)
        shareBitmap(context, bmp, "我在 Mementide Plus 连续打卡 $streak 天（${habit.emoji} ${habit.name}）💪")
    }

    /** 分享纪念日海报 */
    fun shareAnniversary(context: Context, anniversary: com.reverigma.mementideplus.data.model.Anniversary, countdownDays: Long) {
        val bmp = AchievementCardGenerator.generateAnniversary(
            anniversary,
            countdownDays,
            AchievementCardGenerator.anniversaryDateLabel(anniversary)
        )
        val text = when {
            countdownDays > 0 -> "距离 ${anniversary.emoji} ${anniversary.name} 还有 $countdownDays 天"
            countdownDays == 0L -> "今天就是 ${anniversary.emoji} ${anniversary.name}！"
            else -> "${anniversary.emoji} ${anniversary.name} 已过 ${-countdownDays} 天"
        }
        shareBitmap(context, bmp, text)
    }

    /** 分享任意位图（存入相册 + 系统分享面板） */
    fun shareBitmap(context: Context, bmp: android.graphics.Bitmap, text: String) {
        val fileName = "mementide-${System.currentTimeMillis()}.png"
        val uri = save(context, bmp, fileName)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享海报"))
    }

    private fun save(context: Context, bmp: android.graphics.Bitmap, fileName: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/MementidePlus")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("无法创建媒体文件")
            resolver.openOutputStream(uri)?.use {
                bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            uri
        } else {
            val dir = File(context.cacheDir, "share").apply { mkdirs() }
            val file = File(dir, fileName)
            file.outputStream().use {
                bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
    }
}
