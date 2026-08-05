package com.reverigma.mementideplus.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.reverigma.mementideplus.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val hasPin by viewModel.hasPin.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    var showSetPin by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = { Text("设置") })
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("应用锁", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(2.dp))
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
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showSetPin = true }) { Text("更改 PIN") }
                            TextButton(onClick = { viewModel.removePin() }) { Text("关闭并移除 PIN") }
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("外观", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("跟随系统" to 0, "浅色" to 1, "深色" to 2).forEach { (label, mode) ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("数据备份", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "导出为 JSON 文件到本机任意位置（系统文件选择器），或导入恢复。数据完全在本地，不上传。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exportLauncher.launch("mementide-plus-backup-${DateUtils.todayStr()}.json") }) {
                            Text("导出备份")
                        }
                        Button(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                            Text("导入备份")
                        }
                    }
                }
            }

            status?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
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
}
