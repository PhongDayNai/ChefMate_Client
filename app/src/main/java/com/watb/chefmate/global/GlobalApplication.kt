package com.watb.chefmate.global

import android.app.Application
import android.util.Log
import com.watb.chefmate.network.NetworkMonitor

class GlobalApplication : Application() {
    companion object {
        private const val TAG = "GlobalApplication"
        var networkMonitor: NetworkMonitor? = null
    }

    override fun onCreate() {
        super.onCreate()
        networkMonitor = NetworkMonitor(applicationContext)
        Log.d(TAG, "networkMonitor created: $networkMonitor")
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor?.cleanup()
    }
}