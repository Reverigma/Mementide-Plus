package com.reverigma.habittrack.data.model

import androidx.room.Entity

/**
 * 某习惯在某一天的打卡记录。
 * 主键为 (habitId, date)，通过 done 标记是否完成，便于补录与撤销。
 */
@Entity(tableName = "habit_records", primaryKeys = ["habitId", "date"])
data class HabitRecord(
    val habitId: String,
    val date: String,        // yyyy-MM-dd
    val done: Boolean = true
)
