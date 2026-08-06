package com.reverigma.mementideplus.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.reverigma.mementideplus.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** v2 -> v3: anniversaries 表新增 colorInt 列（默认玫瑰红 0xFFE11D48） */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE anniversaries ADD COLUMN colorInt INTEGER NOT NULL DEFAULT -2024120"
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mementide.db")
            .addMigrations(MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5, AppDatabase.MIGRATION_5_6)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHabitDao(db: AppDatabase) = db.habitDao()

    @Provides
    fun provideHabitRecordDao(db: AppDatabase) = db.habitRecordDao()

    @Provides
    fun provideAnniversaryDao(db: AppDatabase) = db.anniversaryDao()
}
