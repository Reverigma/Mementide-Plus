package com.reverigma.mementideplus.ui.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.ui.components.IconPicker
import com.reverigma.mementideplus.util.IconCatalog

@Composable
fun AddHabitDialog(
    initial: Habit? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String, color: Int, target: Int) -> Unit
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var iconName by remember(initial?.id) { mutableStateOf(initial?.iconName?.takeIf { it.isNotBlank() } ?: "star") }
    var color by remember(initial?.id) { mutableStateOf(initial?.colorInt ?: 0xFF4F46E5.toInt()) }
    var target by remember(initial?.id) { mutableStateOf(initial?.targetPerWeek ?: 7) }

    val colors = listOf(
        0xFF4F46E5, 0xFF0EA5E9, 0xFF10B981, 0xFFF59E0B,
        0xFFEF4444, 0xFF8B5CF6, 0xFFEC4899, 0xFF14B8A6
    ).map { it.toInt() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新建习惯" else "编辑习惯") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("图标", style = MaterialTheme.typography.labelMedium)
                IconPicker(
                    selectedIconName = iconName,
                    onIconSelected = { iconName = it }
                )
                Text("颜色", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(colors) { c ->
                        val isSelected = color == c
                        Surface(
                            onClick = { color = c },
                            shape = CircleShape,
                            color = Color(c),
                            modifier = Modifier
                                .size(36.dp)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Text("每周目标：$target 次", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = target.toFloat(),
                    onValueChange = { target = it.toInt() },
                    valueRange = 1f..7f,
                    steps = 6
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(name.trim(), iconName, color, target)
                }
            ) { Text(if (initial == null) "创建" else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
