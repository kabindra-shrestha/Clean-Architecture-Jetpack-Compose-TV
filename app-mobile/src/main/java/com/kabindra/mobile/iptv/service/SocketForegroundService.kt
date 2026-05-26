package com.kabindra.mobile.iptv.service

import android.app.ActivityOptions
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.kabindra.mobile.iptv.BuildConfig
import com.kabindra.mobile.iptv.MainActivity
import com.kabindra.mobile.iptv.MainApplication
import com.kabindra.mobile.iptv.R
import com.kabindra.mobile.iptv.socket.KtorSocketClient
import com.kabindra.mobile.iptv.utils.notification.NotifIds
import com.kabindra.mobile.iptv.utils.notification.NotificationHelper
import com.kabindra.tv.iptv.domain.entity.ConnectionState
import com.kabindra.tv.iptv.domain.entity.NotificationMessage
import com.kabindra.tv.iptv.domain.entity.NotificationPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "SocketForegroundService"
private const val WAKELOCK_TIMEOUT_MS = 60_000L  // 60 seconds max wakelock

// ─────────────────────────────────────────────────────────────────────────────
// SocketForegroundService
//
// A START_STICKY Foreground Service that:
//  1. Holds and manages the KtorSocketClient WebSocket connection
//  2. Updates its persistent notification with the connection state
//  3. On background message receipt:
//     - Launch MainActivity for the incoming message
//     - Acquire WakeLock for CRITICAL notifications
// ─────────────────────────────────────────────────────────────────────────────

class SocketForegroundService : Service() {

    companion object {
        const val ACTION_RELEASE_WAKELOCK = "com.intranet.tvnotify.action.RELEASE_WAKELOCK"

        // Shared singleton client accessible by ViewModel when bound
        @Volatile
        var socketClient: KtorSocketClient? = null
            private set

        fun startService(context: Context) {
            val intent = Intent(context, SocketForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, SocketForegroundService::class.java))
        }

        fun isRunning(): Boolean = socketClient != null
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        NotificationHelper.createChannels(this)

        // Start foreground immediately to satisfy Android's 5-second requirement
        val notification = NotificationHelper.buildServiceNotification(
            this, getString(R.string.notification_service_connected)
        )
        startForeground(NotifIds.FOREGROUND_SERVICE, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")

        if (intent?.action == ACTION_RELEASE_WAKELOCK) {
            releaseWakeLock()
            return START_STICKY
        }

        if (socketClient == null) {
            initializeSocketClient()
        }

        // START_STICKY: OS will restart this service with a null intent if killed
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null  // Not a bound service

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy — cleaning up")
        socketClient?.close()
        socketClient = null
        serviceScope.cancel()
        wakeLock?.release()
    }

    // ── Socket Setup ──────────────────────────────────────────────────────────

    private fun initializeSocketClient() {
        val client = KtorSocketClient()
        socketClient = client

        // ── Observe connection state → update persistent notification ──
        client.connectionState
            .onEach { state -> updateServiceNotification(state) }
            .launchIn(serviceScope)

        // ── Observe incoming notifications ──
        client.notifications
            .onEach { message -> handleIncomingNotification(message) }
            .launchIn(serviceScope)

        // ── Start WebSocket connection ──
        client.connect(
            host = BuildConfig.WS_HOST,
            port = BuildConfig.WS_PORT,
            path = BuildConfig.WS_PATH,
            scope = serviceScope
        )
    }

    // ── Notification Handling ─────────────────────────────────────────────────

