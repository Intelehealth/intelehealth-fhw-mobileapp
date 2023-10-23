package org.intelehealth.ekalarogya.webrtc.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.ekalarogya.databinding.RowItemEkalCallLogBinding
import org.intelehealth.ekalarogya.webrtc.viewholder.EkalCallLogViewHolder
import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.chat.ui.adapter.BaseRecyclerViewAdapter
import org.intelehealth.klivekit.databinding.RowItemCallLogBinding

/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallLogAdapter(context: Context, list: List<RtcCallLog>) :
    BaseRecyclerViewAdapter<RtcCallLog>(context, list.toMutableList()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowItemEkalCallLogBinding.inflate(inflater, parent, false)
        return EkalCallLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EkalCallLogViewHolder) {
            holder.bind(getItem(position))
        }
    }
}