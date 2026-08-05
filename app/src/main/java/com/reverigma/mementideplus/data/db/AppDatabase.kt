package com.reverigma.mementideplus.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.reverigma.mementideplus.data.dao.AnniversaryDao
import com.reverigma.mementideplus.data.dao.HabitDao
import com.reverigma.mementideplus.data.dao.HabitRecordDao
import com.reverigma.mementideplus.data.model.Anniversary
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.data.model.HabitRecord

@Database(
    entities = [Habit::class, HabitRecord::class, Anniversary::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitRecordDao(): HabitRecordDao
    abstract fun anniversaryDao(): AnniversaryDao
}
