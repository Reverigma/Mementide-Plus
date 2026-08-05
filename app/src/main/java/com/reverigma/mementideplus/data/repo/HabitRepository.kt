package com.reverigma.mementideplus.data.repo

import com.reverigma.mementideplus.data.dao.HabitDao
import com.reverigma.mementideplus.data.dao.HabitRecordDao
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.data.model.HabitRecord
import com.reverigma.mementideplus.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val recordDao: HabitRecordDao
) {
    fun habits(): Flow<List<Habit>> = habitDao.observeHabits()

    suspend fun addHabit(h: Habit) = habitDao.insert(h)
    suspend fun updateHabit(h: Habit) = habitDao.update(h)
    suspend fun deleteHabit(h: Habit) = habitDao.delete(h)

    /** 打卡/撤销打卡；记录打卡时间戳（epoch millis） */
    suspend fun setDone(habitId: String, date: String, done: Boolean, timestamp: Long = System.currentTimeMillis()) {
        recordDao.upsert(HabitRecord(habitId, date, done, timestamp))
    }

    /** 移动习惯顺序：把 id 移到 target 位置（before 为 null 表示末尾） */
    suspend fun moveHabit(id: String, beforeId: String?, afterId: String?) {
        val all = habitDao.getAll()
        val list = all.toMutableList()
        val idx = list.indexOfFirst { it.id == id }
        if (idx < 0) return
        val item = list.removeAt(idx)
        val targetIdx = when {
            beforeId != null -> list.indexOfFirst { it.id == beforeId }.let { if (it < 0) list.size else it }
            afterId != null -> list.indexOfFirst { it.id == afterId }.let { if (it < 0) list.size else it + 1 }
            else -> list.size
        }
        list.add(targetIdx.coerceIn(0, list.size), item)
        // 重写 sortOrder：-1 之后从 0 开始递增
        list.forEachIndexed { i, h ->
            if (h.sortOrder != i) {
                habitDao.update(h.copy(sortOrder = i))
            }
        }
    }

    suspend fun isDone(habitId: String, date: String): Boolean =
        recordDao.get(habitId, date)?.done == true

    suspend fun getDoneDates(habitId: String): Set<String> =
        recordDao.getDoneDates(habitId).toSet()

    fun todayDoneIds(): Flow<Set<String>> =
        recordDao.observeDoneHabitIds(DateUtils.todayStr()).map { it.toSet() }
}
