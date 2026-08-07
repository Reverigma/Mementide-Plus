package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * 液态玻璃卡片（iOS 26 Liquid Glass 质感）：
 * - 玻璃底：顶部略亮 → 底部略暗的垂直渐变（模拟玻璃透光厚度）
 * - 高光描边：上沿白色亮线 → 下沿渐隐（模拟玻璃边缘反光）
 * - 大圆角 + 柔和悬浮阴影（主色光晕）
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glass = if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0x2EFFFFFF), Color(0x1FFFFFFF), Color(0x14FFFFFF))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xE6FFFFFF), Color(0xD9FFFFFF), Color(0xC4FFFFFF))
        )
    }
    // 高光描边：顶部亮白 → 中段 40% → 底部近乎透明
    val borderBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0x66FFFFFF), Color(0x26FFFFFF), Color(0x0DFFFFFF))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFFFFFFF), Color(0x80FFFFFF), Color(0x26FFFFFF))
        )
    }
    val shadowColor = if (isDark) Color(0x40000000) else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)

    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                spotColor = shadowColor,
                ambientColor = shadowColor
            )
            .clip(shape)
            .background(glass)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
    ) {
        content()
    }
}
