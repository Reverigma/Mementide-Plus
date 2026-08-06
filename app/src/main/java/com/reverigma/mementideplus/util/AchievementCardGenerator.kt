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

    /**
     * 生成习惯成就海报。
     * @param iconBmp Material 图标位图（可选，null 时回退首字母）
     * @param background 背景图（可选，习惯关联图片；cover 铺满 + 半透明白遮罩保证文字可读）
     */
    fun generate(
        habit: Habit, streak: Int, totalDone: Int, thisWeekDone: Int,
        iconBmp: Bitmap? = null,
        background: Bitmap? = null
    ): Bitmap {
        val tint = habit.colorInt
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景：白色（无背景图时）
        canvas.drawColor(Color.WHITE)
        drawBackground(canvas, background)

        // 顶部色带（习惯色半透明渐变块）
        val bandPaint = Paint().apply {
            color = tint
            alpha = 22
        }
        canvas.drawRoundRect(RectF(0f, 0f, W.toFloat(), 420f), 0f, 0f, bandPaint)
        val accentPaint = Paint().apply { color = tint }
        canvas.drawRoundRect(RectF(0f, 410f, W.toFloat(), 420f), 0f, 0f, accentPaint)

        // 图标（大）：习惯色圆 + Material 图标位图（或首字母兜底）
        drawIcon(canvas, tint, habit.iconName.ifBlank { "star" }, 300f, iconBmp)

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
     * @param iconBmp Material 图标位图（可选，null 时回退首字母）
     * @param background 背景图（可选，纪念日关联图片；cover 铺满 + 半透明白遮罩保证文字可读）
     */
    fun generateAnniversary(
        anniversary: Anniversary,
        countdownDays: Long,
        dateLabel: String,
        iconBmp: Bitmap? = null,
        background: Bitmap? = null
    ): Bitmap {
        val tint = anniversary.colorInt
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景：白色（无背景图时）
        canvas.drawColor(Color.WHITE)
        drawBackground(canvas, background)

        // 顶部色带（纪念日色半透明渐变块）
        val bandPaint = Paint().apply {
            color = tint
            alpha = 22
        }
        canvas.drawRoundRect(RectF(0f, 0f, W.toFloat(), 420f), 0f, 0f, bandPaint)
        val accentPaint = Paint().apply { color = tint }
        canvas.drawRoundRect(RectF(0f, 410f, W.toFloat(), 420f), 0f, 0f, accentPaint)

        // 图标（大）：纪念日色圆 + Material 图标位图（或首字母兜底）
        drawIcon(canvas, tint, anniversary.iconName.ifBlank { "cake" }, 300f, iconBmp)

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

    /** 海报背景图绘制：cover 铺满整卡 + 半透明白遮罩（保证文字可读） */
    private fun drawBackground(canvas: Canvas, background: Bitmap?) {
        if (background == null) return
        val bw = background.width.toFloat()
        val bh = background.height.toFloat()
        if (bw <= 0 || bh <= 0) return
        val scale = maxOf(W / bw, H / bh)
        val sw = W / scale
        val sh = H / scale
        val src = android.graphics.Rect(
            ((bw - sw) / 2f).toInt(), ((bh - sh) / 2f).toInt(),
            ((bw + sw) / 2f).toInt(), ((bh + sh) / 2f).toInt()
        )
        canvas.drawBitmap(
            background,
            src,
            RectF(0f, 0f, W.toFloat(), H.toFloat()),
            Paint().apply { isAntiAlias = true }
        )
        // 80% 白遮罩：背景图隐约可见，文字清晰
        canvas.drawColor(Color.argb(204, 255, 255, 255))
    }

    /**
     * 海报图标绘制：与卡片观感一致——浅色圆形底（tint 20%）+ tint 色图标。
     * 图标位图已裁剪掉空白边缘，这里按内容等比缩放（最长边 = 圆径 55%）居中。
     */
    private fun drawIcon(canvas: Canvas, tint: Int, iconName: String, baselineY: Float, iconBmp: Bitmap?) {
        val cx = W / 2f
        val cy = baselineY - 120f
        val r = 140f
        // 浅色圆底（与卡片 IconBadge 的 20% alpha 一致）
        val circlePaint = Paint().apply {
            color = tint
            alpha = 51 // 20%
            isAntiAlias = true
        }
        canvas.drawCircle(cx, cy, r, circlePaint)

        if (iconBmp != null) {
            val iw = iconBmp.width.toFloat()
            val ih = iconBmp.height.toFloat()
            if (iw > 0 && ih > 0) {
                // 内容等比缩放：最长边为圆径 55%（卡片上图标/圆 = 22/44 = 50%，大图略放大）
                val target = r * 2f * 0.55f
                val scale = target / maxOf(iw, ih)
                val w2 = iw * scale
                val h2 = ih * scale
                canvas.drawBitmap(
                    iconBmp,
                    null,
                    RectF(cx - w2 / 2f, cy - h2 / 2f, cx + w2 / 2f, cy + h2 / 2f),
                    Paint().apply { isAntiAlias = true }
                )
                return
            }
        }
        // 兜底：tint 色首字母
        val letterPaint = Paint().apply {
            textSize = 150f
            textAlign = Paint.Align.CENTER
            color = tint
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            isAntiAlias = true
        }
        val letter = iconName.take(1).uppercase()
        canvas.drawText(letter, cx, cy + 52f, letterPaint)
    }
}
