package com.kabindra.tv.iptv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kabindra.tv.iptv.service.SocketForegroundService

private const val TAG = "BootReceiver"

// ─────────────────────────────────────────────────────────────────────────────
// BootReceiver
//
// Starts the SocketForegroundService automatically after:
//  - Device boot (BOOT_COMPLETED)
//  - App package update (MY_PACKAGE_REPLACED)
// ─────────────────────────────────────────────────────────────────────────────

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.i(TAG, "Boot/update received — starting SocketForegroundService")
                SocketForegroundService.startService(context)
            }
        }
    }
}
