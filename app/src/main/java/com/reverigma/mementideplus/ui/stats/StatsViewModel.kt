package com.reverigma.mementideplus.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reverigma.mementideplus.data.dao.HabitRecordDao
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.data.model.HabitRecord
import com.reverigma.mementideplus.data.repo.HabitRepository
import com.reverigma.mementideplus.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayCell(val date: String, val count: Int, val weekday: Int)
data class HabitStat(val habit: Habit, val currentStreak: Int, val totalDone: Int, val thisWeekDone: Int)
data class StatsUiState(
    val weeks: List<List<DayCell>> = emptyList(),
    val perHabit: List<HabitStat> = emptyList(),
    val totalCompletions: Int = 0,
    val bestStreak: Int = 0,
    val activeHabits: Int = 0,
    val maxCount: Int = 1
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val habitRepo: HabitRepository,
    private val recordDao: HabitRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init { load() }

    private fun load() {
        viewModelScope.launch {
            combine(habitRepo.habits(), recordDao.observeAll()) { habits, records ->
                buildState(habits, records)
            }.collect { _uiState.value = it }
        }
    }

    private fun buildState(habits: List<Habit>, records: List<HabitRecord>): StatsUiState {
        val today = DateUtils.todayStr()
        val done = records.filter { it.done }
        val countMap = done.groupingBy { it.date }.eachCount()

        val perHabit = habits.map { h ->
            val dates = done.filter { it.habitId == h.id }.map { it.date }.toSet()
            val streak = DateUtils.currentStreak(dates, today)
            val total = dates.size
            val mon = DateUtils.thisWeekMonday(today)
            val thisWeek = dates.count { it >= mon }
            HabitStat(h, streak, total, thisWeek)
        }

        val weeksCount = 18
        val totalDays = weeksCount * 7
        val end = DateUtils.parse(today)
        val first = end.minusDays((totalDays - 1).toLong())
        val padFront = (first.dayOfWeek.value - 1).toLong()
        val gridStart = first.minusDays(padFront)
        val cells = (0 until totalDays).map { i ->
            val d = gridStart.plusDays(i.toLong())
            val ds = DateUtils.format(d)
            DayCell(ds, countMap[ds] ?: 0, d.dayOfWeek.value)
        }
        val weeks = cells.chunked(7)
        val maxCount = (countMap.values.maxOrNull() ?: 0).coerceAtLeast(1)
        val total = done.size
        val best = perHabit.maxOfOrNull { it.currentStreak } ?: 0

        return StatsUiState(weeks, perHabit, total, best, habits.size, maxCount)
    }
}
