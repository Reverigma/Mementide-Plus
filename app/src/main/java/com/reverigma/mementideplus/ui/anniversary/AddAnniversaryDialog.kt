package com.reverigma.mementideplus.ui.anniversary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.reverigma.mementideplus.data.model.REPEAT_MONTHLY
import com.reverigma.mementideplus.data.model.REPEAT_NONE
import com.reverigma.mementideplus.data.model.REPEAT_YEARLY
import com.reverigma.mementideplus.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnniversaryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String, date: String, repeat: String, note: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎉") }
    var date by remember { mutableStateOf(DateUtils.todayStr()) }
    var repeat by remember { mutableStateOf(REPEAT_YEARLY) }
    var note by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }

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
        title = { Text("新建纪念日") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                onClick = { onConfirm(name.trim(), emoji, date, repeat, note.trim()) }
            ) { Text("创建") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
