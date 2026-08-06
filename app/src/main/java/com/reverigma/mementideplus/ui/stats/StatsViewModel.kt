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
data class MonthCell(val date: String?, val count: Int, val isToday: Boolean, val isWeekend: Boolean = false)
data class StatsUiState(
    val weeks: List<List<DayCell>> = emptyList(),
    val perHabit: List<HabitStat> = emptyList(),
    val totalCompletions: Int = 0,
    val bestStreak: Int = 0,
    val activeHabits: Int = 0,
    val maxCount: Int = 1,
    val monthCells: List<MonthCell> = emptyList(),
    val monthLabel: String = "",
    /** 某天打卡了哪些习惯：date -> 习惯名列表 */
    val dayDetails: Map<String, List<String>> = emptyMap()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val habitRepo: HabitRepository,
    private val recordDao: HabitRecordDao,
    private val appSettings: com.reverigma.mementideplus.data.settings.AppSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    /** 统计页视图配置：月度日历显示开关 */
    val statsMonthCalendar: StateFlow<Boolean> = appSettings.statsMonthCalendar

    /** 统计页视图配置：热力图显示开关 */
    val statsHeatmap: StateFlow<Boolean> = appSettings.statsHeatmap

    /** 统计页视图配置：显示顺序 "month,heatmap" 或 "heatmap,month" */
    val statsViewOrder: StateFlow<String> = appSettings.statsViewOrder

    /** 月历偏移：0=当月，-1=上月，1=下月 */
    private val _monthOffset = MutableStateFlow(0)
    val monthOffset: StateFlow<Int> = _monthOffset

    init { load() }

    private fun load() {
        viewModelScope.launch {
            combine(
                habitRepo.habits(),
                recordDao.observeAll(),
                _monthOffset
            ) { habits, records, offset ->
                buildState(habits, records, offset)
            }.collect { _uiState.value = it }
        }
    }

    fun prevMonth() { _monthOffset.value -= 1 }
    fun nextMonth() { _monthOffset.value += 1 }
    fun resetMonth() { _monthOffset.value = 0 }

    private fun buildState(habits: List<Habit>, records: List<HabitRecord>, monthOffset: Int): StatsUiState {
        val today = DateUtils.todayStr()
        val done = records.filter { it.done }
        val countMap = done.groupingBy { it.date }.eachCount()
        // 当天打卡详情：date -> 习惯名列表
        val habitById = habits.associateBy { it.id }
        val dayDetails = done.groupBy { it.date }.mapValues { (_, recs) ->
            recs.mapNotNull { r -> habitById[r.habitId]?.name }.distinct()
        }

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

        // 月历：当月（或偏移月）从周日开始排
        val todayDate = DateUtils.parse(today)
        val monthDate = todayDate.plusMonths(monthOffset.toLong())
        val year = monthDate.year
        val month = monthDate.monthValue
        val firstOfMonth = monthDate.withDayOfMonth(1)
        val daysInMonth = monthDate.lengthOfMonth()
        // 周日=0 索引（java.time 周日=7，转成 0..6：周日=0）
        val leading = (firstOfMonth.dayOfWeek.value % 7)
        val monthCells = mutableListOf<MonthCell>()
        repeat(leading) { monthCells.add(MonthCell(null, 0, false)) }
        for (d in 1..daysInMonth) {
            val ds = "%04d-%02d-%02d".format(year, month, d)
            val isToday = ds == today
            monthCells.add(MonthCell(ds, countMap[ds] ?: 0, isToday))
        }
        // 补齐到整周（保持 7 列对齐）
        while (monthCells.size % 7 != 0) monthCells.add(MonthCell(null, 0, false))
        val monthLabel = monthDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年M月"))

        return StatsUiState(weeks, perHabit, total, best, habits.size, maxCount, monthCells, monthLabel, dayDetails)
    }
}
