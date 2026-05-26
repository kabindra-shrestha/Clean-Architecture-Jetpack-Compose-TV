package com.kabindra.tv.iptv

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kabindra.tv.iptv.domain.entity.AlertPayload
import com.kabindra.tv.iptv.domain.entity.NotificationPriority
import com.kabindra.tv.iptv.presentation.ui.theme.ColorCritical
import com.kabindra.tv.iptv.presentation.ui.theme.ColorHigh
import com.kabindra.tv.iptv.presentation.ui.theme.ColorNormal
import com.kabindra.tv.iptv.service.SocketForegroundService
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_PRIORITY = "extra_priority"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
    }

    private var payload: AlertPayload by mutableStateOf(AlertPayload())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar?.hide()

        enableEdgeToEdge()

        installSplashScreen()

        // ── Window flags to wake screen and show over lock screen ──
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        payload = payloadFromIntent(intent)
        val formatter = SimpleDateFormat("HH:mm:ss  dd MMM yyyy", Locale.getDefault())

        setContent {
            val priorityEnum = runCatching {
                NotificationPriority.valueOf(payload.priority.uppercase())
            }.getOrDefault(NotificationPriority.CRITICAL)

            val accentColor = when (priorityEnum) {
                NotificationPriority.CRITICAL -> ColorCritical
                NotificationPriority.HIGH -> ColorHigh
                else -> ColorNormal
            }

            val isCritical = priorityEnum == NotificationPriority.CRITICAL

            App(
                modifier = Modifier,
                payload = payload.takeIf { it.notifId.isNotBlank() },
                onPayloadConsumed = { consumePayload() },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        payload = payloadFromIntent(intent)
    }

    /*@Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Block back-button dismissal for CRITICAL priority
        val isCritical = runCatching {
            NotificationPriority.valueOf(payload.priority.uppercase()) == NotificationPriority.CRITICAL
        }.getOrDefault(false)

        if (!isCritical) {
            dismissAlert()
        }
        // For CRITICAL: back press is ignored — must use Dismiss button
    }*/

    private fun dismissAlert() {
        // Release wakelock via service if available
        releaseWakeLockFromService()
        finish()
    }

    private fun consumePayload() {
        payload = AlertPayload()
    }

    private fun releaseWakeLockFromService() {
        try {
            val serviceIntent = Intent(this, SocketForegroundService::class.java)
            serviceIntent.action = SocketForegroundService.ACTION_RELEASE_WAKELOCK
            startService(serviceIntent)
        } catch (e: Exception) {
            // No-op if service not available
        }
    }

    private fun payloadFromIntent(intent: Intent?): AlertPayload {
        return AlertPayload(
            notifId = intent?.getStringExtra(EXTRA_NOTIFICATION_ID) ?: "",
            title = intent?.getStringExtra(EXTRA_TITLE) ?: "Alert",
            message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "",
            priority = intent?.getStringExtra(EXTRA_PRIORITY) ?: "NORMAL",
            timestamp = intent?.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
                ?: System.currentTimeMillis()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLockFromService()
    }
}
