package com.reverigma.mementideplus.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.reverigma.mementideplus.data.model.Anniversary
import com.reverigma.mementideplus.data.model.CALENDAR_LUNAR
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

        // Emoji（大）；icon 模式画圆形 + 首字母代替
        if (habit.iconName.isBlank()) {
            val emojiPaint = Paint().apply {
                textSize = 220f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            canvas.drawText(habit.emoji, W / 2f, 300f, emojiPaint)
        } else {
            drawIconFallback(canvas, tint, habit.iconName, 300f)
        }

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

    /**
     * 生成「纪念日倒数」分享海报图（风格与习惯成就卡一致）。
     * @param countdownDays 距下次纪念日的天数（>=0 还有N天；0=今天；<0 已过）
     * @param dateLabel 展示日期文本，如 "8月5日" 或 "农历八月初五"
     */
    fun generateAnniversary(
        anniversary: Anniversary,
        countdownDays: Long,
        dateLabel: String
    ): Bitmap {
        val tint = anniversary.colorInt
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景：白色
        canvas.drawColor(Color.WHITE)

        // 顶部色带（纪念日色半透明渐变块）
        val bandPaint = Paint().apply {
            color = tint
            alpha = 22
        }
        canvas.drawRoundRect(RectF(0f, 0f, W.toFloat(), 420f), 0f, 0f, bandPaint)
        val accentPaint = Paint().apply { color = tint }
        canvas.drawRoundRect(RectF(0f, 410f, W.toFloat(), 420f), 0f, 0f, accentPaint)

        // Emoji（大）；icon 模式画圆形 + 首字母代替
        if (anniversary.iconName.isBlank()) {
            val emojiPaint = Paint().apply {
                textSize = 220f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            canvas.drawText(anniversary.emoji, W / 2f, 300f, emojiPaint)
        } else {
            drawIconFallback(canvas, tint, anniversary.iconName, 300f)
        }

        // 纪念日名
        val namePaint = Paint().apply {
            textSize = 64f
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#1F2937")
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        canvas.drawText(anniversary.name, W / 2f, 520f, namePaint)

        // 大数字：倒计时天数
        val bigNumPaint = Paint().apply {
            textSize = 300f
            textAlign = Paint.Align.CENTER
            color = tint
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        }
        val bigText = when {
            countdownDays > 0 -> "$countdownDays"
            countdownDays == 0L -> "今"
            else -> "${-countdownDays}"
        }
        canvas.drawText(bigText, W / 2f, 900f, bigNumPaint)

        // 标签
        val labelPaint = Paint().apply {
            textSize = 52f
            textAlign = Paint.Align.CENTER
            color = Color.parseColor("#6B7280")
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val label = when {
            countdownDays > 0 -> "距离 $countdownDays 天"
            countdownDays == 0L -> "就是今天"
            else -> "已过 ${-countdownDays} 天"
        }
        canvas.drawText(label, W / 2f, 990f, labelPaint)

        // 分隔线
        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 4f
        }
        canvas.drawLine(200f, 1080f, W - 200f, 1080f, linePaint)

        // 底部数据：日期 / 重复
        val statNumPaint = Paint().apply {
            textSize = 60f
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
        canvas.drawText(dateLabel, W * 0.33f, 1210f, statNumPaint)
        canvas.drawText(anniversary.repeatTypeLabel(), W * 0.67f, 1210f, statNumPaint)
        canvas.drawText("纪念日", W * 0.33f, 1280f, statLabelPaint)
        canvas.drawText("重复周期", W * 0.67f, 1280f, statLabelPaint)

        // 备注（可选）
        if (anniversary.note.isNotBlank()) {
            val notePaint = Paint().apply {
                textSize = 44f
                textAlign = Paint.Align.CENTER
                color = Color.parseColor("#6B7280")
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val note = if (anniversary.note.length > 18) anniversary.note.take(18) + "…" else anniversary.note
            canvas.drawText(note, W / 2f, 1345f, notePaint)
        }

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

    /** 纪念日重复周期中文标签 */
    private fun Anniversary.repeatTypeLabel(): String = when (repeatType) {
        com.reverigma.mementideplus.data.model.REPEAT_YEARLY -> "每年"
        com.reverigma.mementideplus.data.model.REPEAT_MONTHLY -> "每月"
        else -> "不重复"
    }

    /** 纪念日日期显示（农历/公历） */
    fun anniversaryDateLabel(a: Anniversary): String {
        return if (a.calendarType == CALENDAR_LUNAR) {
            val parts = a.date.split("-")
            val m = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val d = parts.getOrNull(1)?.toIntOrNull() ?: 1
            "农历${LunarCalendar.lunarLabel(m, d)}"
        } else {
            DateUtils.formatDate(a.date)
        }
    }

    /** 海报上 Material Icon 的替代绘制：习惯色圆 + 白色首字符（icon 名首字母大写） */
    private fun drawIconFallback(canvas: Canvas, tint: Int, iconName: String, baselineY: Float) {
        val cx = W / 2f
        val cy = baselineY - 120f
        val r = 140f
        val circlePaint = Paint().apply {
            color = tint
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, r, circlePaint)
        val letterPaint = Paint().apply {
            textSize = 160f
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            isAntiAlias = true
        }
        val letter = iconName.take(1).uppercase()
        canvas.drawText(letter, cx, cy + 56f, letterPaint)
    }
}
