package com.reverigma.habittrack.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reverigma.habittrack.data.model.Habit
import com.reverigma.habittrack.data.repo.HabitRepository
import com.reverigma.habittrack.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class HabitItem(
    val habit: Habit,
    val doneToday: Boolean,
    val streak: Int
)

data class HomeUiState(
    val items: List<HabitItem> = emptyList(),
    val dateLabel: String = "",
    val doneCount: Int = 0,
    val total: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { refresh() }

    private fun refresh() {
        viewModelScope.launch {
            val habitList = repo.habits().first()
            val today = DateUtils.todayStr()
            val items = habitList.map { h ->
                val doneDates = repo.getDoneDates(h.id)
                HabitItem(
                    habit = h,
                    doneToday = doneDates.contains(today),
                    streak = DateUtils.currentStreak(doneDates, today)
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
        }
    }

    /** 补录 / 撤销某一天的打卡 */
    fun setDoneForDate(habitId: String, date: String, done: Boolean) {
        viewModelScope.launch {
            repo.setDone(habitId, date, done)
            refresh()
        }
    }

    fun addHabit(name: String, emoji: String, colorInt: Int, targetPerWeek: Int) {
        viewModelScope.launch {
            repo.addHabit(
                Habit(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    emoji = emoji,
                    colorInt = colorInt,
                    targetPerWeek = targetPerWeek
                )
            )
            refresh()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repo.deleteHabit(habit)
            refresh()
        }
    }
}
