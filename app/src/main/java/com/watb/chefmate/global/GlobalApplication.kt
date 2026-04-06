package com.watb.chefmate.global

import android.app.Application
import android.util.Log
import com.watb.chefmate.api.SessionRepository
import com.watb.chefmate.helper.ChatSnapshotStore
import com.watb.chefmate.network.NetworkMonitor

class GlobalApplication : Application() {
    companion object {
        private const val TAG = "GlobalApplication"
        var networkMonitor: NetworkMonitor? = null
    }

    override fun onCreate() {
        super.onCreate()
        SessionRepository.init(this)
        ChatSnapshotStore.init(this)
        networkMonitor = NetworkMonitor(applicationContext)
        Log.d(TAG, "networkMonitor created: $networkMonitor")
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor?.cleanup()
    }
}
