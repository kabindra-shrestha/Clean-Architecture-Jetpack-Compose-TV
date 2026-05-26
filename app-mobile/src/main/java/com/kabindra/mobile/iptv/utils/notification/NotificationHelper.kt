package com.kabindra.mobile.iptv.utils.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kabindra.mobile.iptv.MainActivity
import com.kabindra.mobile.iptv.R
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import com.kabindra.tv.iptv.domain.entity.NotificationPriority

// ─────────────────────────────────────────────────────────────────────────────
// Notification Channel IDs
// ─────────────────────────────────────────────────────────────────────────────

object Channels {
    const val SOCKET_STATUS = "channel_socket_status"  // Foreground service persistent
    const val ALERTS = "channel_alerts"          // Incoming notifications
}

// ─────────────────────────────────────────────────────────────────────────────
// Notification IDs
// ─────────────────────────────────────────────────────────────────────────────

object NotifIds {
    const val FOREGROUND_SERVICE = 1001
    const val ALERT_BASE = 2000  // +hashCode of message id for uniqueness
}

private const val TAG = "NotificationHelper"

// ─────────────────────────────────────────────────────────────────────────────
// NotificationHelper
// ─────────────────────────────────────────────────────────────────────────────

object NotificationHelper {

    /**
     * Create both notification channels. Must be called before posting notifications.
     * Safe to call multiple times.
     */
    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ── 1. Socket status channel (silent, persistent) ──
        val statusChannel = NotificationChannel(
            Channels.SOCKET_STATUS,
            "Socket Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the persistent socket connection state"
            setShowBadge(false)
            setSound(null, null)
            enableVibration(false)
        }

        // ── 2. Alerts channel (high importance, sound) ──
        val alertChannel = NotificationChannel(
            Channels.ALERTS,
            "Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming socket notification alerts"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 100, 250)
        }

        manager.createNotificationChannel(statusChannel)
        manager.createNotificationChannel(alertChannel)
    }

    // ── Foreground Service Notification ───────────────────────────────────────

    fun buildServiceNotification(context: Context, statusText: String): Notification {
        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, Channels.SOCKET_STATUS)
            .setContentTitle("TV Notify")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    // ── Alert Notification (background tap-to-open / full-screen wake) ───────

    fun postAlertNotification(
        context: Context,
        message: NotificationMessage,
        forceFullScreen: Boolean = false
    ): Boolean {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tapping the notification → opens MainActivity with the message
        val alertIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_NOTIFICATION_ID, message.id)
            putExtra(MainActivity.EXTRA_TITLE, message.title)
            putExtra(MainActivity.EXTRA_MESSAGE, message.message)
            putExtra(MainActivity.EXTRA_PRIORITY, message.priority)
            putExtra(MainActivity.EXTRA_TIMESTAMP, message.timestamp)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            message.id.hashCode(),
            alertIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val shouldFullScreen =
            forceFullScreen || message.priorityEnum == NotificationPriority.CRITICAL
        val fullScreenAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            manager.canUseFullScreenIntent()
        } else {
            true
        }
        val useFullScreenIntent = shouldFullScreen && fullScreenAllowed

        if (shouldFullScreen && !fullScreenAllowed) {
            Log.w(TAG, "Full-screen intent not allowed by system settings")
        }

        val priority = if (useFullScreenIntent) {
            NotificationCompat.PRIORITY_MAX
        } else {
            when (message.priorityEnum) {
                NotificationPriority.CRITICAL -> NotificationCompat.PRIORITY_MAX
                NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                NotificationPriority.NORMAL -> NotificationCompat.PRIORITY_DEFAULT
                NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            }
        }

        val builder = NotificationCompat.Builder(context, Channels.ALERTS)
            .setContentTitle("[${message.priority}] ${message.title}")
            .setContentText(message.message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setCategory(
                if (useFullScreenIntent) NotificationCompat.CATEGORY_ALARM
                else NotificationCompat.CATEGORY_MESSAGE
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (useFullScreenIntent) {
            builder.setFullScreenIntent(pendingIntent, true)
        }

        val notification = builder.build()

        val notifId = NotifIds.ALERT_BASE + (message.id.hashCode() and 0xFFFF)
        manager.notify(notifId, notification)
        return useFullScreenIntent
    }
}
