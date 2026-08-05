package com.reverigma.mementideplus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

const val REPEAT_YEARLY = "yearly"
const val REPEAT_MONTHLY = "monthly"
const val REPEAT_NONE = "none"

/**
 * 纪念日。
 * date 为首次发生日期（yyyy-MM-dd）；
 * repeatType 决定倒计时如何循环：每年 / 每月 / 不重复。
 * 重复方式仅在创建时设定，之后不可改（卡片上不再提供切换）。
 */
@Entity(tableName = "anniversaries")
data class Anniversary(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String = "🎉",
    val colorInt: Int = 0xFFE11D48.toInt(),       // 卡片主题色（玫瑰红）
    val date: String,                          // yyyy-MM-dd，首次发生日期
    val repeatType: String = REPEAT_YEARLY,    // yearly | monthly | none
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
