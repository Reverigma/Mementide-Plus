package com.reverigma.mementideplus.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.ui.components.HabitIcon
import com.reverigma.mementideplus.ui.components.PosterDialog
import com.reverigma.mementideplus.ui.home.dialogs.BackfillDateDialog
import com.reverigma.mementideplus.util.AchievementCardGenerator
import com.reverigma.mementideplus.util.AchievementShare
import com.reverigma.mementideplus.util.DateUtils
import com.reverigma.mementideplus.util.rememberMaterialIconBitmap

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val advancedMode by viewModel.advancedMode.collectAsState()
    val posterTap by viewModel.posterTap.collectAsState()
    var habitToBackfill by remember { mutableStateOf<Habit?>(null) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var habitPoster by remember { mutableStateOf<HabitItem?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("今日") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    state.dateLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                if (state.total > 0) {
                    TodayProgress(state.doneCount, state.total)
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (state.items.isEmpty()) {
                item { EmptyHabits(onAdd = onAddHabit) }
            } else {
                items(state.items, key = { it.habit.id }) { item ->
                    HabitCard(
                        item = item,
                        showAdvanced = advancedMode,
                        posterEnabled = posterTap,
                        onPoster = { habitPoster = item },
                        onToggle = { viewModel.toggleToday(item.habit) },
                        onBackfill = { habitToBackfill = item.habit },
                        onEdit = { habitToEdit = item.habit },
                        onDelete = { habitToDelete = item.habit },
                        onMoveTop = { viewModel.moveHabitToTop(item.habit) },
                        onMoveUp = { viewModel.moveHabitUp(item.habit) },
                        onMoveDown = { viewModel.moveHabitDown(item.habit) }
                    )
                }
            }
        }
    }

    if (habitToEdit != null) {
        AddHabitDialog(
            initial = habitToEdit,
            onDismiss = { habitToEdit = null },
            onConfirm = { name, iconName, color, target ->
                viewModel.updateHabit(habitToEdit!!, name, iconName, color, target)
                habitToEdit = null
            }
        )
    }

    if (habitToBackfill != null) {
        BackfillDateDialog(
            habit = habitToBackfill!!,
            onDismiss = { habitToBackfill = null },
            onConfirm = { date, done, timestamp ->
                viewModel.setDoneForDate(habitToBackfill!!.id, date, done, timestamp)
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
                TextButton(
                    onClick = { viewModel.deleteHabit(habitToDelete!!); habitToDelete = null }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text("取消") } }
        )
    }

    // 打卡成就海报预览
    if (habitPoster != null) {
        val posterItem = habitPoster!!
        val iconBmp = rememberMaterialIconBitmap(
            iconName = posterItem.habit.iconName.ifBlank { "star" },
            sizeDp = 280,
            tint = Color.White
        )
        val bmp = remember(posterItem.habit.id, posterItem.doneToday, posterItem.streak) {
            AchievementCardGenerator.generate(
                posterItem.habit,
                posterItem.streak,
                posterItem.totalDone,
                posterItem.thisWeekDone,
                iconBmp
            )
        }
        val ctx = LocalContext.current
        PosterDialog(
            bitmap = bmp,
            onShare = {
                AchievementShare.shareBitmap(
                    ctx, bmp,
                    "我在 Mementide Plus 连续打卡 ${posterItem.streak} 天（${posterItem.habit.name}）💪"
                )
                habitPoster = null
            },
            onDismiss = { habitPoster = null }
        )
    }
}

@Composable
private fun TodayProgress(done: Int, total: Int) {
    val fraction = if (total > 0) done.toFloat() / total else 0f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(52.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeWidth = 5.dp,
                strokeCap = StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            CircularProgressIndicator(
                progress = { fraction },
                modifier = Modifier.size(52.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent
            )
            Text(
                "$done/$total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                if (done == total) "全部完成 🎉" else "已完成 $done / $total",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                when {
                    done == 0 -> "今天还没有打卡哦"
                    done < total -> "继续加油，坚持就是胜利"
                    else -> "太棒了，今天全部达标"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyHabits(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.TaskAlt,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "还没有习惯",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "添加第一个习惯，开启每天的打卡之旅",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        FilledTonalButton(onClick = onAdd) {
            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("添加习惯")
        }
    }
}

@Composable
private fun HabitCard(
    item: HabitItem,
    showAdvanced: Boolean,
    posterEnabled: Boolean,
    onPoster: () -> Unit,
    onToggle: () -> Unit,
    onBackfill: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveTop: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val h = item.habit
    val tint = Color(h.colorInt)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = tint.copy(alpha = if (item.doneToday) 0.10f else 0.06f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.20f),
                modifier = Modifier
                    .size(44.dp)
                    .then(
                        if (posterEnabled) {
                            Modifier.clickable(onClick = onPoster)
                        } else Modifier
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    HabitIcon(
                        iconName = h.iconName,
                        tint = tint,
                        iconSize = 22
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    h.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.streak > 0) {
                        Text(
                            "连续 ${item.streak} 天",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Text(
                        "每周 ${h.targetPerWeek} 次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            Checkbox(
                checked = item.doneToday,
                onCheckedChange = { onToggle() }
            )
            if (showAdvanced) {
                IconButton(onClick = onBackfill, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Filled.EditCalendar,
                        "补录",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.Edit,
                        "编辑",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        "删除",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                // 排序菜单
                Box {
                    var menuOpen by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Filled.MoreVert,
                            "更多操作",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("置顶") },
                            onClick = { menuOpen = false; onMoveTop() },
                            leadingIcon = { Icon(Icons.Filled.VerticalAlignTop, null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("上移") },
                            onClick = { menuOpen = false; onMoveUp() },
                            leadingIcon = { Icon(Icons.Filled.KeyboardArrowUp, null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text("下移") },
                            onClick = { menuOpen = false; onMoveDown() },
                            leadingIcon = { Icon(Icons.Filled.KeyboardArrowDown, null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
        }
    }
}
