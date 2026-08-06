package com.reverigma.mementideplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String = "✅",
    /** 非空时使用 Material Icon（存图标名），空时使用 emoji */
    val iconName: String = "",
    val colorInt: Int = 0xFF4F46E5.toInt(),
    val targetPerWeek: Int = 7,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
