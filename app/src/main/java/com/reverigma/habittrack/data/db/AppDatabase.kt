package com.reverigma.habittrack.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.reverigma.habittrack.data.dao.HabitDao
import com.reverigma.habittrack.data.model.Habit

@Database(entities = [Habit::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
