package com.kabindra.mobile.iptv

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.kabindra.mobile.iptv.di.initKoin
import com.kabindra.mobile.iptv.service.SocketForegroundService
import com.kabindra.mobile.iptv.utils.notification.NotificationHelper
import com.kabindra.mobile.iptv.worker.SocketHealthWorker
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {

    companion object {
        @Volatile
        private var resumedActivityCount: Int = 0

        @Volatile
        private var uiHiddenBySystem: Boolean = false

        /**
         * True only when at least one activity is resumed (interactive).
         * This is more reliable for Home-button transitions on TV launchers than
         * tracking started activities.
         */
        fun isAppInForeground(): Boolean = resumedActivityCount > 0 && !uiHiddenBySystem
    }

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
        }

        Log.i("TVNotifyApp", "Application starting…")

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                Log.d(
                    "TVNotifyApp",
                    "onActivityStarted(${activity.localClassName}) -> resumed=0"
                )
            }

            override fun onActivityStopped(activity: Activity) {
                Log.d(
                    "TVNotifyApp",
                    "onActivityStopped(${activity.localClassName}) -> resumed=0"
                )
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityResumed(activity: Activity) {
                uiHiddenBySystem = false
                resumedActivityCount += 1
                Log.d(
                    "TVNotifyApp",
                    "onActivityResumed(${activity.localClassName}) -> resumed=${resumedActivityCount}"
                )
            }

            override fun onActivityPaused(activity: Activity) {
                resumedActivityCount = (resumedActivityCount - 1).coerceAtLeast(0)
                Log.d(
                    "TVNotifyApp",
                    "onActivityPaused(${activity.localClassName}) -> resumed=${resumedActivityCount}"
                )
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) {
                Log.d(
                    "TVNotifyApp",
                    "onActivityDestroyed(${activity.localClassName}) -> resumed=${resumedActivityCount}"
                )
            }
        })

        // Create notification channels before any notifications are posted
        NotificationHelper.createChannels(this)

        // Start the socket Foreground Service
        SocketForegroundService.startService(this)

        // Enqueue WorkManager watchdog
        SocketHealthWorker.enqueue(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            uiHiddenBySystem = true
            resumedActivityCount = 0
            Log.d("TVNotifyApp", "onTrimMemory(UI_HIDDEN) -> forced background")
        }
    }
}