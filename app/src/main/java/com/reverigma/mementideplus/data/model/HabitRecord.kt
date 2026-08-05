package com.reverigma.mementideplus.data.model

import androidx.room.Entity

/**
 * 某习惯在某一天的打卡记录。
 * 主键为 (habitId, date)，通过 done 标记是否完成，便于补录与撤销。
 * timestamp 记录本次打卡的时间（epoch millis），用于展示"几点打卡"。
 */
@Entity(tableName = "habit_records", primaryKeys = ["habitId", "date"])
data class HabitRecord(
    val habitId: String,
    val date: String,        // yyyy-MM-dd
    val done: Boolean = true,
    val timestamp: Long = 0L // epoch millis，0 表示旧数据未记录
)
