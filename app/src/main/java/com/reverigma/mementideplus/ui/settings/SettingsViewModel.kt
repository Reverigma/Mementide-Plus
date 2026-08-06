package com.reverigma.mementideplus.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reverigma.mementideplus.data.repo.BackupRepository
import com.reverigma.mementideplus.data.settings.AppSettings
import com.reverigma.mementideplus.util.UpdateChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val backupRepo: BackupRepository,
    @ApplicationContext private val appContext: Context
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

    /** 检查更新状态：手动触发，不自动检查、不强制更新。 */
    private val _updateState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val updateState: StateFlow<UpdateCheckState> = _updateState

    /** 手动检查更新（仅在点击「检查更新」时触发） */
    fun checkForUpdates(currentVersion: String) {
        if (_updateState.value is UpdateCheckState.Checking) return
        if (_updateState.value is UpdateCheckState.Downloading) return
        viewModelScope.launch {
            _updateState.value = UpdateCheckState.Checking
            val latest = UpdateChecker.fetchLatestRelease()
            _updateState.value = when {
                latest == null -> UpdateCheckState.Error("网络或服务器异常，请稍后再试")
                UpdateChecker.compareVersions(latest.version, currentVersion) > 0 ->
                    UpdateCheckState.HasUpdate(latest.version, latest.apkUrl)
                else -> UpdateCheckState.UpToDate
            }
        }
    }

    /** 应用内下载最新版 APK（带进度），完成后进入 DownloadReady 待安装 */
    fun startDownload() {
        val st = _updateState.value as? UpdateCheckState.HasUpdate ?: return
        viewModelScope.launch {
            _updateState.value = UpdateCheckState.Downloading(0f)
            val file = File(appContext.cacheDir, "share/mementide-update.apk")
            file.parentFile?.mkdirs()
            file.delete()
            val ok = UpdateChecker.downloadApk(st.apkUrl, file) { done, total ->
                val progress = if (total > 0) done.toFloat() / total else 0f
                _updateState.value = UpdateCheckState.Downloading(progress.coerceIn(0f, 1f))
            }
            _updateState.value = if (ok && file.length() > 0) {
                UpdateCheckState.DownloadReady(file.absolutePath)
            } else {
                UpdateCheckState.Error("下载失败，请检查网络后重试")
            }
        }
    }

    /** 关闭更新提示（不强制，用户可稍后再说） */
    fun dismissUpdate() {
        _updateState.value = UpdateCheckState.Idle
    }

    /** 导出全部数据为 JSON 字符串。 */
    suspend fun exportData(): String = backupRepo.exportJson()

    /** 从 JSON 导入并覆盖数据，返回导入的记录条数。 */
    suspend fun importData(json: String): Int = backupRepo.importJson(json)
}

/** 检查更新结果状态 */
sealed class UpdateCheckState {
    object Idle : UpdateCheckState()
    object Checking : UpdateCheckState()
    data class HasUpdate(val latestVersion: String, val apkUrl: String) : UpdateCheckState()
    object UpToDate : UpdateCheckState()
    data class Downloading(val progress: Float) : UpdateCheckState()
    data class DownloadReady(val filePath: String) : UpdateCheckState()
    data class Error(val message: String) : UpdateCheckState()
}
