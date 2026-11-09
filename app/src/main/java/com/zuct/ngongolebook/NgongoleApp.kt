package com.zuct.ngongolebook

import android.app.Application
import com.zuct.ngongolebook.data.AppDatabase
import com.zuct.ngongolebook.data.DebtRepository
import com.zuct.ngongolebook.notifications.NotificationHelper
import com.zuct.ngongolebook.notifications.ReminderManager

/** Creates the app-wide objects once. ViewModels get them through this class. */
class NgongoleApp : Application() {

    val repository: DebtRepository by lazy {
        DebtRepository(AppDatabase.get(this).debtDao())
    }

    val reminders: ReminderManager by lazy { ReminderManager(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        reminders.restoreSchedule()
    }
}
