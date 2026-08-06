package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reverigma.mementideplus.util.IconCatalog

/**
 * 统一图标渲染：Material 线性图标（iconName 为空时兜底默认星形图标）。
 * 用于习惯/纪念日卡片、统计页等所有展示图标的位置。
 */
@Composable
fun HabitIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    iconSize: Int = 22
) {
    val vector = IconCatalog.vector(iconName) ?: IconCatalog.vector("star")!!
    Icon(
        imageVector = vector,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(iconSize.dp)
    )
}
