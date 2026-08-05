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
    }
}
