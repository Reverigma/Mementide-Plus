package com.reverigma.mementideplus.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SettingsSystemDaydream
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.reverigma.mementideplus.BuildConfig
import com.reverigma.mementideplus.reminder.ReminderScheduler
import com.reverigma.mementideplus.ui.components.glassBackgroundBrush
import com.reverigma.mementideplus.util.DateUtils
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val hasPin by viewModel.hasPin.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()
    val advancedMode by viewModel.advancedMode.collectAsState()
    val statsMonthCalendar by viewModel.statsMonthCalendar.collectAsState()
    val statsHeatmap by viewModel.statsHeatmap.collectAsState()
    val statsViewOrder by viewModel.statsViewOrder.collectAsState()
    val posterTap by viewModel.posterTap.collectAsState()
    val updateSource by viewModel.updateSource.collectAsState()
    var showSetPin by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun scheduleReminder() {
        val parts = viewModel.reminderTime.value.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 21
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        ReminderScheduler.schedule(context, hour, minute)
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
            scheduleReminder()
            status = "已开启每日提醒"
        } else {
            status = "需要通知权限才能开启提醒"
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            try {
                val json = viewModel.exportData()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                status = "已导出备份"
            } catch (e: Exception) {
                status = "导出失败：${e.message}"
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
                val n = viewModel.importData(json)
                status = "已导入 $n 条记录"
            } catch (e: Exception) {
                status = "导入失败：${e.message}"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glassBackgroundBrush())
    ) {
        TopAppBar(
            title = { Text("设置") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 外观
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("外观", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            Triple("跟随系统", 0, Icons.Outlined.SettingsSystemDaydream),
                            Triple("浅色", 1, Icons.Outlined.LightMode),
                            Triple("深色", 2, Icons.Outlined.DarkMode)
                        ).forEach { (label, mode, icon) ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(label) },
                                leadingIcon = {
                                    Icon(icon, null, modifier = Modifier.size(18.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 应用锁
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("应用锁", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "开启后，打开应用需输入 PIN。默认关闭，纯本地、不上传。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = appLockEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (hasPin) viewModel.setAppLockEnabled(true)
                                    else showSetPin = true
                                } else {
                                    viewModel.setAppLockEnabled(false)
                                }
                            }
                        )
                    }
                    if (appLockEnabled) {
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TextButton(onClick = { showSetPin = true }) { Text("更改 PIN") }
                            TextButton(onClick = { viewModel.removePin() }) { Text("关闭并移除 PIN") }
                        }
                    }
                }
            }

            // 每日提醒
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("每日提醒", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "每天定时提醒未完成的习惯，纪念日当天也会提醒。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        viewModel.setReminderEnabled(true)
                                        scheduleReminder()
                                    } else {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    viewModel.setReminderEnabled(false)
                                    ReminderScheduler.cancel(context)
                                }
                            }
                        )
                    }
                    if (reminderEnabled) {
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Notifications,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "提醒时间：${reminderTime}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = { showTimePicker = true }) { Text("修改") }
                        }
                    }
                }
            }

            // 高级模式
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("高级模式", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "开启后，习惯和纪念日卡片上显示补录、编辑、删除等进阶操作。默认关闭，保持界面简洁。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = advancedMode,
                            onCheckedChange = { viewModel.setAdvancedMode(it) }
                        )
                    }
                }
            }

            // 统计页视图
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("统计页视图", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "选择统计页展示哪些视图，并调整显示顺序（默认只显示月度日历）。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    // 顺序控制
                    val orderList = statsViewOrder.split(",")
                    val orderLabel = orderList.joinToString(" → ") {
                        when (it) {
                            "month" -> "月度日历"
                            "heatmap" -> "热力图"
                            else -> it
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "当前顺序：$orderLabel",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "统计页按此顺序从上到下显示",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            enabled = orderList.size > 1,
                            onClick = {
                                viewModel.setStatsViewOrder(orderList.reversed().joinToString(","))
                            }
                        ) { Text("交换顺序") }
                    }
                    Spacer(Modifier.height(6.dp))

                    // 月度日历
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("月度日历", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "按天显示打卡密度，可翻月、标记今天",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = statsMonthCalendar,
                            onCheckedChange = {
                                viewModel.setStatsMonthCalendar(it)
                                if (it && !statsHeatmap && statsViewOrder.contains("heatmap")) {
                                    viewModel.setStatsViewOrder("month,heatmap")
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(6.dp))

                    // 近 18 周热力图
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("近 18 周打卡热力图", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "一屏看 18 周连续趋势（绿阶）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = statsHeatmap,
                            onCheckedChange = {
                                viewModel.setStatsHeatmap(it)
                                if (it && !statsMonthCalendar && statsViewOrder.contains("month")) {
                                    viewModel.setStatsViewOrder("heatmap,month")
                                }
                            }
                        )
                    }
                }
            }

            // 海报预览
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("海报预览", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "点击习惯或纪念日卡片上的图标，弹出对应的海报，可右上角分享。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = posterTap,
                            onCheckedChange = { viewModel.setPosterTap(it) }
                        )
                    }
                }
            }

            // 检查更新
            val updateState by viewModel.updateState.collectAsState()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("检查更新", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "手动点击检查最新版本，发现新版可应用内直接下载安装，不强制更新。不会自动检查。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            enabled = updateState !is UpdateCheckState.Checking,
                            onClick = {
                                viewModel.checkForUpdates(BuildConfig.VERSION_NAME)
                            }
                        ) {
                            Text(
                                if (updateState is UpdateCheckState.Checking) "检查中…" else "检查"
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    // 更新源切换
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "更新源",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = updateSource == "gitee",
                            onClick = { viewModel.setUpdateSource("gitee") },
                            label = { Text("Gitee") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = updateSource == "github",
                            onClick = { viewModel.setUpdateSource("github") },
                            label = { Text("GitHub") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    if (updateState is UpdateCheckState.Error) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            (updateState as UpdateCheckState.Error).message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 数据备份
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("数据备份", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "导出为 JSON 文件到本机任意位置（系统文件选择器），或导入恢复。数据完全在本地，不上传。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("mementide-plus-backup-${DateUtils.todayStr()}.json") }
                        ) {
                            Icon(Icons.Outlined.Upload, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("导出备份")
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) }
                        ) {
                            Icon(Icons.Outlined.Download, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("导入备份")
                        }
                    }
                }
            }

            status?.let {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        val initial = viewModel.reminderTime.value.split(":")
        val timeState = rememberTimePickerState(
            initialHour = initial.getOrNull(0)?.toIntOrNull() ?: 21,
            initialMinute = initial.getOrNull(1)?.toIntOrNull() ?: 0,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hh = "%02d".format(timeState.hour)
                    val mm = "%02d".format(timeState.minute)
                    viewModel.setReminderTime("$hh:$mm")
                    scheduleReminder()
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showSetPin) {
        SetPinDialog(
            title = if (hasPin) "更改 PIN" else "设置 PIN",
            onDismiss = { showSetPin = false },
            onConfirm = { pin ->
                viewModel.setPin(pin)
                if (!appLockEnabled) viewModel.setAppLockEnabled(true)
                showSetPin = false
            }
        )
    }

    // 检查更新结果对话框（仅提示，不强制；下载与应用内完成）
    when (val us = viewModel.updateState.collectAsState().value) {
        is UpdateCheckState.HasUpdate -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdate() },
                title = { Text("发现新版本") },
                text = {
                    Text(
                        "Mementide Plus 有新版本 ${us.latestVersion}（当前 ${BuildConfig.VERSION_NAME}）。\n\n是否下载安装？不强制更新，可稍后再决定。"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.startDownload() }) { Text("下载更新") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdate() }) { Text("稍后再说") }
                }
            )
        }
        is UpdateCheckState.Downloading -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdate() },
                title = { Text("正在下载更新") },
                text = {
                    Column {
                        LinearProgressIndicator(
                            progress = { us.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "已下载 ${(us.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissUpdate() }) { Text("后台下载") }
                }
            )
        }
        is UpdateCheckState.DownloadReady -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdate() },
                title = { Text("下载完成") },
                text = { Text("新版本已下载完成，是否现在安装？") },
                confirmButton = {
                    TextButton(onClick = {
                        if (!context.packageManager.canRequestPackageInstalls()) {
                            // 未授权「安装未知应用」，先引导去系统设置授权
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    android.net.Uri.parse("package:${context.packageName}")
                                )
                            )
                            viewModel.dismissUpdate()
                        } else {
                            installDownloadedApk(context, us.filePath)
                            viewModel.dismissUpdate()
                        }
                    }) { Text("安装") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdate() }) { Text("稍后") }
                }
            )
        }
        is UpdateCheckState.UpToDate -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdate() },
                title = { Text("已是最新版本") },
                text = { Text("当前已是最新版本 ${BuildConfig.VERSION_NAME}") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissUpdate() }) { Text("好的") }
                }
            )
        }
        else -> {}
    }
}

/** 用 FileProvider 唤起系统安装界面（安装下载好的 APK） */
private fun installDownloadedApk(context: Context, filePath: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "com.reverigma.mementideplus.fileprovider",
        File(filePath)
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}
