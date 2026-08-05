package com.reverigma.mementideplus.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.reverigma.mementideplus.MainActivity
import com.reverigma.mementideplus.R
import com.reverigma.mementideplus.data.settings.AppSettings
import com.reverigma.mementideplus.util.DateUtils
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

/**
 * 每日提醒：打卡提醒 + 纪念日当天提醒。
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val settings = AppSettings(context.applicationContext)
        if (!settings.reminderEnabled.value) return

        val pendingResult = goAsync()
        try {
            val app = context.applicationContext
            val entryPoint = EntryPointAccessors.fromApplication(app, ReminderEntryPoint::class.java)
            val habitDao = entryPoint.habitDao()
            val recordDao = entryPoint.recordDao()
            val anniDao = entryPoint.anniversaryDao()

            val today = DateUtils.todayStr()

            // 打卡提醒：今天还有多少未完成的习惯
            runBlocking {
                val habits = habitDao.getAll()
                val doneIds = recordDao.getDoneHabitIds(today).toSet()
                val undoneCount = habits.count { it.id !in doneIds }
                if (undoneCount > 0) {
                    notify(
                        app,
                        id = 100,
                        title = "今日打卡提醒",
                        text = if (undoneCount == 1) "还有 1 个习惯未打卡，去完成吧"
                        else "还有 $undoneCount 个习惯未打卡，去完成吧",
                        appSettings = settings
                    )
                }

                // 纪念日提醒：今天到期的纪念日
                val annis = anniDao.getAll()
                val todayAnniversaries = annis.filter {
                    DateUtils.countdownDays(it.date, it.repeatType, today) == 0L
                }
                if (todayAnniversaries.isNotEmpty()) {
                    val names = todayAnniversaries.joinToString("、") { "${it.emoji} ${it.name}" }
                    notify(
                        app,
                        id = 101,
                        title = "纪念日提醒",
                        text = "今天是 $names，别忘了庆祝一下 🎉",
                        appSettings = settings
                    )
                }
            }
        } finally {
            pendingResult.finish()
        }
    }

    private fun notify(context: Context, id: Int, title: String, text: String, appSettings: AppSettings) {
        val channelId = CHANNEL_ID
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // 权限被拒则静默
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_reminder"

        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "每日提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "习惯打卡与纪念日提醒"
                }
                context.getSystemService(NotificationManager::class.java)
                    ?.createNotificationChannel(channel)
            }
        }
    }
}
