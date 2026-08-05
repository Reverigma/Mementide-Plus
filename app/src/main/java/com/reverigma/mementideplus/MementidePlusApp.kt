package com.reverigma.mementideplus

import android.app.Application
import com.reverigma.mementideplus.reminder.ReminderReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MementidePlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderReceiver.ensureChannel(this)
    }
}
