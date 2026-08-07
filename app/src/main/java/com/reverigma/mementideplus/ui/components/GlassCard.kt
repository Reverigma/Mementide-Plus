package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
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
 * - 玻璃底：顶部亮 → 底部暗的垂直渐变（透光厚度）
 * - 内高光：卡片内部上沿一道白色渐变光带（玻璃上沿反光）
 * - 边缘融化：底部渐变为全透明，卡片"融"进背景（iOS 玻璃标志性特征）
 * - 高光描边：上沿亮白线 → 下沿渐隐（边缘反光）
 * - 大圆角 + 柔和悬浮阴影
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // 玻璃底：顶部亮 → 底部微暗（几乎不透明，与背景拉开层次；不做全透明"融化"以免和浅背景糊在一起）
    val glass = if (isDark) {
        Brush.verticalGradient(
            0f to Color(0x38FFFFFF),   // 顶部 22% 白
            0.55f to Color(0x24FFFFFF), // 中部 14%
            1f to Color(0x1AFFFFFF)     // 底部 10%
        )
    } else {
        Brush.verticalGradient(
            0f to Color(0xF7FFFFFF),   // 顶部 97% 白（几乎实白）
            0.55f to Color(0xEBFFFFFF), // 中部 92%
            1f to Color(0xDEFFFFFF)     // 底部 87%
        )
    }
    // 内高光光带：卡片上沿一道亮白渐变
    val innerGlow = if (isDark) {
        Brush.verticalGradient(
            0f to Color(0x59FFFFFF),
            0.22f to Color(0x14FFFFFF),
            1f to Color(0x00FFFFFF)
        )
    } else {
        Brush.verticalGradient(
            0f to Color(0xB3FFFFFF),
            0.25f to Color(0x40FFFFFF),
            1f to Color(0x00FFFFFF)
        )
    }
    // 高光描边：上沿亮白 → 中段 50% → 底部渐隐
    val borderBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0x80FFFFFF), Color(0x33FFFFFF), Color(0x0DFFFFFF))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFFFFFFF), Color(0xB3FFFFFF), Color(0x33FFFFFF))
        )
    }
    val shadowColor = if (isDark) Color(0x50000000) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                spotColor = shadowColor,
                ambientColor = shadowColor
            )
            .clip(shape)
            .background(glass)
            .border(width = 2.dp, brush = borderBrush, shape = shape)
    ) {
        // 内高光光带（玻璃上沿反光）
        Box(
            Modifier
                .fillMaxSize()
                .background(innerGlow)
        )
        content()
    }
}
