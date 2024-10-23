package org.intelehealth.app.feature.video.ui.adapter

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.feature.video.ui.viewholder.EkalCallLogViewHolder
import org.intelehealth.core.ui.adapter.BaseRecyclerViewAdapter
import org.intelehealth.core.ui.viewholder.BaseViewHolder
import org.intelehealth.app.feature.video.databinding.RowItemEkalCallLogBinding
import org.intelehealth.app.feature.video.model.VideoCallLog

/**
 * Created by Vaghela Mithun R. on 23-10-2023 - 15:59.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class CallLogAdapter(context: Context, list: List<VideoCallLog>) :
    BaseRecyclerViewAdapter<VideoCallLog>(context, list.toMutableList()) {
    lateinit var clickListener : BaseViewHolder.ViewHolderClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RowItemEkalCallLogBinding.inflate(inflater, parent, false)
        return EkalCallLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EkalCallLogViewHolder) {
            if(::clickListener.isInitialized) holder.setViewClickListener(clickListener)
            holder.bind(getItem(position))
        }
    }
}