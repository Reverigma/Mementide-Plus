package com.reverigma.habittrack.data.repo

import com.reverigma.habittrack.data.dao.HabitDao
import com.reverigma.habittrack.data.dao.HabitRecordDao
import com.reverigma.habittrack.data.model.Habit
import com.reverigma.habittrack.data.model.HabitRecord
import com.reverigma.habittrack.util.DateUtils
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

    suspend fun setDone(habitId: String, date: String, done: Boolean) {
        recordDao.upsert(HabitRecord(habitId, date, done))
    }

    suspend fun isDone(habitId: String, date: String): Boolean =
        recordDao.get(habitId, date)?.done == true

    suspend fun getDoneDates(habitId: String): Set<String> =
        recordDao.getDoneDates(habitId).toSet()

    fun todayDoneIds(): Flow<Set<String>> =
        recordDao.observeDoneHabitIds(DateUtils.todayStr()).map { it.toSet() }
}
