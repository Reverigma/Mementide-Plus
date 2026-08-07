package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 液态玻璃背景：暗房式虚化光斑（多个大半径 radial 渐变叠加，模拟毛玻璃背后的虚化光影）。
 * 光斑本身是软渐变（天然"虚焦"），玻璃卡片叠上去就有通透的模糊质感。
 *
 * 用法一（包裹）：`GlassBackground { ... }` 包住页面内容根。
 * 用法二（铺渐变）：根 Column 上 `Modifier.background(glassBackgroundBrush())`。
 */
@Composable
fun glassBackgroundBrush(): Brush {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) {
        // 深色白透：深灰 → 微冷深灰（不彩）
        Brush.verticalGradient(
            listOf(Color(0xFF1A1C22), Color(0xFF14151A), Color(0xFF1C181E))
        )
    } else {
        // 白透：纯白 → 微冷白 → 微暖白（不彩，只有极淡的冷暖倾向）
        Brush.verticalGradient(
            listOf(Color(0xFFFFFFFF), Color(0xFFF7F9FF), Color(0xFFFFFCF8))
        )
    }
}

/** 光斑定义：位置（比例 0~1）+ 颜色 + 半径 */
private data class Glow(
    val x: Float,
    val y: Float,
    val color: Color,
    val radius: Float
)

@Composable
private fun glows(isDark: Boolean): List<Glow> = if (isDark) {
    listOf(
        Glow(0.15f, -0.05f, Color(0x14FFFFFF), 900f),  // 白色天光
        Glow(0.95f, 0.25f, Color(0x0FFFFFFF), 800f),
        Glow(0.5f, 1.1f, Color(0x0CFFFFFF), 720f)
    )
} else {
    // 白透：全部用极淡的白色光晕（模拟天光漫射），不引入彩色
    listOf(
        Glow(0.12f, -0.08f, Color(0x33FFFFFF), 900f),  // 白亮光晕
        Glow(1.0f, 0.2f, Color(0x26FFFFFF), 800f),
        Glow(0.5f, 1.1f, Color(0x1AFFFFFF), 720f)
    )
}

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glassBackgroundBrush())
    ) {
        Box(Modifier.fillMaxSize()) {
            glows(isDark).forEach { g ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(g.color, Color.Transparent),
                                center = Offset(g.x, g.y),
                                radius = g.radius
                            )
                        )
                )
            }
        }
        content()
    }
}
