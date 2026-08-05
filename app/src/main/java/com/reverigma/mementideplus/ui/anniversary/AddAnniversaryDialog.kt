package com.reverigma.mementideplus.ui.anniversary

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reverigma.mementideplus.data.model.Anniversary
import com.reverigma.mementideplus.data.model.REPEAT_MONTHLY
import com.reverigma.mementideplus.data.model.REPEAT_NONE
import com.reverigma.mementideplus.data.model.REPEAT_YEARLY
import com.reverigma.mementideplus.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnniversaryDialog(
    initial: Anniversary? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String, colorInt: Int, date: String, repeat: String, note: String) -> Unit
) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var emoji by remember(initial?.id) { mutableStateOf(initial?.emoji ?: "🎉") }
    var color by remember(initial?.id) { mutableStateOf(initial?.colorInt ?: 0xFFE11D48.toInt()) }
    var date by remember(initial?.id) { mutableStateOf(initial?.date ?: DateUtils.todayStr()) }
    var repeat by remember(initial?.id) { mutableStateOf(initial?.repeatType ?: REPEAT_YEARLY) }
    var note by remember(initial?.id) { mutableStateOf(initial?.note ?: "") }
    var showPicker by remember { mutableStateOf(false) }

    val emojis = listOf("🎉", "🎂", "💍", "❤️", "🌟", "🎁", "🥂", "🌹", "🎈", "💖", "📅", "🕯️")
    val colors = listOf(
        0xFFE11D48, 0xFF4F46E5, 0xFF0EA5E9, 0xFF10B981,
        0xFFF59E0B, 0xFFEF4444, 0xFF8B5CF6, 0xFFEC4899
    ).map { it.toInt() }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = DateUtils.epochMillisForDate(date))
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = DateUtils.fromEpochMillis(state.selectedDateMillis ?: return@TextButton)
                    showPicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("取消") } }
        ) {
            DatePicker(state = state)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "新建纪念日" else "编辑纪念日") },
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(emojis) { e ->
                        FilterChip(
                            selected = emoji == e,
                            onClick = { emoji = e },
                            label = { Text(e) }
                        )
                    }
                }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("日期：${DateUtils.formatDate(date)}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { showPicker = true }) { Text("选择日期") }
                }
                Text("重复", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(REPEAT_YEARLY to "每年", REPEAT_MONTHLY to "每月", REPEAT_NONE to "不重复").forEach { (v, l) ->
                        FilterChip(selected = repeat == v, onClick = { repeat = v }, label = { Text(l) })
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim(), emoji, color, date, repeat, note.trim()) }
            ) { Text(if (initial == null) "创建" else "保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
