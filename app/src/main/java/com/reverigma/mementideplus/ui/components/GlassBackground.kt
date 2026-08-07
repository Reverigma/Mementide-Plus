package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 干净清爽背景（全 App 统一）：
 * - 渐变：白 → 微冷白（极淡蓝）→ 微暖白（极淡粉），视觉上就是干净的白
 * - 光斑：2~3 团极淡的彩色氛围光（4~8%），按屏幕尺寸正确分布，克制不抢戏
 * - 深色：深灰渐变 + 极淡白光斑
 *
 * 用法一（包裹）：`GlassBackground { ... }`
 * 用法二（铺背景）：根布局 `Modifier.then(glassBackgroundModifier())`
 */

/** 光斑定义：位置（比例 0~1）+ 颜色 + 半径（相对屏宽） */
private data class Glow(
    val x: Float,
    val y: Float,
    val color: Color,
    val radiusScale: Float
)

@Composable
private fun glows(isDark: Boolean): List<Glow> = if (isDark) {
    listOf(
        Glow(0.15f, -0.05f, Color(0x0FFFFFFF), 0.9f),   // 白色天光
        Glow(0.95f, 0.3f, Color(0x0AFFFFFF), 0.8f)
    )
} else {
    // 干净清爽：只留顶部淡靛蓝天光 + 右下淡粉氛围，其余全白
    listOf(
        Glow(0.12f, -0.08f, Color(0x1A4F46E5), 0.95f),   // 淡靛蓝天光（10%）
        Glow(0.95f, 0.9f, Color(0x12F472B6), 0.85f),     // 淡粉氛围（7%）
        Glow(0.55f, 1.05f, Color(0x0EF59E0B), 0.75f)     // 淡暖橙（5.5%）
    )
}

@Composable
fun glassBackgroundBrush(): Brush {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0xFF1A1C22), Color(0xFF14151A), Color(0xFF1C181E))
        )
    } else {
        // 干净清爽：白 → 微冷白 → 微暖白（色相极淡，视觉仍干净）
        Brush.verticalGradient(
            listOf(Color(0xFFF7F9FF), Color(0xFFFDFEFF), Color(0xFFFFFAFD))
        )
    }
}

/** 统一的页面背景修饰符：渐变 + 光斑（按屏幕尺寸正确分布） */
@Composable
fun glassBackgroundModifier(): Modifier {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glowList = glows(isDark)
    return Modifier
        .background(glassBackgroundBrush())
        .drawBehind {
            val w = size.width
            val h = size.height
            glowList.forEach { g ->
                val cx = w * g.x
                val cy = h * g.y
                val radius = w * g.radiusScale
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(g.color, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )
            }
        }
}

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize().then(glassBackgroundModifier())) {
        content()
    }
}
