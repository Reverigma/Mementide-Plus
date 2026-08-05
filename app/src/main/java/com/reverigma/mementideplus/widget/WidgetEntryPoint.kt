package com.reverigma.mementideplus.widget

import com.reverigma.mementideplus.data.dao.HabitDao
import com.reverigma.mementideplus.data.dao.HabitRecordDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun habitDao(): HabitDao
    fun recordDao(): HabitRecordDao
}
