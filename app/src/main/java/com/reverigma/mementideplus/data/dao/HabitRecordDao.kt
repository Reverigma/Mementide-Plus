package com.reverigma.mementideplus.data.dao

import androidx.room.*
import com.reverigma.mementideplus.data.model.HabitRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitRecordDao {

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun get(habitId: String, date: String): HabitRecord?

    @Query("SELECT habitId FROM habit_records WHERE date = :date AND done = 1")
    fun observeDoneHabitIds(date: String): Flow<List<String>>

    @Query("SELECT habitId FROM habit_records WHERE date = :date AND done = 1")
    suspend fun getDoneHabitIds(date: String): List<String>

    @Query("SELECT date FROM habit_records WHERE habitId = :habitId AND done = 1")
    suspend fun getDoneDates(habitId: String): List<String>

    @Query("SELECT * FROM habit_records")
    fun observeAll(): Flow<List<HabitRecord>>

    @Query("SELECT * FROM habit_records")
    suspend fun getAll(): List<HabitRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: HabitRecord)

    @Insert
    suspend fun insertAll(list: List<HabitRecord>)

    @Query("DELETE FROM habit_records WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: String, date: String)

    @Query("DELETE FROM habit_records")
    suspend fun deleteAll()
}
