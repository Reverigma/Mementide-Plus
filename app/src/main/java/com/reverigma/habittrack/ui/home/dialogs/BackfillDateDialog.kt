package com.reverigma.habittrack.ui.home.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.reverigma.habittrack.data.model.Habit
import com.reverigma.habittrack.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackfillDateDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: (date: String, done: Boolean) -> Unit
) {
    var showPicker by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(DateUtils.todayStr()) }
    var done by remember { mutableStateOf(true) }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = DateUtils.epochMillisForDate(selectedDate))
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = DateUtils.fromEpochMillis(state.selectedDateMillis ?: return@TextButton)
                    showPicker = false
                }) { Text("下一步") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
        ) {
            DatePicker(state = state)
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("补录「${habit.name}」") },
            text = {
                Column {
                    Text("日期：${DateUtils.formatDate(selectedDate)}")
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("标记为已完成")
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = done, onCheckedChange = { done = it })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onConfirm(selectedDate, done) }) { Text("保存") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
        )
    }
}
