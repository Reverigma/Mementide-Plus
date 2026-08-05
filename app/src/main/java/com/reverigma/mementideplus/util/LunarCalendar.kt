package com.reverigma.mementideplus.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 农历转换工具（纯 Kotlin，无第三方依赖）。
 * 支持 1900-01-31 ~ 2099 范围公历 <-> 农历互转，算法与经典 ChineseCalendar 一致。
 *
 * 数据表每项（int）：
 * - 低 4 位 (0xF)   ：闰月月份，0 表示无闰月
 * - bit 0x10000     ：闰月为 30 天（否则 29 天）
 * - bit 0x8000>>m   ：第 m 个月为 30 天（否则 29 天）
 */
object LunarCalendar {

    // 1900~2099 农历数据表
    private val LUNAR_INFO = intArrayOf(
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x16a95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557,
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0,
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0,
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6,
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570,
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0,
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5,
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930,
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530,
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45,
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0,
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0,
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4,
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0,
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160,
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252,
        0x0d520
    )

    // 1900-01-31 = 农历 1900 年正月初一
    private val BASE_SOLAR = LocalDate.of(1900, 1, 31)

    /** 农历年闰月月份（0=无闰月） */
    fun leapMonth(lunarYear: Int): Int {
        val y = lunarYear - 1900
        return if (y in 0 until LUNAR_INFO.size) (LUNAR_INFO[y] and 0xF) else 0
    }

    /** 农历年闰月天数（无闰月返回 0） */
    fun leapDays(lunarYear: Int): Int {
        if (leapMonth(lunarYear) == 0) return 0
        val y = lunarYear - 1900
        return if ((LUNAR_INFO[y] and 0x10000) != 0) 30 else 29
    }

    /** 农历第 month 月天数（29/30）；闰月请用 leapDays */
    fun monthDays(lunarYear: Int, month: Int): Int {
        val y = lunarYear - 1900
        if (y !in 0 until LUNAR_INFO.size) return 30
        return if ((LUNAR_INFO[y] and (0x10000 shr month)) != 0) 30 else 29
    }

    /** 农历年总天数（含闰月） */
    fun yearDays(lunarYear: Int): Int {
        val y = lunarYear - 1900
        if (y !in 0 until LUNAR_INFO.size) return 354
        var sum = 348
        var i = 0x8000
        while (i > 0x8) {
            if ((LUNAR_INFO[y] and i) != 0) sum += 1
            i = i shr 1
        }
        return sum + leapDays(lunarYear)
    }

    /** 公历 -> 农历。返回 [lunarYear, lunarMonth, lunarDay, isLeap(0/1)] */
    fun solarToLunar(solar: LocalDate): IntArray {
        var offset = ChronoUnit.DAYS.between(BASE_SOLAR, solar).toInt()
        var year = 1900
        while (year < 2100 && offset >= yearDays(year)) {
            offset -= yearDays(year)
            year++
        }
        val leap = leapMonth(year)
        var isLeap = false
        var month = 1
        while (true) {
            var days: Int
            if (leap > 0 && month == leap + 1 && !isLeap) {
                month--
                isLeap = true
                days = leapDays(year)
            } else {
                days = monthDays(year, month)
            }
            if (offset < days) break
            offset -= days
            if (isLeap && month == leap) {
                isLeap = false
                month = leap + 1
            } else {
                month++
            }
        }
        return intArrayOf(year, month, offset + 1, if (isLeap) 1 else 0)
    }

    /** 农历 -> 公历。isLeap 表示该月为闰月（month 指向被闰的那个月） */
    fun lunarToSolar(lunarYear: Int, lunarMonth: Int, lunarDay: Int, isLeap: Boolean = false): LocalDate {
        var offset = 0
        var y = 1900
        while (y < lunarYear) {
            offset += yearDays(y)
            y++
        }
        val leap = leapMonth(lunarYear)
        var m = 1
        while (m < lunarMonth) {
            offset += monthDays(lunarYear, m)
            m++
        }
        if (isLeap) {
            offset += leapDays(lunarYear)
        }
        offset += lunarDay - 1
        return BASE_SOLAR.plusDays(offset.toLong())
    }

    /**
     * 把"农历月日"（MM-dd 字符串，可能带负号表示闰月？这里统一不带）映射到 after 之后最近的公历日期。
     * lunarMonthDay 形如 "MM-dd"。
     */
    fun nextSolarOccurrence(lunarMonthDay: String, after: LocalDate): LocalDate {
        val parts = lunarMonthDay.split("-")
        val month = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val day = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val base = lunarToSolar(after.year, month, day)
        // 若今年的农历月日已过，用明年
        var result = if (base.isBefore(after)) base.plusYears(1) else base
        // 若明年该农历日期无效（如闰月不存在），顺延再试
        var guard = 0
        while (guard < 3) {
            result = try {
                lunarToSolar(result.year, month, day).let {
                    if (it.isBefore(after)) it.plusYears(1) else it
                }
            } catch (e: Exception) {
                result.plusYears(1)
            }
            guard++
            if (result.year <= after.year + 2) break
        }
        return result
    }

    /** 农历月日中文标签，如 "八月十五" */
    fun lunarLabel(month: Int, day: Int): String {
        val m = arrayOf("正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
        val d = arrayOf(
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
            "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
            "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
        )
        return "${m[month - 1]}月${d[day - 1]}"
    }
}
