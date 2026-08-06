package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reverigma.mementideplus.util.IconCatalog

/**
 * 图标选择器：两个页签——「图标」(Material Icons，风格统一) / 「emoji」(个性化)。
 * @param selectedIconName 当前选中的图标名（空 = 未选图标）
 * @param selectedEmoji 当前选中的 emoji
 * @param onIconSelected 选择图标时回调（应清空 emoji）
 * @param onEmojiSelected 选择 emoji 时回调（应清空 iconName）
 */
@Composable
fun IconPicker(
    selectedIconName: String,
    selectedEmoji: String,
    onIconSelected: (String) -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf("✅", "💧", "🏃", "📚", "🧘", "🥗", "😴", "🎯", "💪", "🎨", "🌱", "🎵", "🎉", "🎂", "❤️")

    // 图标页签（默认展示，选中任一图标后保持）
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(IconCatalog.DEFAULT_NAMES) { name ->
            val vector = IconCatalog.vector(name) ?: return@items
            val isSelected = selectedIconName == name
            Surface(
                onClick = { onIconSelected(name) },
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(40.dp)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = vector,
                        contentDescription = name,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.size(4.dp))
    // emoji 页签
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(emojis) { e ->
            FilterChip(
                selected = selectedEmoji == e,
                onClick = { onEmojiSelected(e) },
                label = { Text(e) }
            )
        }
    }
}
