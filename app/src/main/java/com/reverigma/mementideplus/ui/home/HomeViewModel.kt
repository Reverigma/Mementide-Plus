package com.reverigma.mementideplus.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.data.repo.HabitRepository
import com.reverigma.mementideplus.data.settings.AppSettings
import com.reverigma.mementideplus.util.DateUtils
import com.reverigma.mementideplus.widget.HabitWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class HabitItem(
    val habit: Habit,
    val doneToday: Boolean,
    val streak: Int,
    val totalDone: Int = 0,
    val thisWeekDone: Int = 0
)

data class HomeUiState(
    val items: List<HabitItem> = emptyList(),
    val dateLabel: String = "",
    val doneCount: Int = 0,
    val total: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: HabitRepository,
    private val appSettings: AppSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    /** 高级模式：卡片显示补录/编辑/删除按钮。 */
    val advancedMode: StateFlow<Boolean> = appSettings.advancedMode

    /** 点击卡片 emoji 弹出海报预览。 */
    val posterTap: StateFlow<Boolean> = appSettings.posterTap

    init { refresh() }

    private fun refresh() {
        viewModelScope.launch {
            val habitList = repo.habits().first()
            val today = DateUtils.todayStr()
            val mon = DateUtils.thisWeekMonday(today)
            val items = habitList.map { h ->
                val doneDates = repo.getDoneDates(h.id)
                HabitItem(
                    habit = h,
                    doneToday = doneDates.contains(today),
                    streak = DateUtils.currentStreak(doneDates, today),
                    totalDone = doneDates.size,
                    thisWeekDone = doneDates.count { it >= mon }
                )
            }
            _uiState.value = HomeUiState(
                items = items,
                dateLabel = DateUtils.formatTodayLabel(),
                doneCount = items.count { it.doneToday },
                total = items.size
            )
        }
    }

    fun toggleToday(habit: Habit) {
        viewModelScope.launch {
            val today = DateUtils.todayStr()
            val done = repo.isDone(habit.id, today)
            repo.setDone(habit.id, today, !done)
            refresh()
            HabitWidgetProvider.updateAll(appContext)
        }
    }

    /** 补录 / 撤销某一天的打卡；可指定打卡时间（epoch millis） */
    fun setDoneForDate(habitId: String, date: String, done: Boolean, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repo.setDone(habitId, date, done, timestamp)
            refresh()
        }
    }

    fun addHabit(name: String, iconName: String, colorInt: Int, targetPerWeek: Int, imagePath: String = "") {
        viewModelScope.launch {
            val all = repo.habits().first()
            val nextOrder = (all.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repo.addHabit(
                Habit(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    emoji = "",
                    iconName = iconName,
                    imagePath = imagePath,
                    colorInt = colorInt,
                    targetPerWeek = targetPerWeek,
                    sortOrder = nextOrder
                )
            )
            refresh()
            HabitWidgetProvider.updateAll(appContext)
        }
    }

    /** 上移一个位置（sortOrder 交换） */
    fun moveHabitUp(habit: Habit) {
        viewModelScope.launch {
            val all = repo.habits().first().sortedBy { it.sortOrder }
            val idx = all.indexOfFirst { it.id == habit.id }
            if (idx <= 0) return@launch
            val prev = all[idx - 1]
            repo.updateHabit(habit.copy(sortOrder = prev.sortOrder))
            repo.updateHabit(prev.copy(sortOrder = habit.sortOrder))
            refresh()
            HabitWidgetProvider.updateAll(appContext)
        }
    }

    /** 下移一个位置（sortOrder 交换） */
    fun moveHabitDown(habit: Habit) {
        viewModelScope.launch {
            val all = repo.habits().first().sortedBy { it.sortOrder }
            val idx = all.indexOfFirst { it.id == habit.id }
            if (idx < 0 || idx >= all.size - 1) return@launch
            val next = all[idx + 1]
            repo.updateHabit(habit.copy(sortOrder = next.sortOrder))
            repo.updateHabit(next.copy(sortOrder = habit.sortOrder))
            refresh()
            HabitWidgetProvider.updateAll(appContext)
        }
    }

    /** 置顶：排到最前 */
    fun moveHabitToTop(habit: Habit) {
        viewModelScope.launch {
            val all = repo.habits().first().sortedBy { it.sortOrder }
            val minOrder = all.minOfOrNull { it.sortOrder } ?: 0
            repo.updateHabit(habit.copy(sortOrder = minOrder - 1))
            refresh()
            HabitWidgetProvider.updateAll(appContext)
        }
    }

    /** 编辑习惯：保留 id 与打卡记录，仅更新展示属性 */
    fun updateHabit(habit: Habit, name: String, iconName: String, colorInt: Int, targetPerWeek: Int, imagePath: String = "") {
        viewModelScope.launch {
            repo.updateHabit(
                habit.copy(
                    name = name,
                    emoji = "",
                    iconName = iconName,
                    imagePath = imagePath,
                    colorInt = colorInt,
                    targetPerWeek = targetPerWeek
                )
            )
            refresh()
            HabitWidgetProvider.updateAll(appContext)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repo.deleteHabit(habit)
            refresh()
            HabitWidgetProvider.updateAll(appContext)
        }
    }
}
