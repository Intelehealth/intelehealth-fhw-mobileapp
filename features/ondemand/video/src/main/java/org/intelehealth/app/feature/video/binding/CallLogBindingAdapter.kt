package org.intelehealth.app.feature.video.binding

import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import org.intelehealth.app.feature.video.R
import org.intelehealth.app.feature.video.model.VideoCallLog

/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 16:05.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@BindingAdapter("android:callStatus")
fun setCallStatus(view: AppCompatImageView, callLog: VideoCallLog) {

    var bg = R.drawable.call_outgoing_status_bg;
    var statusIcon = R.drawable.ic_call_outgoing
    if (callLog.callMode.isIncomingCall() && callLog.callStatus.isMissed()) {
        bg = R.drawable.call_missed_status_bg;
        statusIcon = R.drawable.ic_call_missed
    } else if (callLog.callMode.isIncomingCall()) {
        bg = R.drawable.call_incoming_status_bg;
        statusIcon = R.drawable.ic_call_incoming
    }

    view.setBackgroundDrawable(ContextCompat.getDrawable(view.context, bg))
    view.setImageDrawable(ContextCompat.getDrawable(view.context, statusIcon))
}

@BindingAdapter("android:callStatusColor")
fun setCallStatusColor(view: TextView, callLog: VideoCallLog) {
    var statusIcon = org.intelehealth.app.R.color.colorOutgoingCall
    if (callLog.callMode.isIncomingCall() && callLog.callStatus.isMissed()) {
        statusIcon = org.intelehealth.app.R.color.colorMissedCall
    } else if (callLog.callMode.isIncomingCall()) {
        statusIcon = org.intelehealth.app.R.color.colorIncomingCall
    }

    view.setTextColor(ContextCompat.getColor(view.context, statusIcon))
}
