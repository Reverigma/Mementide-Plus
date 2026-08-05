package com.reverigma.mementideplus.data.settings

import android.content.Context
import androidx.core.content.edit
import com.reverigma.mementideplus.util.PinCrypto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用设置（纯本地，SharedPreferences）。
 * - 应用锁：默认关闭（appLockEnabled=false），未设 PIN（hasPin=false）。
 * - 外观主题：themeMode=0 跟随系统 / 1 浅色 / 2 深色，默认跟随系统。
 */
@Singleton
class AppSettings @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _appLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_LOCK, false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _hasPin = MutableStateFlow(prefs.getString(KEY_PIN_HASH, "").isNullOrEmpty().not())
    val hasPinState: StateFlow<Boolean> = _hasPin.asStateFlow()
    val hasPin: Boolean get() = _hasPin.value

    private val _themeMode = MutableStateFlow(prefs.getInt(KEY_THEME, 0))
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(prefs.getBoolean(KEY_REMINDER_ENABLED, false))
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    /** 每日提醒时间 "HH:mm"，默认 21:00 */
    private val _reminderTime = MutableStateFlow(prefs.getString(KEY_REMINDER_TIME, "21:00") ?: "21:00")
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()

    /** 高级模式：开启后卡片显示补录/编辑/删除等进阶操作，默认关闭保持界面简洁。 */
    private val _advancedMode = MutableStateFlow(prefs.getBoolean(KEY_ADVANCED, false))
    val advancedMode: StateFlow<Boolean> = _advancedMode.asStateFlow()

    /** 统计页：是否显示月度日历（默认开） */
    private val _statsMonthCalendar = MutableStateFlow(prefs.getBoolean(KEY_STATS_MONTH, true))
    val statsMonthCalendar: StateFlow<Boolean> = _statsMonthCalendar.asStateFlow()

    /** 统计页：是否显示近 18 周打卡密度热力图（默认关，与月历去重） */
    private val _statsHeatmap = MutableStateFlow(prefs.getBoolean(KEY_STATS_HEATMAP, false))
    val statsHeatmap: StateFlow<Boolean> = _statsHeatmap.asStateFlow()

    /** 统计页视图顺序："month,heatmap" 或 "heatmap,month"，默认月历在前 */
    private val _statsViewOrder = MutableStateFlow(prefs.getString(KEY_STATS_ORDER, "month,heatmap") ?: "month,heatmap")
    val statsViewOrder: StateFlow<String> = _statsViewOrder.asStateFlow()

    fun setAdvancedMode(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_ADVANCED, enabled) }
        _advancedMode.value = enabled
    }

    fun setStatsMonthCalendar(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_STATS_MONTH, enabled) }
        _statsMonthCalendar.value = enabled
    }

    fun setStatsHeatmap(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_STATS_HEATMAP, enabled) }
        _statsHeatmap.value = enabled
    }

    fun setStatsViewOrder(order: String) {
        prefs.edit { putString(KEY_STATS_ORDER, order) }
        _statsViewOrder.value = order
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_REMINDER_ENABLED, enabled) }
        _reminderEnabled.value = enabled
    }

    fun setReminderTime(time: String) {
        prefs.edit { putString(KEY_REMINDER_TIME, time) }
        _reminderTime.value = time
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_LOCK, enabled) }
        _appLockEnabled.value = enabled
    }

    /** 设置/重设 PIN（覆盖旧的盐与哈希）。 */
    fun setPin(pin: String) {
        val salt = PinCrypto.generateSalt()
        val hash = PinCrypto.hashPin(pin, salt)
        prefs.edit {
            putString(KEY_SALT, salt)
            putString(KEY_PIN_HASH, hash)
        }
        _hasPin.value = true
    }

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_SALT, "") ?: ""
        val hash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        if (hash.isEmpty()) return false
        return PinCrypto.hashPin(pin, salt) == hash
    }

    /** 移除 PIN 并顺手关闭应用锁。 */
    fun clearPin() {
        prefs.edit {
            remove(KEY_PIN_HASH)
            remove(KEY_SALT)
            putBoolean(KEY_LOCK, false)
        }
        _hasPin.value = false
        _appLockEnabled.value = false
    }

    fun setThemeMode(mode: Int) {
        prefs.edit { putInt(KEY_THEME, mode) }
        _themeMode.value = mode
    }

    companion object {
        private const val PREFS_NAME = "mementide_settings"
        private const val KEY_LOCK = "app_lock_enabled"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_SALT = "pin_salt"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_ADVANCED = "advanced_mode"
        private const val KEY_STATS_MONTH = "stats_month_calendar"
        private const val KEY_STATS_HEATMAP = "stats_heatmap"
        private const val KEY_STATS_ORDER = "stats_view_order"
    }
}
