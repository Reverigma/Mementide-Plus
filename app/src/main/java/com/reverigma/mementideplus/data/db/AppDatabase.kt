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
    version = 6,
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

        /** v4 -> v5：habits / anniversaries 加 iconName（空=emoji，非空=Material Icon 名） */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN iconName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE anniversaries ADD COLUMN iconName TEXT NOT NULL DEFAULT ''")
            }
        }

        /** v5 -> v6：移除 emoji 图标，把空 iconName 的旧数据填充默认图标名（习惯=star，纪念日=cake） */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE habits SET iconName = 'star' WHERE iconName = ''")
                db.execSQL("UPDATE anniversaries SET iconName = 'cake' WHERE iconName = ''")
            }
        }
    }
}
