package com.reverigma.mementideplus.data.dao

import androidx.room.*
import com.reverigma.mementideplus.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, createdAt DESC")
    fun observeHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY sortOrder ASC, createdAt DESC")
    suspend fun getAll(): List<Habit>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: String): Habit?

    @Insert
    suspend fun insert(habit: Habit)

    @Insert
    suspend fun insertAll(list: List<Habit>)

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("DELETE FROM habits")
    suspend fun deleteAll()
}
