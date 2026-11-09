package com.zuct.ngongolebook.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Formatters {

    const val DAY_MILLIS = 24L * 60 * 60 * 1000

    /** 1250.5 -> "K1,250.50" (Zambian Kwacha). */
    fun money(amount: Double): String = "K" + String.format(Locale.US, "%,.2f", amount)

    /** For due dates. They are stored as UTC midnight, so they must be shown in UTC. */
    fun dueDate(millis: Long): String =
        SimpleDateFormat("d MMM yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(millis))

    /** For real moments in time (when a payment was recorded), shown in the phone's time zone. */
    fun moment(millis: Long): String =
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(Date(millis))

    /** Today's calendar date (in the phone's time zone) expressed as UTC midnight. */
    fun todayUtcMidnight(): Long {
        val local = Calendar.getInstance()
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun daysFromToday(days: Int): Long = todayUtcMidnight() + days * DAY_MILLIS
}
