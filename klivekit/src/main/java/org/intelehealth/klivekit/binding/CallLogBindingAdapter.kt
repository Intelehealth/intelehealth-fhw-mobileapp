package org.intelehealth.klivekit.binding

import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import org.intelehealth.klivekit.R
import org.intelehealth.klivekit.call.model.RtcCallLog

/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 16:05.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@BindingAdapter("android:callStatus")
fun setCallStatus(view: AppCompatImageView, callLog: RtcCallLog) {

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
fun setCallStatusColor(view: TextView, callLog: RtcCallLog) {
    var statusIcon = R.color.colorOutgoingCall
    if (callLog.callMode.isIncomingCall() && callLog.callStatus.isMissed()) {
        statusIcon = R.color.colorMissedCall
    } else if (callLog.callMode.isIncomingCall()) {
        statusIcon = R.color.colorIncomingCall
    }

    view.setTextColor(ContextCompat.getColor(view.context, statusIcon))
}
