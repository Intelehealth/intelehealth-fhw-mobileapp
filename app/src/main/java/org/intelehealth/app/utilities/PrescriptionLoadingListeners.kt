package org.intelehealth.app.utilities

interface PrescriptionLoadingListeners {
    fun isReceivedRecentLoaded(status:Boolean)
    fun isReceivedOldLoaded(status:Boolean)
    fun isPendingRecentLoaded(status:Boolean)
    fun isPendingOldLoaded(status:Boolean)
}