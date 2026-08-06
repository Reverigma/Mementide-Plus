package com.reverigma.mementideplus.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * 习惯/纪念日关联图片的存取：从系统相册选图后压缩存入应用私有目录（filesDir），
 * 只存路径，不依赖原始文件。删除/替换时清理旧文件。
 */
object ImageStore {

    /** 压缩后的最长边（px），海报位图 1200x1500，背景图 1200 足够 */
    private const val MAX_EDGE = 1200

    /**
     * 把相册选择的图片复制到应用私有目录并压缩。
     * @return 保存后的绝对路径；失败返回 null
     */
    fun saveFromUri(context: Context, uri: Uri, dirName: String): String? {
        return try {
            val dir = File(context.filesDir, dirName).apply { mkdirs() }
            val outFile = File(dir, "${UUID.randomUUID()}.jpg")
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            val w = options.outWidth
            val h = options.outHeight
            if (w <= 0 || h <= 0) return null

            var sample = 1
            while (w / sample > MAX_EDGE || h / sample > MAX_EDGE) sample *= 2
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null

            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, fos)
            }
            if (!bitmap.isRecycled) bitmap.recycle()
            outFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** 海报生成时解码背景图（采样缩小，避免 OOM） */
    fun decodeScaled(path: String): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)
            val w = options.outWidth
            val h = options.outHeight
            if (w <= 0 || h <= 0) return null
            var sample = 1
            while (w / sample > MAX_EDGE || h / sample > MAX_EDGE) sample *= 2
            BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        } catch (e: Exception) {
            null
        }
    }

    /** 删除旧图片文件（替换/移除时调用），忽略不存在或删除失败 */
    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            File(path).delete()
        } catch (_: Exception) {
        }
    }
}
