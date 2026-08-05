package com.reverigma.mementideplus.util

import com.reverigma.mementideplus.data.model.REPEAT_MONTHLY
import com.reverigma.mementideplus.data.model.REPEAT_NONE
import com.reverigma.mementideplus.data.model.REPEAT_YEARLY
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    private val ISO = DateTimeFormatter.ISO_LOCAL_DATE

    fun todayStr(): String = LocalDate.now().format(ISO)
    fun parse(s: String): LocalDate = LocalDate.parse(s, ISO)
    fun format(d: LocalDate): String = d.format(ISO)

    /** 例如 "2026年8月4日 星期二" */
    fun formatTodayLabel(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE", Locale.CHINESE))

    /** 例如 "2026年8月4日" */
    fun formatDate(s: String): String = parse(s)
        .format(DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINESE))

    /** 供 Material3 DatePicker 初始化使用 */
    fun epochMillisForDate(s: String): Long =
        parse(s).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /** 把 DatePicker 返回的毫秒转回 yyyy-MM-dd（按本机时区） */
    fun fromEpochMillis(millis: Long): String =
        java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(ISO)

    /** 连续打卡天数：从今天（或昨天，若今天未打）往前数连续的完成日 */
    fun currentStreak(doneDates: Set<String>, today: String): Int {
        var streak = 0
        var cursor = today
        if (!doneDates.contains(cursor)) cursor = minusDays(today, 1)
        while (doneDates.contains(cursor)) {
            streak++
            cursor = minusDays(cursor, 1)
        }
        return streak
    }

    fun minusDays(date: String, n: Int): String = parse(date).minusDays(n.toLong()).format(ISO)
    fun addDaysStr(date: String, n: Int): String = parse(date).plusDays(n.toLong()).format(ISO)
    fun dayOfWeekValue(date: String): Int = parse(date).dayOfWeek.value

    /** 本周一（周一为一周起点）的 yyyy-MM-dd */
    fun thisWeekMonday(today: String): String {
        val t = parse(today)
        val back = (t.dayOfWeek.value - 1).toLong()
        return t.minusDays(back).format(ISO)
    }

    /** 计算下一次纪念日（含重复规则） */
    fun nextOccurrence(base: LocalDate, repeat: String, today: LocalDate): LocalDate {
        return when (repeat) {
            REPEAT_NONE -> base
            REPEAT_YEARLY -> {
                var c = base.withYear(today.year)
                if (c < today) c = c.plusYears(1)
                c
            }
            REPEAT_MONTHLY -> {
                val day = base.dayOfMonth
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
                c
            }
            else -> base
        }
    }

    /** 距下一次纪念日的天数：>=0 还有N天；<0 已过N天（仅不重复类型可能为负） */
    fun countdownDays(date: String, repeat: String, today: String): Long {
        val base = parse(date)
        val t = parse(today)
        val next = nextOccurrence(base, repeat, t)
        return ChronoUnit.DAYS.between(t, next)
    }
}
