package org.intelehealth.videolibrary.listing.activity

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

fun ProgressBar?.checkAndHideProgressBar() {
    if (this?.visibility == View.VISIBLE) {
        this.visibility = View.GONE
    }
}

fun SwipeRefreshLayout?.checkAndHideProgressBar() {
    if (this?.isRefreshing == true) {
        this.isRefreshing = false
    }
}