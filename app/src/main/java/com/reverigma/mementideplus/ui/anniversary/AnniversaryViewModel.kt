package com.reverigma.mementideplus.ui.anniversary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reverigma.mementideplus.data.model.Anniversary
import com.reverigma.mementideplus.data.model.REPEAT_MONTHLY
import com.reverigma.mementideplus.data.model.REPEAT_NONE
import com.reverigma.mementideplus.data.model.REPEAT_YEARLY
import com.reverigma.mementideplus.data.repo.AnniversaryRepository
import com.reverigma.mementideplus.data.settings.AppSettings
import com.reverigma.mementideplus.util.AnniversaryCountdown
import com.reverigma.mementideplus.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AnniversaryItem(
    val anniversary: Anniversary,
    val countdownDays: Long   // >=0 还有N天；<0 已过N天
)

data class AnniversaryUiState(
    val items: List<AnniversaryItem> = emptyList()
)

@HiltViewModel
class AnniversaryViewModel @Inject constructor(
    private val repo: AnniversaryRepository,
    private val appSettings: AppSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnniversaryUiState())
    val uiState: StateFlow<AnniversaryUiState> = _uiState

    /** 高级模式：卡片显示编辑/删除按钮。 */
    val advancedMode: StateFlow<Boolean> = appSettings.advancedMode

    /** 点击卡片 emoji 弹出海报预览。 */
    val posterTap: StateFlow<Boolean> = appSettings.posterTap

    init { refresh() }

    private fun refresh() {
        viewModelScope.launch {
            val list = repo.anniversaries().first()
            val today = DateUtils.todayStr()
            val items = list.map { a ->
                AnniversaryItem(a, AnniversaryCountdown.countdownDays(a, today))
            }.sortedBy { it.countdownDays }
            _uiState.value = AnniversaryUiState(items)
        }
    }

    fun add(name: String, iconName: String, colorInt: Int, date: String, repeatType: String, note: String, calendarType: String) {
        viewModelScope.launch {
            repo.add(
                Anniversary(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    emoji = "",
                    iconName = iconName,
                    colorInt = colorInt,
                    date = date,
                    repeatType = repeatType,
                    note = note,
                    calendarType = calendarType
                )
            )
            refresh()
        }
    }

    /** 编辑纪念日：保留 id，仅更新展示属性 */
    fun update(a: Anniversary, name: String, iconName: String, colorInt: Int, date: String, repeatType: String, note: String, calendarType: String) {
        viewModelScope.launch {
            repo.update(
                a.copy(
                    name = name,
                    emoji = "",
                    iconName = iconName,
                    colorInt = colorInt,
                    date = date,
                    repeatType = repeatType,
                    note = note,
                    calendarType = calendarType
                )
            )
            refresh()
        }
    }

    fun delete(a: Anniversary) {
        viewModelScope.launch {
            repo.delete(a)
            refresh()
        }
    }
}

fun repeatLabel(r: String): String = when (r) {
    REPEAT_YEARLY -> "每年"
    REPEAT_MONTHLY -> "每月"
    REPEAT_NONE -> "不重复"
    else -> "不重复"
}
