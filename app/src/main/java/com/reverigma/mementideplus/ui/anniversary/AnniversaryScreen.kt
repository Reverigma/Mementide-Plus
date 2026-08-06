package com.reverigma.mementideplus.ui.anniversary

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reverigma.mementideplus.data.model.Anniversary
import com.reverigma.mementideplus.ui.components.HabitIcon
import com.reverigma.mementideplus.ui.components.PosterDialog
import com.reverigma.mementideplus.util.AchievementCardGenerator
import com.reverigma.mementideplus.util.AchievementShare
import com.reverigma.mementideplus.util.DateUtils
import com.reverigma.mementideplus.util.ImageStore
import com.reverigma.mementideplus.util.LunarCalendar
import com.reverigma.mementideplus.util.rememberMaterialIconBitmap

@Composable
fun AnniversaryScreen(
    viewModel: AnniversaryViewModel,
    modifier: Modifier = Modifier,
    onAddAnniversary: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val advancedMode by viewModel.advancedMode.collectAsState()
    val posterTap by viewModel.posterTap.collectAsState()
    var toDelete by remember { mutableStateOf<Anniversary?>(null) }
    var toEdit by remember { mutableStateOf<Anniversary?>(null) }
    var posterItem by remember { mutableStateOf<AnniversaryItem?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("纪念日") },
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
            if (state.items.isEmpty()) {
                item { EmptyAnniversaries(onAdd = onAddAnniversary) }
            } else {
                items(state.items, key = { it.anniversary.id }) { item ->
                    AnniversaryCard(
                        item = item,
                        showAdvanced = advancedMode,
                        posterEnabled = posterTap,
                        onPoster = { posterItem = item },
                        onEdit = { toEdit = item.anniversary },
                        onDelete = { toDelete = item.anniversary }
                    )
                }
            }
        }
    }

    if (toEdit != null) {
        AddAnniversaryDialog(
            initial = toEdit,
            onDismiss = { toEdit = null },
            onConfirm = { n, ic, c, d, r, nt, ct, img ->
                viewModel.update(toEdit!!, n, ic, c, d, r, nt, ct, img)
                toEdit = null
            }
        )
    }

    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("删除纪念日") },
            text = { Text("确定删除「${toDelete!!.name}」？") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.delete(toDelete!!); toDelete = null }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("取消") } }
        )
    }

    // 纪念日海报预览
    if (posterItem != null) {
        val p = posterItem!!
        val iconBmp = rememberMaterialIconBitmap(
            iconName = p.anniversary.iconName.ifBlank { "cake" },
            sizeDp = 280,
            tint = Color(p.anniversary.colorInt)
        )
        val bmp = remember(p.anniversary.id, p.countdownDays) {
            val bg = p.anniversary.imagePath.ifBlank { null }?.let { ImageStore.decodeScaled(it) }
            AchievementCardGenerator.generateAnniversary(
                p.anniversary,
                p.countdownDays,
                AchievementCardGenerator.anniversaryDateLabel(p.anniversary),
                iconBmp,
                bg
            )
        }
        val ctx = LocalContext.current
        PosterDialog(
            bitmap = bmp,
            onShare = {
                AchievementShare.shareAnniversary(ctx, p.anniversary, p.countdownDays)
                posterItem = null
            },
            onDismiss = { posterItem = null }
        )
    }
}

@Composable
private fun EmptyAnniversaries(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Cake,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "还没有纪念日",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "记录生日、纪念日等重要日子，获得倒数提醒",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        FilledTonalButton(onClick = onAdd) {
            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("添加纪念日")
        }
    }
}

@Composable
private fun AnniversaryCard(
    item: AnniversaryItem,
    showAdvanced: Boolean,
    posterEnabled: Boolean,
    onPoster: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val a = item.anniversary
    val tint = Color(a.colorInt)
    val label = when {
        item.countdownDays > 0 -> "还有 ${item.countdownDays} 天"
        item.countdownDays == 0L -> "就是今天"
        else -> "已过 ${-item.countdownDays} 天"
    }
    val labelColor = when {
        item.countdownDays == 0L -> MaterialTheme.colorScheme.tertiary
        item.countdownDays in 1..7 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
                        iconName = a.iconName,
                        tint = tint,
                        iconSize = 22
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    a.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${anniversaryDateLabel(a)} · ${repeatLabel(a.repeatType)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (a.note.isNotBlank()) {
                    Text(
                        a.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = labelColor.copy(alpha = 0.1f),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = labelColor
                )
            }
            if (showAdvanced) {
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
            }
        }
    }
}

/** 纪念日日期显示：农历显示"八月十五"，公历显示"2026年8月5日" */
private fun anniversaryDateLabel(a: com.reverigma.mementideplus.data.model.Anniversary): String {
    return if (a.calendarType == com.reverigma.mementideplus.data.model.CALENDAR_LUNAR) {
        val parts = a.date.split("-")
        val m = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val d = parts.getOrNull(1)?.toIntOrNull() ?: 1
        "农历${LunarCalendar.lunarLabel(m, d)}"
    } else {
        DateUtils.formatDate(a.date)
    }
}

