package com.reverigma.mementideplus.ui.home.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.util.DateUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackfillDateDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: (date: String, done: Boolean, timestamp: Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(DateUtils.todayStr()) }
    var done by remember { mutableStateOf(true) }
    // 默认时间为"当前时间"
    var hour by remember { mutableStateOf(java.time.LocalTime.now().hour) }
    var minute by remember { mutableStateOf(java.time.LocalTime.now().minute) }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择打卡时间") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    hour = timeState.hour
                    minute = timeState.minute
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } }
        )
    }

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
                        Text("打卡时间：${"%02d".format(hour)}:${"%02d".format(minute)}")
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { showTimePicker = true }) { Text("修改") }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("标记为已完成")
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = done, onCheckedChange = { done = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val timestamp = LocalDateTime.of(
                        LocalDate.parse(selectedDate),
                        LocalTime.of(hour, minute)
                    ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onConfirm(selectedDate, done, timestamp)
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
        )
    }
}
