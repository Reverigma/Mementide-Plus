package com.reverigma.mementideplus.reminder

import com.reverigma.mementideplus.data.dao.AnniversaryDao
import com.reverigma.mementideplus.data.dao.HabitDao
import com.reverigma.mementideplus.data.dao.HabitRecordDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun habitDao(): HabitDao
    fun recordDao(): HabitRecordDao
    fun anniversaryDao(): AnniversaryDao
}
