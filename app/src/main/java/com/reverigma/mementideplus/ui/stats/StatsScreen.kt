package com.reverigma.mementideplus.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.reverigma.mementideplus.data.model.Habit

@Composable
fun StatsScreen(viewModel: StatsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = { Text("统计") })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryCard("总打卡", "${state.totalCompletions}", Modifier.weight(1f))
                    SummaryCard("进行中", "${state.activeHabits}", Modifier.weight(1f))
                    SummaryCard("最长连续", "${state.bestStreak} 天", Modifier.weight(1f))
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("近 18 周打卡密度", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        if (state.activeHabits == 0) {
                            Text("还没有习惯，先去「今日」添加一个吧。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Heatmap(state = state)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("少", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Legend()
                            Text("多", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            if (state.perHabit.isNotEmpty()) {
                item { Text("各习惯", style = MaterialTheme.typography.titleMedium) }
                items(state.perHabit, key = { it.habit.id }) { hs -> HabitStatCard(hs) }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Heatmap(state: StatsUiState) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        state.weeks.forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                week.forEach { cell ->
                    val color = if (cell.count <= 0) scheme.surfaceVariant
                    else scheme.primary.copy(alpha = 0.25f + 0.75f * (cell.count.toFloat() / state.maxCount))
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun Legend() {
    val scheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(0.25f, 0.5f, 0.75f, 1f).forEach { a ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(scheme.primary.copy(alpha = a))
            )
        }
    }
}

@Composable
private fun HabitStatCard(hs: HabitStat) {
    val h: Habit = hs.habit
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(h.colorInt).copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { Text(h.emoji, fontSize = 20.sp) }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(h.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "🔥 连续 ${hs.currentStreak} 天 · 总完成 ${hs.totalDone} · 本周 ${hs.thisWeekDone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
