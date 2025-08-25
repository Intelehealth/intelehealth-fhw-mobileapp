package org.intelehealth.app.activities.achievements.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

class AppUsageTracker(private val context: Context) : DefaultLifecycleObserver {

    private var startTimeMillis: Long = 0
    private var totalUsageTimeMillis: Long = 0

    private val prefs = context.getSharedPreferences("usage_prefs", Context.MODE_PRIVATE)

    init {
        // Load saved total time when tracker is created
        totalUsageTimeMillis = prefs.getLong("total_usage_time", 0L)
    }

    override fun onStart(owner: LifecycleOwner) {
        startTimeMillis = System.currentTimeMillis()
    }

    override fun onStop(owner: LifecycleOwner) {
        val endTime = System.currentTimeMillis()
        val sessionTime = endTime - startTimeMillis
        totalUsageTimeMillis += sessionTime

        // Save the updated total time to SharedPreferences
        prefs.edit().putLong("total_usage_time", totalUsageTimeMillis).apply()
    }

    fun getTotalUsageTimeMillis(): Long {
        // Add current session time if app is running
        return totalUsageTimeMillis + (System.currentTimeMillis() - startTimeMillis)
    }
}