    private fun handleIncomingNotification(message: NotificationMessage) {
        val appInForeground = MainApplication.isAppInForeground()
        Log.i(
            TAG,
            "Incoming: ${message.title} [${message.priority}] appInForeground=$appInForeground"
        )

        if (!appInForeground) {
            if (message.priorityEnum == NotificationPriority.CRITICAL) {
                acquireWakeLock()
            }
            val launchedViaPendingIntent = launchAlertViaPendingIntent(message)
            if (launchedViaPendingIntent) {
                Log.i(TAG, "Background alert launched via pending-intent path")
                return
            }

            val launchedDirectly = runCatching {
                launchMainActivity(message)
                true
            }.onFailure { error ->
                Log.e(TAG, "Direct activity launch in background failed: ${error.message}")
            }.getOrDefault(false)

            if (launchedDirectly) {
                Log.i(TAG, "Background alert launched via direct activity fallback")
                return
            }

            Log.w(TAG, "Activity launch blocked; posting full-screen notification fallback")
            NotificationHelper.postAlertNotification(
                context = this,
                message = message,
                forceFullScreen = true
            )
            return
        }

        when (message.priorityEnum) {
            NotificationPriority.CRITICAL -> {
                // Wake the screen and launch MainActivity directly
                acquireWakeLock()
                launchMainActivity(message)
            }

            NotificationPriority.HIGH,
            NotificationPriority.NORMAL,
            NotificationPriority.LOW -> {
                // Post system notification — user taps to open alert
                NotificationHelper.postAlertNotification(this, message)
            }
        }
    }

    private fun launchAlertViaPendingIntent(message: NotificationMessage): Boolean {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_NOTIFICATION_ID, message.id)
            putExtra(MainActivity.EXTRA_TITLE, message.title)
            putExtra(MainActivity.EXTRA_MESSAGE, message.message)
            putExtra(MainActivity.EXTRA_PRIORITY, message.priority)
            putExtra(MainActivity.EXTRA_TIMESTAMP, message.timestamp)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            message.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic().apply {
                    setPendingIntentBackgroundActivityStartMode(
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                    )
                }
                pendingIntent.send(this, 0, intent, null, null, null, options.toBundle())
            } else {
                pendingIntent.send()
            }
            true
        }.onFailure { error ->
            Log.e(TAG, "Pending-intent launch failed: ${error.message}")
        }.getOrDefault(false)
    }

    // ── WakeLock ──────────────────────────────────────────────────────────────

    private fun acquireWakeLock() {
        try {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock?.release()
            wakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK
                        or PowerManager.ACQUIRE_CAUSES_WAKEUP
                        or PowerManager.ON_AFTER_RELEASE,
                "TVNotify:CriticalAlert"
            ).also {
                it.acquire(WAKELOCK_TIMEOUT_MS)
                Log.i(TAG, "WakeLock acquired (${WAKELOCK_TIMEOUT_MS}ms)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire WakeLock: ${e.message}")
        }
    }

    fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
                Log.i(TAG, "WakeLock released")
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock: ${e.message}")
        }
    }

    // ── Launch Alert Activity ─────────────────────────────────────────────────

    private fun launchMainActivity(message: NotificationMessage) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_NOTIFICATION_ID, message.id)
            putExtra(MainActivity.EXTRA_TITLE, message.title)
            putExtra(MainActivity.EXTRA_MESSAGE, message.message)
            putExtra(MainActivity.EXTRA_PRIORITY, message.priority)
            putExtra(MainActivity.EXTRA_TIMESTAMP, message.timestamp)
        }
        startActivity(intent)
    }

    // ── Update Persistent Notification ────────────────────────────────────────

    private fun updateServiceNotification(state: ConnectionState) {
        val statusText = when (state) {
            is ConnectionState.Connected -> getString(R.string.notification_service_connected)
            is ConnectionState.Connecting -> "Connecting to server…"
            is ConnectionState.Reconnecting -> "Reconnecting… (attempt ${state.attempt})"
            is ConnectionState.Error -> "Error: ${state.message}"
            is ConnectionState.Disconnected -> getString(R.string.notification_service_disconnected)
        }

        val notification = NotificationHelper.buildServiceNotification(this, statusText)
        val manager = getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(NotifIds.FOREGROUND_SERVICE, notification)
    }
}
