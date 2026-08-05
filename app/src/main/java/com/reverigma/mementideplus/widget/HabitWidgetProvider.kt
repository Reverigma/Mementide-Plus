package com.reverigma.mementideplus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.reverigma.mementideplus.MainActivity
import com.reverigma.mementideplus.R
import com.reverigma.mementideplus.data.dao.HabitDao
import com.reverigma.mementideplus.data.dao.HabitRecordDao
import com.reverigma.mementideplus.util.DateUtils
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

const val ACTION_TOGGLE_HABIT = "com.reverigma.mementideplus.action.TOGGLE_HABIT"
const val EXTRA_HABIT_ID = "habit_id"

class HabitWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TOGGLE_HABIT) {
            val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
            val pendingResult = goAsync()
            try {
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, WidgetEntryPoint::class.java)
                val habitDao = entryPoint.habitDao()
                val recordDao = entryPoint.recordDao()
                runBlocking {
                    val habit = habitDao.getById(habitId) ?: return@runBlocking
                    val today = DateUtils.todayStr()
                    val existing = recordDao.get(habitId, today)
                    if (existing == null) {
                        recordDao.upsert(
                            com.reverigma.mementideplus.data.model.HabitRecord(
                                habitId = habitId,
                                date = today,
                                done = true
                            )
                        )
                    } else {
                        recordDao.delete(habitId, today)
                    }
                }
                updateAll(app)
            } finally {
                pendingResult.finish()
            }
            return
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        try {
            val app = context.applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(
                app,
                WidgetEntryPoint::class.java
            )
            val habitDao = entryPoint.habitDao()
            val recordDao = entryPoint.recordDao()

            val habits = runBlocking { habitDao.getAll() }
            val today = DateUtils.todayStr()
            val doneIds = runBlocking {
                recordDao.getDoneHabitIds(today).toSet()
            }
            val undone = habits.filter { it.id !in doneIds }
            val doneCount = habits.size - undone.size

            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId, doneCount, habits.size, undone)
            }
        } finally {
            pendingResult.finish()
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                ComponentName(context, HabitWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    Intent(context, HabitWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    }
                )
            }
        }

        private fun updateWidget(
            context: Context,
            mgr: AppWidgetManager,
            widgetId: Int,
            done: Int,
            total: Int,
            undone: List<com.reverigma.mementideplus.data.model.Habit>
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_habit)

            // Click to open app
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pending)

            // Progress
            views.setTextViewText(R.id.widget_progress_text, "$done/$total")

            // Undone habits (max 3)
            for (i in 0..2) {
                val itemId = when (i) {
                    0 -> R.id.widget_item_0
                    1 -> R.id.widget_item_1
                    else -> R.id.widget_item_2
                }
                val dotId = when (i) {
                    0 -> R.id.widget_dot_0
                    1 -> R.id.widget_dot_1
                    else -> R.id.widget_dot_2
                }
                val textId = when (i) {
                    0 -> R.id.widget_text_0
                    1 -> R.id.widget_text_1
                    else -> R.id.widget_text_2
                }

                if (i < undone.size) {
                    val h = undone[i]
                    views.setViewVisibility(itemId, android.view.View.VISIBLE)
                    views.setTextColor(dotId, h.colorInt)
                    views.setTextViewText(textId, "${h.emoji} ${h.name}")
                    // 点击该项直接完成打卡
                    val toggleIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                        action = ACTION_TOGGLE_HABIT
                        putExtra(EXTRA_HABIT_ID, h.id)
                    }
                    val togglePending = PendingIntent.getBroadcast(
                        context, h.id.hashCode(), toggleIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(itemId, togglePending)
                } else {
                    views.setViewVisibility(itemId, android.view.View.GONE)
                }
            }

            // Empty state
            views.setViewVisibility(
                R.id.widget_empty,
                if (undone.isEmpty() && total > 0) android.view.View.VISIBLE else android.view.View.GONE
            )

            mgr.updateAppWidget(widgetId, views)
        }
    }
}
