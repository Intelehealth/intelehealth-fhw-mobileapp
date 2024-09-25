package org.intelehealth.klivekit.binding

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import org.intelehealth.klivekit.R
import org.intelehealth.klivekit.chat.model.MessageStatus
import org.intelehealth.klivekit.chat.model.MessageStatus.Companion.getStatus

/**
 * Created by Vaghela Mithun R. on 14-08-2023 - 20:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/

@BindingAdapter("android:status")
fun setMessageStatusIcon(textView: TextView, messageStatus: Int) {
    getStatus(messageStatus).also {
        val drawable: Int
        val statusLbl: Int
        when (it) {
            MessageStatus.READ -> {
                drawable = R.drawable.ic_status_msg_read
                statusLbl = R.string.read
            }

            MessageStatus.DELIVERED -> {
                drawable = R.drawable.ic_status_msg_delivered
                statusLbl = R.string.read
            }

            MessageStatus.SENT -> {
                drawable = R.drawable.ic_status_msg_sent
                statusLbl = R.string.read
            }

            MessageStatus.SENDING -> {
                drawable = R.drawable.ic_status_msg_sending
                statusLbl = R.string.read
            }

            else -> {
                drawable = R.drawable.ic_status_msg_read
                statusLbl = R.string.read
            }
        }

        val index = textView.tag as Int
        textView.isVisible = index == 0
        textView.text = textView.context.getText(statusLbl)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
    }
}