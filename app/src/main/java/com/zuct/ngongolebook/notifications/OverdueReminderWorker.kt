package com.zuct.ngongolebook.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zuct.ngongolebook.NgongoleApp
import com.zuct.ngongolebook.domain.Formatters
import com.zuct.ngongolebook.domain.balance

/**
 * Runs in the background (even if the app is closed). Looks for debts past their due date
 * and posts one notification summarising them.
 */
class OverdueReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as NgongoleApp
        val isTest = inputData.getBoolean(KEY_TEST, false)

        val overdue = app.repository.overdue(Formatters.todayUtcMidnight())
        if (overdue.isEmpty() && !isTest) return Result.success()

        val total = overdue.sumOf { it.balance }
        val title = "Ngongole Book"
        val text = when {
            overdue.isEmpty() -> "Reminders are working. Nobody is overdue right now."
            overdue.size == 1 -> "1 customer is overdue: ${Formatters.money(total)} still owed."
            else -> "${overdue.size} customers are overdue: ${Formatters.money(total)} still owed."
        }
        NotificationHelper.show(applicationContext, title, text)
        return Result.success()
    }

    companion object {
        const val KEY_TEST = "is_test"
    }
}
