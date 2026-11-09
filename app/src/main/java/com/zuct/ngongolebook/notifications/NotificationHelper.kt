package com.zuct.ngongolebook.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zuct.ngongolebook.MainActivity
import com.zuct.ngongolebook.R

object NotificationHelper {

    private const val CHANNEL_ID = "overdue_reminders"
    private const val NOTIFICATION_ID = 1001

    /** Android 8+ requires every notification to belong to a channel. Safe to call many times. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Overdue debt reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "A daily reminder when customers have passed their promised payment date"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** True on Android 13+ only when the user has granted POST_NOTIFICATIONS. Always true below 13. */
    fun hasRuntimePermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Permission granted AND notifications not switched off in system settings. */
    fun canNotify(context: Context): Boolean =
        hasRuntimePermission(context) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    @SuppressLint("MissingPermission") // guarded by canNotify()
    fun show(context: Context, title: String, text: String) {
        if (!canNotify(context)) return // missing permission must never crash the app
        createChannel(context)

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
