package org.intelehealth.app.activities.user.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.intelehealth.app.utilities.SessionManager

class AppUsageTrackerNew(context: Context,
                         private val sessionManager: SessionManager
) : DefaultLifecycleObserver {

    private val prefs = context.getSharedPreferences("usage_prefs", Context.MODE_PRIVATE)

    // Persist startTimeMillis in SharedPreferences
    private var startTimeMillis: Long
        get() = prefs.getLong("start_time_millis", 0L)
        set(value) = prefs.edit().putLong("start_time_millis", value).apply()

    // Persist accumulated usage time
    private var currentUnsavedTimeMillis: Long
        get() = prefs.getLong("unsaved_usage_time", 0L)
        set(value) = prefs.edit().putLong("unsaved_usage_time", value).apply()

    fun startTracking() {

        if (!sessionManager.isLoggedIn) {
            return
        }
        if (startTimeMillis == 0L) {  // Only start if not already started
            startTimeMillis = System.currentTimeMillis()
        } else {
        }
    }

    fun stopTracking() {
        if (!sessionManager.isLoggedIn) {
            return
        }
        if (startTimeMillis != 0L) {
            val sessionTime = System.currentTimeMillis() - startTimeMillis
            currentUnsavedTimeMillis += sessionTime
            startTimeMillis = 0L  // Clear saved start time after stopping
        } else {
        }
    }

    fun getUnsavedTime(): Long {
        val runningSession = if (startTimeMillis != 0L) {
            System.currentTimeMillis() - startTimeMillis
        } else 0L
        val total = currentUnsavedTimeMillis + runningSession
        return total
    }

    fun clearUnsavedTime() {
        startTimeMillis = 0L
        currentUnsavedTimeMillis = 0L
    }
}