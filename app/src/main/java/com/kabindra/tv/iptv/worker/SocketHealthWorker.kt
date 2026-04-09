package com.kabindra.tv.iptv.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kabindra.tv.iptv.service.SocketForegroundService
import java.util.concurrent.TimeUnit

private const val TAG = "SocketHealthWorker"
private const val WORK_NAME = "socket_health_check"

// ─────────────────────────────────────────────────────────────────────────────
// SocketHealthWorker
//
// A PeriodicWorkRequest that runs every 15 minutes and:
//  1. Checks whether SocketForegroundService is alive
//  2. Restarts it if it has been killed by the OS
//
// This acts as an external watchdog for the Foreground Service.
// ─────────────────────────────────────────────────────────────────────────────

class SocketHealthWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Health check running…")

        if (!SocketForegroundService.isRunning()) {
            Log.w(TAG, "Service not running — restarting…")
            SocketForegroundService.startService(context)
        } else {
            Log.i(TAG, "Service is alive — no action needed")
        }

        return Result.success()
    }

    companion object {
        /**
         * Enqueue the periodic health check.
         * Call this from Application.onCreate().
         * Uses KEEP policy to avoid duplicate workers.
         */
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<SocketHealthWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

            Log.i(TAG, "Periodic health check enqueued (15 min interval)")
        }

        /**
         * Cancel the periodic health check (e.g., on explicit user sign-out).
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.i(TAG, "Health check cancelled")
        }
    }
}
