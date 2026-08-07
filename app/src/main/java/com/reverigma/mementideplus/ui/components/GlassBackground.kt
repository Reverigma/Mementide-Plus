package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 液态玻璃背景：柔和双色渐变 + 顶部光斑（radial 高光），
 * 半透明玻璃卡片叠在其上即可透出光影形成玻璃质感。
 *
 * 用法一（包裹）：`GlassBackground { ... }` 包住页面内容根。
 * 用法二（铺渐变）：根 Column 上 `Modifier.background(glassBackgroundBrush())`。
 */
@Composable
fun glassBackgroundBrush(): Brush {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val base = MaterialTheme.colorScheme.background
    return if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0xFF1E2A4A), base, Color(0xFF14102A))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFE8EDFF), base, Color(0xFFFDF0F6))
        )
    }
}

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val accentGlow = if (isDark) Color(0x33256BB3) else Color(0x264F46E5)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glassBackgroundBrush())
    ) {
        // 顶部光斑（模拟天光/环境高光）
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentGlow, Color.Transparent),
                        radius = 900f
                    )
                )
        )
        content()
    }
}
