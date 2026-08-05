package com.reverigma.mementideplus.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.reverigma.mementideplus.data.model.Habit

/**
 * 生成「打卡成就」分享卡片图（纯 Canvas 绘制，无 Compose 依赖）。
 * 输出 1200x1500 PNG。
 */
object AchievementCardGenerator {

    private const val W = 1200
    private const val H = 1500

    fun generate(habit: Habit, streak: Int, totalDone: Int, thisWeekDone: Int): Bitmap {
        val tint = habit.colorInt
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景：白色
        canvas.drawColor(Color.WHITE)

        // 顶部色带（习惯色半透明渐变块）
        val bandPaint = Paint().apply {
            color = tint
            alpha = 22
        }
        canvas.drawRoundRect(RectF(0f, 0f, W.toFloat(), 420f), 0f, 0f, bandPaint)
        val accentPaint = Paint().apply { color = tint }
        canvas.drawRoundRect(RectF(0f, 410f, W.toFloat(), 420f), 0f, 0f, accentPaint)

        // Emoji（大）
        val emojiPaint = Paint().apply {
            textSize = 220f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        canvas.drawText(habit.emoji, W / 2f, 300f, emojiPaint)

        // 习惯名
        val namePaint = Paint().apply {
            textSize = 64f
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#1F2937")
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        canvas.drawText(habit.name, W / 2f, 520f, namePaint)

        // 大数字：连续天数
        val bigNumPaint = Paint().apply {
            textSize = 300f
            textAlign = Paint.Align.CENTER
            color = tint
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        }
        canvas.drawText("$streak", W / 2f, 900f, bigNumPaint)

        // 「连续打卡 N 天」标签
        val labelPaint = Paint().apply {
            textSize = 52f
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#6B7280")
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        canvas.drawText(if (streak <= 0) "坚持记录每一天" else "连续打卡 $streak 天", W / 2f, 990f, labelPaint)

        // 分隔线
        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 4f
        }
        canvas.drawLine(200f, 1080f, W - 200f, 1080f, linePaint)

        // 底部数据：总完成 / 本周
        val statNumPaint = Paint().apply {
            textSize = 72f
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#1F2937")
            typeface = Typeface.create("sans-serif-bold", Typeface.BOLD)
        }
        val statLabelPaint = Paint().apply {
            textSize = 40f
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#9CA3AF")
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        canvas.drawText("$totalDone", W * 0.33f, 1210f, statNumPaint)
        canvas.drawText("$thisWeekDone", W * 0.67f, 1210f, statNumPaint)
        canvas.drawText("累计打卡", W * 0.33f, 1280f, statLabelPaint)
        canvas.drawText("本周打卡", W * 0.67f, 1280f, statLabelPaint)

        // 底部水印
        val watermarkPaint = Paint().apply {
            textSize = 36f
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#D1D5DB")
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        canvas.drawText("Mementide Plus · 时光记忆", W / 2f, 1400f, watermarkPaint)

        return bitmap
    }
}
