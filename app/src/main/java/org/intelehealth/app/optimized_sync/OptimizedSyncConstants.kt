package org.intelehealth.app.optimized_sync

import org.intelehealth.app.app.AppConstants

object OptimizedSyncConstants {
    const val LOW_BATTERY_THRESHOLD: Float = 15f

    // Notification. The channel name, title and body are user-facing and so are read from resources
    // in OptimizedSyncForegroundInfo; only the identifiers belong here.
    const val SYNC_CHANNEL_ID: String = "optimized_sync_id"
    const val NOTIFICATION_ID: Int = 69

    /**
     * Minutes, taken from the existing schedule rather than restated. eZazi runs this every two hours;
     * every other project including this one syncs on a fifteen-minute cadence, so the value is read
     * from [AppConstants.REPEAT_INTERVAL] to stay in step if that is ever retuned.
     */
    val PERIODIC_WORK_INTERVAL_MINUTES: Long = AppConstants.REPEAT_INTERVAL.toLong()

    const val UNIQUE_PERIODIC_WORK_NAME: String = "OPTIMIZED_SYNC_PERIODIC"
    const val UNIQUE_ONE_TIME_WORK_NAME: String = "OPTIMIZED_SYNC_ONE_TIME"
}
