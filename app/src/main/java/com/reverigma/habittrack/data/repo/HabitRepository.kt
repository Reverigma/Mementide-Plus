package com.reverigma.habittrack.data.repo

import com.reverigma.habittrack.data.model.Habit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor() {
    // M0 占位：返回示例数据，后续接入 Room（HabitDao）做真实持久化
    fun sampleHabits(): List<Habit> = listOf(
        Habit("1", "喝水", "💧", targetPerWeek = 7),
        Habit("2", "运动", "🏃", targetPerWeek = 3),
        Habit("3", "阅读", "📚", targetPerWeek = 5)
    )
}
