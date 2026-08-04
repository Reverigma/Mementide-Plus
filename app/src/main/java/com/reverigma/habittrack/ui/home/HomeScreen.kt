package com.reverigma.habittrack.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reverigma.habittrack.data.model.Habit
import com.reverigma.habittrack.ui.home.dialogs.BackfillDateDialog
import com.reverigma.habittrack.util.DateUtils

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var habitToBackfill by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("今日", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(2.dp))
            Text(state.dateLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            if (state.total > 0) {
                LinearProgressIndicator(
                    progress = state.doneCount.toFloat() / state.total,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "已完成 ${state.doneCount} / ${state.total}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
            } else {
                Text(
                    "还没有习惯，点右下角 + 添加一个吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
            }
        }
        items(state.items, key = { it.habit.id }) { item ->
            HabitCard(
                item = item,
                onToggle = { viewModel.toggleToday(item.habit) },
                onBackfill = { habitToBackfill = item.habit },
                onDelete = { habitToDelete = item.habit }
            )
        }
    }

    if (habitToBackfill != null) {
        BackfillDateDialog(
            habit = habitToBackfill!!,
            onDismiss = { habitToBackfill = null },
            onConfirm = { date, done ->
                viewModel.setDoneForDate(habitToBackfill!!.id, date, done)
                habitToBackfill = null
            }
        )
    }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("删除习惯") },
            text = { Text("确定删除「${habitToDelete!!.name}」？相关打卡记录也会一并删除。") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteHabit(habitToDelete!!); habitToDelete = null }) {
                    Text("删除")
                }
            },
            dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun HabitCard(
    item: HabitItem,
    onToggle: () -> Unit,
    onBackfill: () -> Unit,
    onDelete: () -> Unit
) {
    val h = item.habit
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(h.colorInt).copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { Text(h.emoji, fontSize = 20.sp) }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(h.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.streak > 0) {
                        AssistChip(
                            onClick = {},
                            label = { Text("🔥 连续 ${item.streak} 天") }
                        )
                    }
                    Text(
                        "每周 ${h.targetPerWeek} 次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Checkbox(checked = item.doneToday, onCheckedChange = { onToggle() })
            IconButton(onClick = onBackfill) { Icon(Icons.Filled.EditCalendar, "补录") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "删除") }
        }
    }
}
