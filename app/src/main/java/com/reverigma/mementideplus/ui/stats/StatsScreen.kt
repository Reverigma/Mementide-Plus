package com.reverigma.mementideplus.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.reverigma.mementideplus.ui.components.glassBackgroundBrush
import com.reverigma.mementideplus.util.DateUtils
@Composable
fun StatsScreen(viewModel: StatsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    val showMonth by viewModel.statsMonthCalendar.collectAsState()
    val showHeat by viewModel.statsHeatmap.collectAsState()
    val viewOrder by viewModel.statsViewOrder.collectAsState()
    // 点击月历日期查看当天打卡详情
    var detailDay by remember { mutableStateOf<String?>(null) }
    val detailNames = detailDay?.let { state.dayDetails[it].orEmpty() } ?: emptyList()
    // 按设置顺序排列视图
    val views = buildList {
        if (showMonth) add("month")
        if (showHeat) add("heatmap")
    }.sortedWith(compareBy { viewOrder.split(",").indexOf(it).let { i -> if (i < 0) Int.MAX_VALUE else i } })
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glassBackgroundBrush())
    ) {
        TopAppBar(
            title = { Text("统计") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
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

            // 统计视图按设置顺序渲染（月度日历 / 热力图）
            views.forEach { v ->
                when (v) {
                    "month" -> item {
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
                                    activeHabits = state.activeHabits,
                                    onDayClick = { detailDay = it }
                                )
                            }
                        }
                    }
                    "heatmap" -> item {
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
                }
            }
        }
    }

    // 当天打卡详情对话框
    if (detailDay != null) {
        AlertDialog(
            onDismissRequest = { detailDay = null },
            title = { Text(DateUtils.formatDate(detailDay ?: "")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (detailNames.isEmpty()) {
                        Text(
                            "这一天没有打卡记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        detailNames.forEach { name ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "共 ${detailNames.size} 个习惯完成打卡",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { detailDay = null }) { Text("关闭") } }
        )
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
        // 月份标签行（与格子同宽，左对齐）
        MonthLabels(state.weeks)
        Spacer(Modifier.height(6.dp))
        // 18 列按权重铺满卡片宽度、居中，格子随屏宽等比缩放
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            state.weeks.forEach { week ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    week.forEach { cell ->
                        val color = heatmapColor(cell.count, state.maxCount)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        labels.forEach { label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp),
                contentAlignment = Alignment.TopCenter
            ) {
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
            // 与主题统一：主色靛蓝的透明度阶（浅→深）
            val base = MaterialTheme.colorScheme.primary
            val ratio = count.toFloat() / maxCount
            when {
                ratio < 0.25f -> base.copy(alpha = 0.2f)
                ratio < 0.5f -> base.copy(alpha = 0.4f)
                ratio < 0.75f -> base.copy(alpha = 0.65f)
                else -> base.copy(alpha = 0.9f)
            }
        }
    }
}

@Composable
private fun HeatmapLegend() {
    val base = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("少", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        listOf(
            base.copy(alpha = 0.2f),
            base.copy(alpha = 0.4f),
            base.copy(alpha = 0.65f),
            base.copy(alpha = 0.9f)
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
private fun MonthCalendar(cells: List<MonthCell>, activeHabits: Int, onDayClick: (String) -> Unit) {
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
                                    .clickable { onDayClick(cell.date) }
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
