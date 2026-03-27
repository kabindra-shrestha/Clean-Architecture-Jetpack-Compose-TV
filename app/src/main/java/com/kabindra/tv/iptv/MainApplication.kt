package com.kabindra.tv.iptv

import android.app.Application
import com.kabindra.tv.iptv.di.initKoin
import org.koin.android.ext.koin.androidContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
        }
    }
}