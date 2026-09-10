package org.intelehealth.app.optimized_sync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

class PowerStateProvider(context: Context) {
    private val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
        context.registerReceiver(null, filter)
    }

    private fun isPowerLow(): Boolean {
        val batteryLevel: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val batteryScale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (batteryLevel < 0 || batteryScale <= 0) {
            // this condition handles failed battery read cases
            true
        } else {
            val batteryPct: Float = (batteryLevel * 100) / batteryScale.toFloat()
            return batteryPct <= OptimizedSyncConstants.LOW_BATTERY_THRESHOLD
        }
    }

    private fun isDeviceCharging(): Boolean {
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isBatteryCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        val isBatteryFull = status == BatteryManager.BATTERY_STATUS_FULL
        return isBatteryCharging || isBatteryFull
    }

    fun isPowerRequirementMet(): Boolean {
        return !isPowerLow() || isDeviceCharging()
    }
}