package com.reverigma.mementideplus.util

import com.reverigma.mementideplus.data.model.Anniversary
import com.reverigma.mementideplus.data.model.CALENDAR_LUNAR
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 纪念日倒计时计算，支持公历与农历。
 */
object AnniversaryCountdown {

    /**
     * 计算距下一次纪念日的天数：
     * >=0 还有 N 天；<0 已过 N 天（仅不重复类型可能为负）。
     */
    fun countdownDays(a: Anniversary, today: String): Long {
        val t = LocalDate.parse(today)
        val next = nextOccurrence(a, t)
        return ChronoUnit.DAYS.between(t, next)
    }

    /** 下一次发生日期 */
    fun nextOccurrence(a: Anniversary, today: LocalDate): LocalDate {
        val repeat = a.repeatType
        return when (repeat) {
            com.reverigma.mementideplus.data.model.REPEAT_NONE -> baseDate(a)
            com.reverigma.mementideplus.data.model.REPEAT_YEARLY -> nextYearly(a, today)
            com.reverigma.mementideplus.data.model.REPEAT_MONTHLY -> nextMonthly(a, today)
            else -> baseDate(a)
        }
    }

    /** 基准日期（首次发生的公历日期）；农历类型按今年换算 */
    private fun baseDate(a: Anniversary): LocalDate {
        return if (a.calendarType == CALENDAR_LUNAR) {
            val parts = a.date.split("-")
            val m = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val d = parts.getOrNull(1)?.toIntOrNull() ?: 1
            LunarCalendar.lunarToSolar(LocalDate.now().year, m, d)
        } else {
            LocalDate.parse(a.date)
        }
    }

    private fun nextYearly(a: Anniversary, today: LocalDate): LocalDate {
        return if (a.calendarType == CALENDAR_LUNAR) {
            val parts = a.date.split("-")
            val m = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val d = parts.getOrNull(1)?.toIntOrNull() ?: 1
            val thisYear = LunarCalendar.lunarToSolar(today.year, m, d)
            var c = if (thisYear.isBefore(today)) LunarCalendar.lunarToSolar(today.year + 1, m, d) else thisYear
            // 防御：若该年无此农历日期则顺延
            var guard = 0
            while (c.isBefore(today) && guard < 3) {
                c = LunarCalendar.lunarToSolar(c.year + 1, m, d)
                guard++
            }
            c
        } else {
            var c = LocalDate.parse(a.date).withYear(today.year)
            if (c < today) c = c.plusYears(1)
            c
        }
    }

    private fun nextMonthly(a: Anniversary, today: LocalDate): LocalDate {
        val day = if (a.calendarType == CALENDAR_LUNAR) {
            // 农历"每月"语义不明确，回退为每月同一天（取日）
            a.date.split("-").getOrNull(1)?.toIntOrNull() ?: 1
        } else {
            LocalDate.parse(a.date).dayOfMonth
        }
        var c = if (day <= today.lengthOfMonth()) {
            today.withDayOfMonth(day)
        } else {
            today.withDayOfMonth(today.lengthOfMonth())
        }
        while (c < today) {
            val nm = c.plusMonths(1)
            c = if (day <= nm.lengthOfMonth()) {
                nm.withDayOfMonth(day)
            } else {
                nm.withDayOfMonth(nm.lengthOfMonth())
            }
        }
        return c
    }
}
