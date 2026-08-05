package com.reverigma.mementideplus.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mementide.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHabitDao(db: AppDatabase) = db.habitDao()

    @Provides
    fun provideHabitRecordDao(db: AppDatabase) = db.habitRecordDao()

    @Provides
    fun provideAnniversaryDao(db: AppDatabase) = db.anniversaryDao()
}
