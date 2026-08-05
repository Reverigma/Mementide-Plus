package com.reverigma.mementideplus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reverigma.mementideplus.data.repo.BackupRepository
import com.reverigma.mementideplus.data.settings.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val backupRepo: BackupRepository
) : ViewModel() {

    /** 应用锁总开关（来自持久化设置）。 */
    val appLockEnabled: StateFlow<Boolean> = appSettings.appLockEnabled

    /** 是否已设置 PIN。 */
    val hasPin: StateFlow<Boolean> = appSettings.hasPinState

    /** 外观主题：0 跟随系统 / 1 浅色 / 2 深色。 */
    val themeMode: StateFlow<Int> = appSettings.themeMode

    /** 每日打卡提醒开关。 */
    val reminderEnabled: StateFlow<Boolean> = appSettings.reminderEnabled

    /** 每日提醒时间 "HH:mm"。 */
    val reminderTime: StateFlow<String> = appSettings.reminderTime

    /** 高级模式：卡片上显示补录/编辑/删除等进阶操作。 */
    val advancedMode: StateFlow<Boolean> = appSettings.advancedMode

    /** 统计页：月度日历显示开关。 */
    val statsMonthCalendar: StateFlow<Boolean> = appSettings.statsMonthCalendar

    /** 统计页：近 18 周热力图显示开关。 */
    val statsHeatmap: StateFlow<Boolean> = appSettings.statsHeatmap

    /** 统计页视图顺序："month,heatmap" 或 "heatmap,month"。 */
    val statsViewOrder: StateFlow<String> = appSettings.statsViewOrder

    /** 点击卡片 emoji 弹出海报预览。 */
    val posterTap: StateFlow<Boolean> = appSettings.posterTap

    /** 本次运行中的「是否处于加锁态」——由后台返回逻辑控制。 */
    private val _isLocked = MutableStateFlow(false)

    /** 是否需要展示锁屏：开关打开 且 已设 PIN 且 当前处于加锁态。 */
    val needsLock: StateFlow<Boolean> = combine(appLockEnabled, hasPin, _isLocked) { enabled, pin, locked ->
        enabled && pin && locked
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** 切后台/冷启动时调用，置为加锁态。 */
    fun lock() { _isLocked.value = true }

    /** PIN 验证通过后调用，解除加锁态。 */
    fun unlock() { _isLocked.value = false }

    fun setAppLockEnabled(enabled: Boolean) = appSettings.setAppLockEnabled(enabled)
    fun setPin(pin: String) = appSettings.setPin(pin)
    fun verifyPin(pin: String): Boolean = appSettings.verifyPin(pin)
    fun removePin() = appSettings.clearPin()
    fun setThemeMode(mode: Int) = appSettings.setThemeMode(mode)
    fun setReminderEnabled(enabled: Boolean) = appSettings.setReminderEnabled(enabled)
    fun setReminderTime(time: String) = appSettings.setReminderTime(time)
    fun setAdvancedMode(enabled: Boolean) = appSettings.setAdvancedMode(enabled)
    fun setStatsMonthCalendar(enabled: Boolean) = appSettings.setStatsMonthCalendar(enabled)
    fun setStatsHeatmap(enabled: Boolean) = appSettings.setStatsHeatmap(enabled)
    fun setStatsViewOrder(order: String) = appSettings.setStatsViewOrder(order)
    fun setPosterTap(enabled: Boolean) = appSettings.setPosterTap(enabled)

    /** 导出全部数据为 JSON 字符串。 */
    suspend fun exportData(): String = backupRepo.exportJson()

    /** 从 JSON 导入并覆盖数据，返回导入的记录条数。 */
    suspend fun importData(json: String): Int = backupRepo.importJson(json)
}
