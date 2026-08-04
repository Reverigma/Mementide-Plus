package com.reverigma.habittrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.reverigma.habittrack.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeHabits(): Flow<List<Habit>>

    @Insert
    suspend fun insert(habit: Habit)
}
