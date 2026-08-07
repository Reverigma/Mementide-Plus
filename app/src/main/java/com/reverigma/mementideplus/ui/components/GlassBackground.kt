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
        Brush.verticalGradient(
            listOf(Color(0xFF1B2A4A), Color(0xFF101827), Color(0xFF1A1233))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFE4EBFF), Color(0xFFF7F8FF), Color(0xFFFFEEF7))
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
        Glow(0.15f, -0.05f, Color(0x30256BB3), 900f),  // 靛蓝天光
        Glow(0.95f, 0.25f, Color(0x2E7C3AED), 800f),   // 紫光
        Glow(-0.1f, 0.75f, Color(0x260EA5E9), 840f),   // 青光
        Glow(1.05f, 1.05f, Color(0x2CDB2777), 760f),   // 粉光
        Glow(0.5f, 1.1f, Color(0x28C026D3), 720f)      // 品红光
    )
} else {
    listOf(
        Glow(0.12f, -0.08f, Color(0x284F46E5), 900f),  // 靛蓝
        Glow(1.0f, 0.2f, Color(0x208B5CF6), 760f),     // 紫
        Glow(-0.05f, 0.7f, Color(0x200EA5E9), 800f),   // 蓝
        Glow(1.02f, 1.0f, Color(0x1EF472B6), 700f),    // 粉
        Glow(0.45f, 1.12f, Color(0x1EF59E0B), 680f)    // 暖橙
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
