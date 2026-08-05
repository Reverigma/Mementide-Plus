package com.reverigma.mementideplus.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reverigma.mementideplus.data.dao.AnniversaryDao
import com.reverigma.mementideplus.data.dao.HabitDao
import com.reverigma.mementideplus.data.dao.HabitRecordDao
import com.reverigma.mementideplus.data.model.Anniversary
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.data.model.HabitRecord

@Database(
    entities = [Habit::class, HabitRecord::class, Anniversary::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitRecordDao(): HabitRecordDao
    abstract fun anniversaryDao(): AnniversaryDao

    companion object {
        /** v3 -> v4：habits 加 sortOrder、habit_records 加 timestamp、anniversaries 加 calendarType */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE habit_records ADD COLUMN timestamp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE anniversaries ADD COLUMN calendarType TEXT NOT NULL DEFAULT 'solar'")
            }
        }
    }
}
