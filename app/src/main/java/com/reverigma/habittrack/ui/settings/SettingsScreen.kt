package com.reverigma.habittrack.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val hasPin by viewModel.hasPin.collectAsState()
    var showSetPin by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("设置", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

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
