package com.zuct.ngongolebook.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Remembers whether reminders are on and schedules / cancels the daily background job. */
class ReminderManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    val isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) schedule() else WorkManager.getInstance(appContext).cancelUniqueWork(WORK_NAME)
    }

    /** Called on app start so the schedule survives updates and reboots. */
    fun restoreSchedule() {
        if (isEnabled) schedule()
    }

    /** Fires the worker once, right now, so the user (and the demo) can see a notification. */
    fun sendTestNow() {
        val request = OneTimeWorkRequestBuilder<OverdueReminderWorker>()
            .setInputData(workDataOf(OverdueReminderWorker.KEY_TEST to true))
            .build()
        WorkManager.getInstance(appContext).enqueue(request)
    }

    private fun schedule() {
        val request = PeriodicWorkRequestBuilder<OverdueReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(millisUntilNext(REMINDER_HOUR), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun millisUntilNext(hour: Int): Long {
        val now = Calendar.getInstance()
        val next = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis - now.timeInMillis
    }

    companion object {
        const val REMINDER_HOUR = 8
        private const val WORK_NAME = "daily_overdue_reminder"
        private const val KEY_ENABLED = "reminders_enabled"
    }
}
