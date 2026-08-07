package com.reverigma.mementideplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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

                // 左右滑动翻页（底部导航联动）
                val pagerState = rememberPagerState(initialPage = 0) { 4 }
                val scope = rememberCoroutineScope()
                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { selected = it }
                }

                val lockEnabled by settingsVm.appLockEnabled.collectAsState()
                val hasPin by settingsVm.hasPin.collectAsState()
                val needsLock by settingsVm.needsLock.collectAsState()

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
                        NavigationBar(
                            // 与页面背景同色 + 零阴影，避免导航栏上方出现一条缝/阴影线
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            // 选中态用主色（靛蓝），取代默认的 secondaryContainer（蓝绿）
                            val navColors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            NavigationBarItem(
                                selected = selected == 0,
                                onClick = { scope.launch { pagerState.scrollToPage(0) } },
                                colors = navColors,
                                icon = {
                                    Icon(
                                        imageVector = if (selected == 0) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                        contentDescription = "今日",
                                        modifier = Modifier.size(28.dp)
                                    )
                                },
                                label = null
                            )
                            NavigationBarItem(
                                selected = selected == 1,
                                onClick = { scope.launch { pagerState.scrollToPage(1) } },
                                colors = navColors,
                                icon = {
                                    Icon(
                                        imageVector = if (selected == 1) Icons.Filled.Cake else Icons.Outlined.Cake,
                                        contentDescription = "纪念日",
                                        modifier = Modifier.size(28.dp)
                                    )
                                },
                                label = null
                            )
                            NavigationBarItem(
                                selected = selected == 2,
                                onClick = { scope.launch { pagerState.scrollToPage(2) } },
                                colors = navColors,
                                icon = {
                                    Icon(
                                        imageVector = if (selected == 2) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                        contentDescription = "统计",
                                        modifier = Modifier.size(28.dp)
                                    )
                                },
                                label = null
                            )
                            NavigationBarItem(
                                selected = selected == 3,
                                onClick = { scope.launch { pagerState.scrollToPage(3) } },
                                colors = navColors,
                                icon = {
                                    Icon(
                                        imageVector = if (selected == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                                        contentDescription = "设置",
                                        modifier = Modifier.size(28.dp)
                                    )
                                },
                                label = null
                            )
                        }
                    },
                    floatingActionButton = {
                        if (selected != 2 && selected != 3) {
                            FloatingActionButton(
                                onClick = { if (selected == 0) showAddHabit = true else showAddAnni = true },
                                shape = RoundedCornerShape(18.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 2.dp
                                )
                            ) {
                                Icon(Icons.Filled.Add, "添加")
                            }
                        }
                    }
                ) { padding ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().padding(padding)
                    ) { page ->
                        when (page) {
                            0 -> HomeScreen(homeVm, Modifier.fillMaxSize(), onAddHabit = { showAddHabit = true })
                            1 -> AnniversaryScreen(anniVm, Modifier.fillMaxSize(), onAddAnniversary = { showAddAnni = true })
                            2 -> StatsScreen(statsVm, Modifier.fillMaxSize())
                            3 -> SettingsScreen(settingsVm, Modifier.fillMaxSize())
                        }
                    }
                }

                if (showAddHabit) {
                    AddHabitDialog(
                        onDismiss = { showAddHabit = false },
                        onConfirm = { n, ic, c, t, img ->
                            homeVm.addHabit(n, ic, c, t, img)
                            showAddHabit = false
                        }
                    )
                }
                if (showAddAnni) {
                    AddAnniversaryDialog(
                        onDismiss = { showAddAnni = false },
                        onConfirm = { n, ic, c, d, r, nt, ct, img ->
                            anniVm.add(n, ic, c, d, r, nt, ct, img)
                            showAddAnni = false
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val elapsed = if (lastStopTime == 0L) Long.MAX_VALUE else System.currentTimeMillis() - lastStopTime
        if (elapsed > 30_000) settingsVm.lock()
    }

    override fun onStop() {
        super.onStop()
        lastStopTime = System.currentTimeMillis()
    }
}
