package com.reverigma.habittrack.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reverigma.habittrack.data.settings.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettings
) : ViewModel() {

    /** 应用锁总开关（来自持久化设置）。 */
    val appLockEnabled: StateFlow<Boolean> = appSettings.appLockEnabled

    /** 是否已设置 PIN。 */
    val hasPin: StateFlow<Boolean> = appSettings.hasPinState

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
}
