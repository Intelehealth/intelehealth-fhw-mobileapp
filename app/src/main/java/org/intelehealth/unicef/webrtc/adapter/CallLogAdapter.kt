package org.intelehealth.unicef.webrtc.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.unicef.webrtc.viewholder.UnicefCallLogViewHolder
import org.intelehealth.klivekit.call.model.RtcCallLog
import org.intelehealth.klivekit.chat.ui.adapter.BaseRecyclerViewAdapter
import org.intelehealth.klivekit.chat.ui.adapter.viewholder.BaseViewHolder
import org.intelehealth.unicef.databinding.RowItemEkalCallLogBinding

/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallLogAdapter(context: Context, list: List<RtcCallLog>) :
    BaseRecyclerViewAdapter<RtcCallLog>(context, list.toMutableList()) {
    lateinit var clickListener : BaseViewHolder.ViewHolderClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowItemEkalCallLogBinding.inflate(inflater, parent, false)
        return UnicefCallLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is UnicefCallLogViewHolder) {
            if(::clickListener.isInitialized) holder.setViewClickListener(clickListener)
            holder.bind(getItem(position))
        }
    }
}