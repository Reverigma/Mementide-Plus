package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reverigma.mementideplus.util.IconCatalog

/**
 * 统一的图标渲染：iconName 非空显示 Material Icon，否则显示 emoji。
 * 用于习惯/纪念日卡片、统计页等所有展示 emoji 的位置。
 */
@Composable
fun HabitIcon(
    iconName: String,
    emoji: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    fontSize: Int = 22,
    iconSize: Int = 22
) {
    val vector = iconName.ifBlank { null }?.let { IconCatalog.vector(it) }
    if (vector != null) {
        Icon(
            imageVector = vector,
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(iconSize.dp)
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = fontSize.sp)
        }
    }
}
