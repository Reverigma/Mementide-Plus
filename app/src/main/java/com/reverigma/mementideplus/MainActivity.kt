package com.reverigma.mementideplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.reverigma.mementideplus.ui.anniversary.AddAnniversaryDialog
import com.reverigma.mementideplus.ui.anniversary.AnniversaryScreen
import com.reverigma.mementideplus.ui.anniversary.AnniversaryViewModel
import com.reverigma.mementideplus.ui.home.AddHabitDialog
import com.reverigma.mementideplus.ui.home.HomeScreen
import com.reverigma.mementideplus.ui.home.HomeViewModel
import com.reverigma.mementideplus.ui.lock.LockScreen
import com.reverigma.mementideplus.ui.settings.SettingsScreen
import com.reverigma.mementideplus.ui.settings.SettingsViewModel
import com.reverigma.mementideplus.ui.stats.StatsScreen
import com.reverigma.mementideplus.ui.stats.StatsViewModel
import com.reverigma.mementideplus.ui.theme.MementideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 与 setContent 内 hiltViewModel() 共享同一 Activity 作用域实例
    private val settingsVm: SettingsViewModel by viewModels()
    private var lastStopTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settingsVm.themeMode.collectAsState()
            val isDark = when (themeMode) {
                2 -> true
                1 -> false
                else -> isSystemInDarkTheme()
            }
            MementideTheme(darkTheme = isDark) {
                var selected by remember { mutableStateOf(0) }
                val homeVm: HomeViewModel = hiltViewModel()
                val anniVm: AnniversaryViewModel = hiltViewModel()
                val statsVm: StatsViewModel = hiltViewModel()
                var showAddHabit by remember { mutableStateOf(false) }
                var showAddAnni by remember { mutableStateOf(false) }

                val lockEnabled by settingsVm.appLockEnabled.collectAsState()
                val hasPin by settingsVm.hasPin.collectAsState()
                val needsLock by settingsVm.needsLock.collectAsState()

                // 应用锁拦截：开关开 + 已设 PIN + 当前处于加锁态 → 展示锁屏
                if (lockEnabled && hasPin && needsLock) {
                    LockScreen(
                        verify = settingsVm::verifyPin,
                        onSuccess = { settingsVm.unlock() },
                        modifier = Modifier.fillMaxSize()
                    )
                    return@MementideTheme
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selected == 0,
                                onClick = { selected = 0 },
                                icon = { Icon(Icons.Filled.CheckCircle, "今日") },
                                label = { Text("今日") }
                            )
                            NavigationBarItem(
                                selected = selected == 1,
                                onClick = { selected = 1 },
                                icon = { Icon(Icons.Filled.Cake, "纪念日") },
                                label = { Text("纪念日") }
                            )
                            NavigationBarItem(
                                selected = selected == 2,
                                onClick = { selected = 2 },
                                icon = { Icon(Icons.Filled.ShowChart, "统计") },
                                label = { Text("统计") }
                            )
                            NavigationBarItem(
                                selected = selected == 3,
                                onClick = { selected = 3 },
                                icon = { Icon(Icons.Filled.Settings, "设置") },
                                label = { Text("设置") }
                            )
                        }
                    },
                    floatingActionButton = {
                        if (selected != 2 && selected != 3) {
                            FloatingActionButton(
                                onClick = { if (selected == 0) showAddHabit = true else showAddAnni = true }
                            ) {
                                Icon(Icons.Filled.Add, "添加")
                            }
                        }
                    }
                ) { padding ->
                    when (selected) {
                        0 -> HomeScreen(homeVm, Modifier.fillMaxSize().padding(padding))
                        1 -> AnniversaryScreen(anniVm, Modifier.fillMaxSize().padding(padding))
                        2 -> StatsScreen(statsVm, Modifier.fillMaxSize().padding(padding))
                        3 -> SettingsScreen(settingsVm, Modifier.fillMaxSize().padding(padding))
                    }
                }

                if (showAddHabit) {
                    AddHabitDialog(
                        onDismiss = { showAddHabit = false },
                        onConfirm = { n, e, c, t ->
                            homeVm.addHabit(n, e, c, t)
                            showAddHabit = false
                        }
                    )
                }
                if (showAddAnni) {
                    AddAnniversaryDialog(
                        onDismiss = { showAddAnni = false },
                        onConfirm = { n, e, d, r, nt ->
                            anniVm.add(n, e, d, r, nt)
                            showAddAnni = false
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // 冷启动（lastStopTime==0）或离后台超过 30 秒 → 进入加锁态
        val elapsed = if (lastStopTime == 0L) Long.MAX_VALUE else System.currentTimeMillis() - lastStopTime
        if (elapsed > 30_000) settingsVm.lock()
    }

    override fun onStop() {
        super.onStop()
        lastStopTime = System.currentTimeMillis()
    }
}
