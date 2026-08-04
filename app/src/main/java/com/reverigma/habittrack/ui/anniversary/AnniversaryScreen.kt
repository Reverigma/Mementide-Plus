package com.reverigma.habittrack.ui.anniversary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reverigma.habittrack.data.model.Anniversary
import com.reverigma.habittrack.util.DateUtils

@Composable
fun AnniversaryScreen(
    viewModel: AnniversaryViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var toDelete by remember { mutableStateOf<Anniversary?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("纪念日", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(10.dp))
            if (state.items.isEmpty()) {
                Text(
                    "还没有纪念日，点右下角 + 添加",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(state.items, key = { it.anniversary.id }) { item ->
            AnniversaryCard(item = item, onDelete = { toDelete = item.anniversary })
        }
    }

    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("删除纪念日") },
            text = { Text("确定删除「${toDelete!!.name}」？") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(toDelete!!); toDelete = null }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun AnniversaryCard(item: AnniversaryItem, onDelete: () -> Unit) {
    val a = item.anniversary
    val label = when {
        item.countdownDays > 0 -> "还有 ${item.countdownDays} 天"
        item.countdownDays == 0L -> "就是今天 🎉"
        else -> "已过 ${-item.countdownDays} 天"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(a.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(a.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${DateUtils.formatDate(a.date)} · ${repeatLabel(a.repeatType)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (a.note.isNotBlank()) {
                    Text(a.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            AssistChip(onClick = {}, label = { Text(label) })
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "删除") }
        }
    }
}
