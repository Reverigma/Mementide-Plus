package com.reverigma.habittrack.data.dao

import androidx.room.*
import com.reverigma.habittrack.data.model.HabitRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitRecordDao {

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun get(habitId: String, date: String): HabitRecord?

    @Query("SELECT habitId FROM habit_records WHERE date = :date AND done = 1")
    fun observeDoneHabitIds(date: String): Flow<List<String>>

    @Query("SELECT date FROM habit_records WHERE habitId = :habitId AND done = 1")
    suspend fun getDoneDates(habitId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: HabitRecord)

    @Query("DELETE FROM habit_records WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: String, date: String)
}
