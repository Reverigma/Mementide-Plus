package com.reverigma.mementideplus.data.repo

import com.reverigma.mementideplus.data.dao.AnniversaryDao
import com.reverigma.mementideplus.data.dao.HabitDao
import com.reverigma.mementideplus.data.dao.HabitRecordDao
import com.reverigma.mementideplus.data.model.Anniversary
import com.reverigma.mementideplus.data.model.Habit
import com.reverigma.mementideplus.data.model.HabitRecord
import com.reverigma.mementideplus.data.model.REPEAT_YEARLY
import com.reverigma.mementideplus.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本机备份：把所有习惯 / 打卡记录 / 纪念日导出为 JSON，或从 JSON 导入覆盖。
 * 纯本地，不依赖任何云端或联网权限。
 */
@Singleton
class BackupRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val recordDao: HabitRecordDao,
    private val anniDao: AnniversaryDao
) {
    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val habits = habitDao.getAll()
        val records = recordDao.getAll()
        val annis = anniDao.getAll()

        val root = JSONObject().apply {
            put("app", "Mementide Plus")
            put("version", 1)
            put("exportedAt", DateUtils.todayStr())
        }
        val jh = JSONArray()
        habits.forEach { h ->
            jh.put(JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("emoji", h.emoji)
                put("colorInt", h.colorInt)
                put("targetPerWeek", h.targetPerWeek)
                put("createdAt", h.createdAt)
            })
        }
        root.put("habits", jh)

        val jr = JSONArray()
        records.forEach { r ->
            jr.put(JSONObject().apply {
                put("habitId", r.habitId)
                put("date", r.date)
                put("done", r.done)
            })
        }
        root.put("records", jr)

        val ja = JSONArray()
        annis.forEach { a ->
            ja.put(JSONObject().apply {
                put("id", a.id)
                put("name", a.name)
                put("emoji", a.emoji)
                put("colorInt", a.colorInt)
                put("date", a.date)
                put("repeatType", a.repeatType)
                put("note", a.note)
                put("createdAt", a.createdAt)
            })
        }
        root.put("anniversaries", ja)

        root.toString(2)
    }

    /** 导入并覆盖现有数据，返回导入的记录总条数。 */
    suspend fun importJson(json: String): Int = withContext(Dispatchers.IO) {
        val root = JSONObject(json)

        val habits = mutableListOf<Habit>()
        val jh = root.getJSONArray("habits")
        for (i in 0 until jh.length()) {
            val o = jh.getJSONObject(i)
            habits.add(
                Habit(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    emoji = o.optString("emoji", "✅"),
                    colorInt = o.optInt("colorInt", 0xFF4F46E5.toInt()),
                    targetPerWeek = o.optInt("targetPerWeek", 7),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val records = mutableListOf<HabitRecord>()
        val jr = root.getJSONArray("records")
        for (i in 0 until jr.length()) {
            val o = jr.getJSONObject(i)
            records.add(
                HabitRecord(
                    habitId = o.getString("habitId"),
                    date = o.getString("date"),
                    done = o.optBoolean("done", true)
                )
            )
        }

        val annis = mutableListOf<Anniversary>()
        val ja = root.getJSONArray("anniversaries")
        for (i in 0 until ja.length()) {
            val o = ja.getJSONObject(i)
            annis.add(
                Anniversary(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    emoji = o.optString("emoji", "🎉"),
                    colorInt = o.optInt("colorInt", 0xFFE11D48.toInt()),
                    date = o.getString("date"),
                    repeatType = o.optString("repeatType", REPEAT_YEARLY),
                    note = o.optString("note", ""),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        habitDao.deleteAll()
        recordDao.deleteAll()
        anniDao.deleteAll()
        if (habits.isNotEmpty()) habitDao.insertAll(habits)
        if (records.isNotEmpty()) recordDao.insertAll(records)
        if (annis.isNotEmpty()) anniDao.insertAll(annis)

        habits.size + records.size + annis.size
    }
}
