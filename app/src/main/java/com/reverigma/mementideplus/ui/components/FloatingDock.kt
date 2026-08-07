package com.reverigma.mementideplus.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 悬浮胶囊 Dock（iOS 26 液态玻璃风格底部导航）：
 * - 整条悬浮大圆角胶囊（白毛玻璃 + 上沿高光描边 + 柔光阴影）
 * - 选中项为独立"选中胶囊"，点击时弹性滑动到对应位置
 * - 每个 tab：图标 + 小字标签，选中主色
 */
@Composable
fun FloatingDock(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val tabs = listOf(
        Triple(Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "今日"),
        Triple(Icons.Filled.Cake, Icons.Outlined.Cake, "纪念日"),
        Triple(Icons.Filled.BarChart, Icons.Outlined.BarChart, "统计"),
        Triple(Icons.Filled.Settings, Icons.Outlined.Settings, "设置")
    )

    // 玻璃材质
    val glass = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x2EFFFFFF), Color(0x1AFFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xF0FFFFFF), Color(0xDEFFFFFF)))
    }
    val borderBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0x80FFFFFF), Color(0x26FFFFFF)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0x80FFFFFF)))
    }
    val shadowColor = if (isDark) Color(0x66000000) else Color(0x2464748B)
    // 选中胶囊底色（比 Dock 更实，浮起）
    val pillColor = if (isDark) Color(0x33FFFFFF) else Color(0xE6FFFFFF)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val cellWidth = maxWidth / tabs.size
        // 选中胶囊滑动位置
        val pillOffset by animateDpAsState(
            targetValue = cellWidth * selected + 6.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "dockPill"
        )
        val pillWidth = cellWidth - 12.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = shadowColor, ambientColor = shadowColor)
                .clip(RoundedCornerShape(32.dp))
                .background(glass)
                .border(1.dp, borderBrush, RoundedCornerShape(32.dp))
        ) {
            // 选中胶囊（滑动层）
            Box(
                Modifier
                    .offset(x = pillOffset)
                    .width(pillWidth)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(pillColor)
            )

            // tab 内容层
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { i, (filled, outlined, label) ->
                    val isSel = selected == i
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSel) 1f else 0.92f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "dockIcon$i"
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onSelect(i) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSel) filled else outlined,
                            contentDescription = label,
                            tint = if (isSel) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(if (isSel) 24.dp else 22.dp)
                                .scale(iconScale)
                        )
                        Text(
                            label,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            color = if (isSel) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
