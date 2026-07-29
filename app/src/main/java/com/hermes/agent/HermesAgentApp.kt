package com.hermes.agent

import android.app.Application
import android.util.Log

/**
 * Application class for Hermes Agent.
 * Initializes global settings and monitoring.
 */
class HermesAgentApp : Application() {

    companion object {
        private const val TAG = "HermesAgentApp"
        lateinit var instance: HermesAgentApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "Hermes Agent Android client initialized")
    }
}
