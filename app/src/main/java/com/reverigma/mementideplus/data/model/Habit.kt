package com.reverigma.mementideplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String = "✅",
    val colorInt: Int = 0xFF4F46E5.toInt(),
    val targetPerWeek: Int = 7,
    val createdAt: Long = System.currentTimeMillis()
)
