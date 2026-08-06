package com.reverigma.mementideplus.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reverigma.mementideplus.util.IconCatalog

/**
 * 图标选择器：Material 线性图标（风格统一，与应用整体设计一致）。
 * @param selectedIconName 当前选中的图标名
 * @param onIconSelected 选择图标时回调
 */
@Composable
fun IconPicker(
    selectedIconName: String,
    onIconSelected: (String) -> Unit
) {
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
                Box(contentAlignment = Alignment.Center) {
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
}
