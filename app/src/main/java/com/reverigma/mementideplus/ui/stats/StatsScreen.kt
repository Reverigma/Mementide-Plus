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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.util.AchievementShare
import com.reverigma.mementideplus.util.DateUtils

@Composable
fun StatsScreen(viewModel: StatsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("统计") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(
                        label = "总打卡",
                        value = "${state.totalCompletions}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = "进行中",
                        value = "${state.activeHabits}",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        label = "最长连续",
                        value = "${state.bestStreak}",
                        unit = "天",
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "月度日历",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.prevMonth() }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上月")
                            }
                            Text(
                                state.monthLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            IconButton(onClick = { viewModel.nextMonth() }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下月")
                            }
                            IconButton(onClick = { viewModel.resetMonth() }) {
                                Icon(Icons.Filled.Today, "回到本月")
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        MonthCalendar(
                            cells = state.monthCells,
                            activeHabits = state.activeHabits
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "近 18 周打卡密度",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(14.dp))
                        if (state.activeHabits == 0) {
                            EmptyStats()
                        } else {
                            Heatmap(state = state)
                            Spacer(Modifier.height(12.dp))
                            HeatmapLegend()
                        }
                    }
                }
            }

            if (state.perHabit.isNotEmpty()) {
                item {
                    Text(
                        "各习惯",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(state.perHabit, key = { it.habit.id }) { hs ->
                    HabitStatCard(hs)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String = "",
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                    color = color
                )
                if (unit.isNotBlank()) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Heatmap(state: StatsUiState) {
    Column {
        // 月份标签行
        MonthLabels(state.weeks)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            state.weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { cell ->
                        val color = heatmapColor(cell.count, state.maxCount)
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthLabels(weeks: List<List<DayCell>>) {
    if (weeks.isEmpty()) return
    val labels = weeks.mapIndexed { index, week ->
        val firstDay = week.firstOrNull()?.date
        if (firstDay != null && (index == 0 || firstDay.endsWith("-01") || firstDay.endsWith("-15"))) {
            val month = firstDay.substring(5, 7).toIntOrNull() ?: 0
            val monthStr = when (month) {
                1 -> "1月"; 2 -> "2月"; 3 -> "3月"; 4 -> "4月"
                5 -> "5月"; 6 -> "6月"; 7 -> "7月"; 8 -> "8月"
                9 -> "9月"; 10 -> "10月"; 11 -> "11月"; 12 -> "12月"
                else -> ""
            }
            monthStr
        } else ""
    }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEach { label ->
            Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.TopCenter) {
                if (label.isNotBlank()) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun heatmapColor(count: Int, maxCount: Int): Color {
    return when {
        count <= 0 -> MaterialTheme.colorScheme.surfaceContainerHighest
        maxCount <= 0 -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> {
            val ratio = count.toFloat() / maxCount
            when {
                ratio < 0.25f -> Color(0xFFDCFCE7) // 浅绿
                ratio < 0.5f -> Color(0xFF86EFAC)  // 绿
                ratio < 0.75f -> Color(0xFF22C55E) // 深绿
                else -> Color(0xFF15803D)          // 最深绿
            }
        }
    }
}

@Composable
private fun HeatmapLegend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("少", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        listOf(
            Color(0xFFDCFCE7),
            Color(0xFF86EFAC),
            Color(0xFF22C55E),
            Color(0xFF15803D)
        ).forEach { c ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(c)
            )
        }
        Text("多", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyStats() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "还没有习惯",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "先去「今日」添加一个习惯，这里就会显示打卡热力图",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HabitStatCard(hs: HabitStat) {
    val h: Habit = hs.habit
    val tint = Color(h.colorInt)
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.20f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(h.emoji, fontSize = 22.sp)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    h.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatBadge("连续", "${hs.currentStreak}天", MaterialTheme.colorScheme.primary)
                    StatBadge("总完成", "${hs.totalDone}", MaterialTheme.colorScheme.secondary)
                    StatBadge("本周", "${hs.thisWeekDone}", MaterialTheme.colorScheme.tertiary)
                }
            }
            IconButton(onClick = { AchievementShare.share(context, h, hs.currentStreak, hs.totalDone, hs.thisWeekDone) }) {
                Icon(
                    Icons.Outlined.Share,
                    "分享成就",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 月度日历网格（周日起始），每天按完成习惯数着色，今日加边框 */
@Composable
private fun MonthCalendar(cells: List<MonthCell>, activeHabits: Int) {
    val weekdayLabels = listOf("日", "一", "二", "三", "四", "五", "六")
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdayLabels.forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cell.date == null) {
                            Box(modifier = Modifier.size(34.dp))
                        } else {
                            val color = cellColor(cell.count, activeHabits)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (cell.count > 0) color
                                        else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.35f)
                                    )
                                    .then(
                                        if (cell.isToday) {
                                            Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    cell.date.substring(8, 10).toIntOrNull()?.toString() ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (cell.count > 0) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "打卡密度：",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            listOf(0, 1, 2, 3).forEach { level ->
                val c = cellColor(level, activeHabits.coerceAtLeast(1))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(c)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                "颜色越深打卡越多",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 按完成数映射颜色：0 灰、1 浅、2 中、3+ 深（使用习惯主色阶） */
@Composable
private fun cellColor(count: Int, activeHabits: Int): Color {
    val base = MaterialTheme.colorScheme.primary
    return when {
        count <= 0 -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
        count == 1 -> base.copy(alpha = 0.35f)
        count == 2 -> base.copy(alpha = 0.6f)
        else -> base.copy(alpha = 0.9f)
    }
}
