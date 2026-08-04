package com.reverigma.habittrack.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String, color: Int, target: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("✅") }
    var color by remember { mutableStateOf(0xFF4F46E5.toInt()) }
    var target by remember { mutableStateOf(7) }

    val emojis = listOf("✅", "💧", "🏃", "📚", "🧘", "🥗", "😴", "🎯", "💪", "🎨", "🌱", "🎵")
    val colors = listOf(0xFF4F46E5, 0xFF0EA5E9, 0xFF10B981, 0xFFF59E0B, 0xFFEF4444, 0xFF8B5CF6, 0xFFEC4899, 0xFF14B8A6)
        .map { it.toInt() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建习惯") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("图标", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(emojis) { e ->
                        FilterChip(selected = emoji == e, onClick = { emoji = e }, label = { Text(e) })
                    }
                }
                Text("颜色", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(colors) { c ->
                        Surface(
                            selected = color == c,
                            onClick = { color = c },
                            shape = CircleShape,
                            color = Color(c),
                            modifier = Modifier.size(32.dp)
                        ) {}
                    }
                }
                Text("每周目标：$target 次", style = MaterialTheme.typography.labelMedium)
                Slider(value = target.toFloat(), onValueChange = { target = it.toInt() }, valueRange = 1f..7f, steps = 6)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), emoji, color, target) }
            ) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
