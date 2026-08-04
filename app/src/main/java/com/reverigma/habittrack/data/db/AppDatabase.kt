package com.reverigma.habittrack.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.reverigma.habittrack.data.dao.AnniversaryDao
import com.reverigma.habittrack.data.dao.HabitDao
import com.reverigma.habittrack.data.dao.HabitRecordDao
import com.reverigma.habittrack.data.model.Anniversary
import com.reverigma.habittrack.data.model.Habit
import com.reverigma.habittrack.data.model.HabitRecord

@Database(
    entities = [Habit::class, HabitRecord::class, Anniversary::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitRecordDao(): HabitRecordDao
    abstract fun anniversaryDao(): AnniversaryDao
}
