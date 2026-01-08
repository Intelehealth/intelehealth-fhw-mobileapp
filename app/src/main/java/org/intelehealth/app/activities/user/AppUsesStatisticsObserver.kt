package org.intelehealth.app.user

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.activities.user.api.AppUsageTrackerNew
import timber.log.Timber

class AppUsesStatisticsObserver(
    private val userSessionRepository: UserSessionRepository,
    private val unsavedUsageTracker: AppUsageTrackerNew

) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        if (BuildConfig.FLAVOR_client == "ekal") {
            userSessionRepository.startSession()
            unsavedUsageTracker.startTracking()
        }
        super.onStart(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        if (BuildConfig.FLAVOR_client == "ekal") {
            userSessionRepository.endSession()
            unsavedUsageTracker.stopTracking()
            unsavedUsageTracker.clearUnsavedTime()
        }
        super.onStop(owner)
    }

}
